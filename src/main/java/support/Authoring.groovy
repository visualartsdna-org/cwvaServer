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

class Authoring {
	
	static def dir="/work/author/ttl"
	def prefixes = Prefixes.forQuery

	def ju = new JenaUtilities()
	def model = ju.loadFiles(dir) // TODO initializing here creates problems for dir redef?!
	def tsh = new TopicShorthand()
	
	Authoring(){
	}
	
	def query(s) {
		
	}
	
	def subPrefix(s) {
		s.replaceAll("http://visualartsdna.org/takeout/","tko:")
		.replaceAll("http://visualartsdna.org/thesaurus/","the:")
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
	
	def qConceptScheme() {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s a tko:KeepConceptScheme ;
			skos:prefLabel ?l
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
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s a skos:Concept ;
			skos:prefLabel ?l 
} order by ?l
""")
	normalize(l)
	}
	
	def qSubTopics(topic) {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s a skos:Concept ;
			skos:prefLabel ?l .
		?s2 a tko:Topic ;
			tko:head ?s .
		filter (?s != $topic)
} order by ?l
""")
	normalize(l)
	}
	
	def qTopicLinks(topic) {
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s ?l {
		?s0 a tko:Topic ;
			tko:head $topic ;
			skos:memberList/rdf:rest*/rdf:first/tko:head ?s .
		?s skos:prefLabel ?l
}""")
	normalize(l)
	}
	
	def qLinkStr0(lt) {
		def s= ""
		lt.each{
			def l = ju.queryListMap1(model, prefixes, """
select distinct ?s {
		?s a tko:Topic ;
			tko:head $it .
}""")
			s += "${subPrefix(l[0].s)} "
		}
		s
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
	
	def qLinkTopic(topic) {
		def s= ""
		def l = ju.queryListMap1(model, prefixes, """
select distinct ?s {
		?s a tko:Topic ;
			tko:head $topic .
}""")
		"${subPrefix(l[0].s)}"
	}
	
	def execTopicLinks0(topic, links) {
		def ls = qLinkStr(links)
		ju.queryExecUpdate(model,prefixes, """
delete {?s skos:memberList ?ml }
#insert {?s skos:memberList (${ls}) }
where {
		?s tko:head $topic .
}
""")
	def s = qLinkTopic(topic)
	def ttl = "$prefixes \n$s skos:memberList  ($ls)."
	def mdl = ju.saveStringModel(ttl, "ttl")
	model.add mdl
	}
	
	def execTopicLinks(topic, links) {
		def ls = qLinkStr(links)
		
		def js = ju.saveModelString(model,"JSONLD")
		def jc = new JsonSlurper().parseText(js)
		
		def mj = jc["@graph"].find{
			it.head == topic
		}
		mj.memberList["@list"] = ls

		def js2 = new JsonOutput().toJson(jc)
		model = ju.saveStringModel(js2, "JSONLD")
	}
	
	def execTopicLinks2(topic, ls) {
		//def ls = qLinkStr(links)
		
		def js = ju.saveModelString(model,"JSONLD")
		def jc = new JsonSlurper().parseText(js)
		
		def mj = jc["@graph"].find{
			it.head == topic
		}
		mj.memberList["@list"] = ls

		def js2 = new JsonOutput().toJson(jc)
		model = ju.saveStringModel(js2, "JSONLD")
	}
	

	
	def add(topic, links) {
		def l = []
		links.each{
			l += it.s
		}
		def ls = qLinkStr(l)
		def top = qLinkTopic(topic)
		ls += top
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
	

	
	def handleQueryParams(m) {
		
		m.selectSchemeOpts = qConceptScheme()
		m.selectTopicOpts = qTopics()
		m.selectSubTopicOpts = qSubTopics(m.selTopic ? m.selTopic : "<_:nil>")
		m.linkTopics = qTopicLinks(m.selTopic ? m.selTopic : m.selectTopicOpts[0].s)
		m.selectConcepts = qConcepts()
		
		
		if (m.isEmpty()) {
			println "new Authoring"
		}
		else {
			
			
			switch(m.action) {
				
				case 'moveUp': // linkSubTopic
				if (m.linkSubTopic
					&& m.linkSubTopic != m.linkTopics[0].s) {
					
					def l = moveUp(m.linkSubTopic, m.linkTopics)
					execTopicLinks(m.selTopic, l)
					
					m.linkTopics = qTopicLinks(m.selTopic ? m.selTopic : m.selectTopicOpts[0].s)
				}
				
				break
				case 'moveDown': 
				if (m.linkSubTopic
					&& m.linkSubTopic != m.linkTopics[m.linkTopics.size()-1].s) {
					
					def l = moveDown(m.linkSubTopic, m.linkTopics)
					execTopicLinks(m.selTopic, l)
					
					m.linkTopics = qTopicLinks(m.selTopic ? m.selTopic : m.selectTopicOpts[0].s)
				}

				break
				case 'removeSel': 
				if (m.linkSubTopic) {
					
					def l = remove(m.linkSubTopic, m.linkTopics)
					execTopicLinks(m.selTopic, l)
					
					m.linkTopics = qTopicLinks(m.selTopic ? m.selTopic : m.selectTopicOpts[0].s)
				}

				break
				case 'clearList': 
					
				def l = []
				execTopicLinks(m.selTopic, l)
				
				m.linkTopics = qTopicLinks(m.selTopic ? m.selTopic : m.selectTopicOpts[0].s)

				break
				case 'addSubTopic':
				if (m.selSubTopic) {
					
					def l = add(m.selSubTopic, m.linkTopics)
					execTopicLinks2(m.selTopic, l)
					
					m.linkTopics = qTopicLinks(m.selTopic ? m.selTopic : m.selectTopicOpts[0].s)
				}
					
				break
				
			}
			println ""
			m.each{k,v->
				println "$k=$v"
			}
		}
		println "model=${model.size()}"
		
		printHtml(m)
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
  width: 50px;
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
</script>

<h3>
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
Authoring</h3>

<table>
<tr>
<td>

<table>
<tr>
<td>
<form id="myForm" action="http://localhost:8082/author_page.entry" method="get">
<input type="hidden" id="action" name="action" value="">
<label for="selectScheme">Select Concept Scheme</label>
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
<label for="selTopic">Top Concept</label>
<h3><i><p style="text-indent: 20px;">Extinction Statement</i></h3>
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
	<button onclick="setAction('moveUpLevel')">Move up a level</button>
<br>
<br>
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
	<button onclick="setAction('saveTsh')">Save TSH</button>
<!--
<input type="button" onclick="resetFunction()" value="Reset form">
-->
<br>
</td></tr><tr><td>
  <br><label for="dir">Save dir:</label><br>
  <input type="text" id="dir" name="dir" size="25" value="/work/author/ttl">
<br>
  <label for="model">DB</label>
  <input type="checkbox" id="model" name="model" value="RDMS" ${m.model=="RDMS" ? "checked" : ""}>
</td></tr>
</table>
</form>
</td>
<td>

${tsh.printTopics(model)}

</td>
</tr>
</table>
<br>

version 0.5
</body></html>
"""
	html
	}
	
	
}
