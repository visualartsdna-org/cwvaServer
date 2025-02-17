package support

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes
import tsh.TopicShorthand
import org.apache.jena.rdf.model.ModelFactory

class Authoring {
	
	static def verbose=false
	def work="/work/author/ttl"
//	def tFile = "rspates.art.ttl"
	def aFile = "topics.ttl"
	def prefixes = Prefixes.forQuery

	def ju = new JenaUtilities()
	def model
	def abox 
	def tbox 
	def account
	def base
	
	Authoring(dbm){
		base = dbm
	}
	
	def id=100
	def ms = System.currentTimeMillis()
	def genId() {
		"${ms}_${id++}"
	}
	
	def diff(n1,m1,n2,m2) {
		
		def s = ""
		def d1 = m1.difference(m2)
		def d2 = m2.difference(m1)
		
		s+= "$n1 not in $n2-------------\n"
		s+= ju.saveModelString(d1,"ttl")
		s+= "$n2 not in $n1-------------\n"
		s+= ju.saveModelString(d2,"ttl")
	}
	
	def difference() {
		def dir = "$work/$account"
		// load last version of model
		def vfile = []
		new File(dir).eachFile{file->
			if (file.name =~ /topics\.ttl[0-9]+/)
				vfile += file
		}
		if (vfile.isEmpty()) 
			return "no version files"
		vfile.sort()
		def n2 = vfile[0].name
		def m2 = ju.loadFileModel(dir,n2)    //loadModelString(vfile[0].text)
		diff("$aFile", abox, "$n2", m2)
	}
	
	def versioning() {
		def dir = "$work/$account"
		def status = "version "
		if (new File("$dir/$aFile").exists()) {
			def newFile = "$dir/$aFile${System.currentTimeMillis()}"
			new File(newFile).text = new File("$dir/$aFile").text
			status += "$newFile created. "
		}
		status
	}
	
	def saveSession() {
		def status = "save "
		def dir = "$work/$account"
		ju.saveModelFile(abox,"$dir/$aFile","ttl")
		status += "$dir/$aFile"
	}
	
	def loadSession(acct) {
		account = acct
		def dir = "$work/$account"
		def tFile = "${acct}.ttl"
		tbox = ju.loadFile("$dir/$tFile")
		if (base) {
			tbox.add base
		}
		try {
			abox = ju.loadFile("$dir/$aFile")
		} catch (FileNotFoundException fnfe) {
			abox = ju.newModel()
		}
		initSession()
	}
	def adds=0
	def deletes=0
	def initSession() {
		model = ModelFactory.createRDFSModel(tbox, abox)
		
		adds=0
		def deleted=execOldTopicsRemove()
		deletes = deleted.size()
		println "deleted"
		deleted.each{
			println "\t${subPrefix(it.s)}"
		}
		def ttl = ""
		def lcs = qConceptScheme()
		lcs.each{sch->
			def lc = qConceptsInScheme(sch.s)
			lc.each{top->
				adds++
				ttl += """
	tko:t${genId()}	a    tko:Topic ;
        tko:head         $top.s ;
        tko:memberList  "[]" ;
	.
"""
			}
		}
		abox.add ju.saveStringModel("$prefixes\n$ttl", "ttl")
		
		println "added"
		println ttl
		//println ju.saveModelString(abox)
		[adds:adds,deletes:deletes]
	}
	
	def addConceptTopic(top) {
		adds++
		def ttl = """
	tko:t${genId()}	
		a    tko:Topic ;
		a    tko:ConceptTopic ;
        tko:head         $top ;
        tko:memberList  "[]" ;
	.
"""
		abox.add ju.saveStringModel("$prefixes\n$ttl", "ttl")

	}
	
	def subPrefix(s) {
		s.replaceAll("http://visualartsdna.org/takeout/","tko:")
		.replaceAll("http://visualartsdna.org/thesaurus/","the:")
		.replaceAll("http://visualartsdna.org/2021/07/16/model#","vad:")
		.replaceAll("http://visualartsdna.org/work/","work:")
	}
	
	def normalize(l) {
		def l2 = []
		l.each{m->
			def m2=[:]
			m2.s = subPrefix(m.s)
			m2.l = m.l
			l2 += m2
			}
			l2
	
	}
	
	// remove links with missing head refs
	def execOldTopicsRemove() {
		def l = ju.queryListMap1(model, prefixes, """
select   ?s {
		?s a tko:Topic ;
			tko:head ?topic .
		filter not exists {
			?topic ?p2 ?o2
		}
}
""")
		l.each{
		ju.queryExecUpdate(model,prefixes, """
delete {${subPrefix(it.s)} ?p ?o }
where {
		${subPrefix(it.s)} ?p ?o
}
""")
		}
	l
	}
	
	
	def qConceptScheme() {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s a tko:KeepConceptScheme ;
			skos:prefLabel ?l
} order by ?l
""")
	normalize(l)
	}
	
	def qConceptSchemeTopConcept() {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s0 a tko:KeepConceptScheme ;
			skos:hasTopConcept ?s .
		?s skos:prefLabel ?l
} order by ?l
""")
	normalize(l)
	}
	
	def qConceptsInScheme(sch) {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s a skos:Concept ;
			skos:inScheme $sch ;
			skos:prefLabel ?l .
		filter not exists {
		?s2 a tko:Topic ;
			tko:head ?s .
		}
} order by ?l
""")
	normalize(l)
	}
	
	def qTopics() {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s a skos:Concept ;
			skos:prefLabel ?l .
		?s2 a tko:Topic ;
			tko:head ?s
} order by ?l
""")
	normalize(l)
	}
	
	def qConcepts() {
//		def l = ju.queryListMap1(model, prefixes, """
//select distinct ?s ?l {
//		?s a skos:Concept ;
//			skos:prefLabel ?l 
//} order by ?l
//""")
		def l2 = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s a skos:Concept ;
			{
			?s skos:prefLabel ?l
			} union
			{
			?s rdfs:label ?l
			}
} order by ?l
""")
//	l.addAll(l2)
	normalize(l2)
	}
	
	def qTopicExists(concept) {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s {
		?s a tko:Topic ;
		tko:head $concept
}
""")
		!l.isEmpty()
	}
	
	def qSubTopics(list) {
		def ls = ""
		int i=0
		list.each{ 
			if (i++ >0) ls += ","
			ls += it 
			}
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s a skos:Concept ;
			skos:prefLabel ?l .
		?s2 a tko:Topic ;
			tko:head ?s .
		filter (?s not in ($ls))
} order by ?l
""")
	normalize(l)
	}
	
	def qTest(topic) {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s2 {
		bind($topic as ?s)
		?s skos:prefLabel ?l .
		bind(substr(str(?s),34) as ?s2)
}""")
	l
	}
	
	def qTopicLinks(topic) {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?ml {
		?s0 a tko:Topic ;
			tko:head $topic ;
			tko:memberList ?ml .
}""")
		def ml = new JsonSlurper().parseText(l.ml)
		def lAll = []
		ml.each{
		def l2 = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		${it} a tko:Topic ;
			tko:head ?s .
			{
			?s skos:prefLabel ?l
			} union
			{
			?s rdfs:label ?l
			}
}""")
			lAll += l2
		}
	normalize(lAll)
	}
	
	def qLinkStr(lt) {
		def l= []
		lt.each{
			def l0 = ju.queryListMap1(model, prefixes, """
select distinct ?s {
		?s a tko:Topic ;
			tko:head $it .
}""")
			l += "${subPrefix(l0[0].s)}"
		}
		l
	}
	
	
	def execTopicLinks(topic, links) {
		def ls = qLinkStr(links)
		int i=0
		def n=links.size()
		def ins = new JsonOutput().toJson(ls)

		ju.queryExecUpdate(model,prefixes, """
delete {?s tko:memberList ?ml }
insert {?s tko:memberList \"\"\"$ins\"\"\"}
where {
		?s tko:head $topic .
		?s tko:memberList ?ml .
}
""")
	}
	

	def add(topic, links) {
		def l = []
		links.each{
			l += it.s
		}
		def ls = l 
		ls += topic
		ls
	}
	def remove(topic, links) {
		def l=[]
		def n = -1
		int i=0
		links.each{
			if (it.s == topic) n = i
			l += it.s
			i++
		}
		l.remove(n)
		l
	}
	def move(topic, links, dif) {
		def l=[]
		def n = -1
		int i=0
		links.each{
			if (it.s == topic) n = i
			l += it.s
			i++
		}
		if (dif == -1 && n>0
		|| dif == 1 && n < links.size()-1) {
			def t = l[n]
			l[n] = l[n + dif]
			l[n + dif] = t
		}
		l
	}
	def moveUp(topic, links) {
		move(topic, links, -1)
	}
	
	def moveDown(topic, links) {
		move(topic, links, 1)
	}
	
	/*
	 * for exclusion in subTopic select list,
	 * find all topic refs used in memberlists
	 * add to that the topconcept and
	 * currently selected topic from session
	 */
	def getHierarchy(m) {
		def g = getGraph(abox)
		def set=[m.selTopic,m.topConcept[0].s]
		g.each{
			def l = new JsonSlurper().parseText(it["memberList"])
			l.each{t->
				def h = g.find{
					it["@id"] == t
				}
				if (h)
					set += h.head
			}
		}
		set
	}
	
	def handleQueryParams(m) {
		
		if (m.selectAccount) {
			loadSession(m.selectAccount)
			m=[:]
		}
		
		m.selectSchemeOpts = qConceptScheme()
		m.topConcept = qConceptSchemeTopConcept()
		if (!m.selTopic)
			m.selTopic = m.topConcept[0].s
		m.selectTopicOpts = qTopics()
		
		m.linkTopics = qTopicLinks(m.selTopic ? m.selTopic : m.selectTopicOpts[0].s)
		m.selectConcepts = qConcepts()
		if (!m.selCptSch)
			m.selCptSch = m.selectSchemeOpts[0].s
		
		
		switch(m.action) {
			
			case 'moveUp': // linkSubTopic
			if (m.linkSubTopic
				&& m.linkSubTopic != m.linkTopics[0].s) {
				
				def l = moveUp(m.linkSubTopic, m.linkTopics)
				execTopicLinks(m.selTopic, l)
				
				saveSession()
			}
			
			break
			case 'moveDown': 
			if (m.linkSubTopic
				&& m.linkSubTopic != m.linkTopics[m.linkTopics.size()-1].s) {
				
				def l = moveDown(m.linkSubTopic, m.linkTopics)
				execTopicLinks(m.selTopic, l)
				
				saveSession()
			}

			break
			case 'removeSel': 
			if (m.linkSubTopic) {
				
				def l = remove(m.linkSubTopic, m.linkTopics)
				execTopicLinks(m.selTopic, l)
				
				saveSession()
			}

			break
			case 'clearList': 
				
				def l = []
				execTopicLinks(m.selTopic, l)
				
				saveSession()
			break
			case 'addSubTopic':
			if (m.selSubTopic) {
				
				def l = add(m.selSubTopic, m.linkTopics)
				execTopicLinks(m.selTopic, l)
				
				saveSession()
			}
				
			break
			case 'addConcept':
			if (m.concept) {
				
				def l = add(m.concept, m.linkTopics)
				// if a concept/topic does not exist
				// add it here
				if (!qTopicExists(m.concept))
					addConceptTopic(m.concept)
				execTopicLinks(m.selTopic, l)
				
				saveSession()
			}
				
			break 
			case 'moveToTop':
			// move to top concept
				m.selTopic = m.topConcept[0].s
		
			break
			case 'moveUpLevel':
			// locate current topic as a linked subtopic
			// in the graph hierarchy
			// change the selected topic to the link head
			// reload as if topic selected
//			if (m.linkSubTopic
//				&& m.linkSubTopic != m.linkTopics[0].s) {
//				
//				def l = moveUp(m.linkSubTopic, m.linkTopics)
//				execTopicLinks(m.selTopic, l)
//				
//			}
			
			break
			case 'version':
			println versioning()
			break
			
		}
		
		// catch any revision from add, delete or move actions
		m.linkTopics = qTopicLinks(m.selTopic ? m.selTopic : m.selectTopicOpts[0].s)
		
		// get subtopics after any add/delete actions
		m.selectSubTopicOpts = qSubTopics(getHierarchy(m))
		
		if (verbose) {
			println ""
			m.each{k,v->
				println "$k=$v"
			}
		}
		if (verbose) println "model=${model.size()}"
		
		printHtml(m)
	}
	
	def getGraph( m) {

		def s = ju.saveModelString(m,"json-ld")
		//println "\n$s\n"
		def map = new JsonSlurper().parseText(s)
		map["@graph"]
	}

	def printTopics(sch) {
		def sb = new StringBuilder()
		def l = getGraph(model)
		def topCpt = ""
		l.each {
			if (it["@id"] == sch) {
				topCpt = it.hasTopConcept
			}
		}
		printTopic(topCpt,l,sb,1)
		""+sb

	}
	
	def rehost(s) {s}
	
	def printTopic(topCpt, l, sb, n) {
		
		def topic = l.find{
			it.head == topCpt
		}
		def ltop = new JsonSlurper().parseText(topic.memberList)
		
		def cpt = l.find{
			it["@id"] == topCpt
		}
		def defn = cpt.definition ? cpt.definition : cpt.description
		def image = ""
		def dim = ""
//		if (cpt.label=="Adelaide Sketch") {
//			println "here"
//		}
		if (cpt.image) {
			def size = "width=\"500\""
			if (cpt.height && cpt.width) { // assume cpt is a work
				def h = cpt.height as int
				def w = cpt.width as int
				def year = cpt.dateCreated.substring(0,4)
				dim = "($year, $h x $w in)"
				size = h<w ? "height=\"400\"" : "width=\"400\""
			}
			if (cpt.image instanceof List) {
				image = ""
				cpt.image.each {
					image += """<img src="${rehost(it)}" $size><br/><br/>"""
				}
			} else {
				image = """<img src="${rehost(cpt.image)}" $size><br/><br/>"""
			}
		}
		sb.append """
<h$n>
${cpt.prefLabel ? cpt.prefLabel : cpt.label}
</h$n>
${defn} $dim
<br/>
<br/>
${image}
"""
		ltop.each{top->
			def c = l.find {
				it["@id"] == top
			}
			if (c) printTopic(c.head, l, sb, n+1)
			
		}
	}
	
	// TODO: like in rdfUtil-tsh
	// locate inline op, e.g., [inline:concept]
	// extract concept
	// derive definition from concept
	// insert into text replacing op
	def inlineConcept(m3, map) {
		def defn = m3.definition
		(defn =~ /(\[inline\:.*?\])/).collect{
			it[1]
		}.each{
			def cpt = (it =~ /\[inline\:(.*)\]/)[0][1]
			println cpt
			defn = defn.replaceAll("\\$it",map["tko:$cpt"].definition+"\n")
		}
		defn
	}
		
	def printHtml(m) {
				
		 def html = """
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style>
table, th, td {
  #border: 1px solid black;
  border-collapse: collapse;
}
th, td {
  padding-top: 5px;
  padding-bottom: 5px;
  padding-left: 5px;
  padding-right: 5px;
}
.button {
  background-color: #f0f0f0; /* Gray */
  border:  1px solid #e4e4e4;
  color: black;
  padding: 5px 2px;
  text-align: center;
  text-decoration: none;
  display: inline-block;
  font-size: 12px;
	border-radius: 3px;
		cursor: pointer;
}
img {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 5px;
}
</style>
    </head>
	<body>
<script>
function setAction(x) {
	document.getElementById("action").value=x;
	document.getElementById("myForm").submit();
}
function resetFunction() {
  document.getElementById("myForm").reset();
}

window.addEventListener("beforeunload", () => {
  localStorage.setItem("scrollPositon", document.querySelector("#contents_container").scrollTop);
});
window.addEventListener("load", () => {
  document.querySelector("#contents_container").scrollTop = localStorage.getItem("scrollPositon") || 0;
});

</script>

<h3>
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
Authoring</h3>

<table>
<tr>
<td>

<form id="myForm" action="/author_page.entry" method="get">
<table>
<tr>
<td>
<input type="hidden" id="action" name="action" value="">
<label for="selectScheme">Select Concept Scheme</label><br>
<select name="selectScheme" id="selectScheme" onchange="setAction('selCptSch')">
"""
		 m.selectSchemeOpts.each{m0->
			 html += """
<option value="${m0.s}" ${m.selectScheme==m0.s ? "selected" : ""}>${m0.l}</option>
"""
		 }

		 html+=
"""
</option>
</select>
<br>
<br>
Top concept: ${m.topConcept[0].l}
<br>
<br>
Select Topic
<br>
<select name="selTopic" id="selTopic" onchange="setAction('selTopic')">
"""
		 m.selectTopicOpts.each{m0->
			 html += """
<option value="${m0.s}" ${m.selTopic==m0.s ? "selected" : ""}>${m0.l}</option>
"""
		 }

		 html+=
"""
</select>
</td></tr><tr><td>
	<button onclick="setAction('moveToTop')">Move to top</button>
<br>
<br>
<!--
	<button onclick="setAction('moveUpLevel')">Move up a level</button>
<br>
<br>
-->
<br>
Linked Subtopics
<br>
<select name="linkSubTopic" id="linkSubTopic" size="5">
"""
		 m.linkTopics.each{m0->
			 html += """
<option value="${m0.s}" ${m.linkSubTopic==m0.s ? "selected" : ""}>${m0.l}</option>
"""
		 }

		 html+=
"""
</select>
</td></tr><tr><td>
	<button onclick="setAction('moveUp')">Move up</button>
</td></tr><tr><td>
	<button onclick="setAction('moveDown')">Move down</button>
</td></tr><tr><td>
	<button onclick="setAction('removeSel')">Remove selected</button>
</td></tr><tr><td>
	<button onclick="setAction('clearList')"">Clear list</button>
</td></tr><tr><td>
<br>
<br>
Choose a Substopic
<br>
<select name="selSubTopic" id="selSubTopic"> <!-- onchange="setAction('selSubTopic')"> -->
"""
		 m.selectSubTopicOpts.each{m0->
			 html += """
<option value="${m0.s}" ${m.selSubTopic==m0.s ? "selected" : ""}>${m0.l}</option>
"""
		 }

		 html+=
"""
</select>
<br/>
</td></tr><tr><td>
	<button onclick="setAction('addSubTopic')">Add subtopic</button>
</td></tr><tr><td>
<br>
Choose a Concept
<br>
<select name="concept" id="concept">
"""
		 m.selectConcepts.each{m0->
			 html += """
<option value="${m0.s}" ${m.concept==m0.s ? "selected" : ""}>${m0.l}</option>
"""
		 }

		 html+=
"""
</select>
</td></tr><tr><td>
	<button onclick="setAction('addConcept')">Add concept</button>
<br>
<br>
<br>
	<button onclick="setAction('version')">Version</button>
<!--
<input type="button" onclick="resetFunction()" value="Reset form">
-->
<br>
</td></tr><tr><td>
  <br><label for="dir">Save dir:</label><br>
  <input type="text" id="dir" name="dir" size="25" value="/work/author/ttl">
<br>
<hr>
Links: added $adds, deleted $deletes; dbm ${base ? "enabled" : "disabled"}
</td></tr>
</table>
</form>
</td>
<td>
<div id="contents_container" style="overflow-y: scroll; max-height: 88vh;">

${printTopics(m.selCptSch)}
</div>
</td>
</tr>
</table>
<br>

version 1.1
</body></html>
"""
	html
	}
	
	
}
