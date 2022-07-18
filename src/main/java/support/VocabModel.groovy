package support

import static org.junit.jupiter.api.Assertions.*

import java.nio.charset.StandardCharsets
import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import rdf.JenaUtilities

class VocabModel {
	def prefixes = """
prefix xs: <http://www.w3.org/2001/XMLSchema#> 
prefix skos: <http://www.w3.org/2004/02/skos/core#> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
prefix voc:	<http://visualartsdna.org/voc#> 
"""

	def ju = new JenaUtilities()
	def saveModel = "/temp/junk/vocab.ttl"

	VocabModel(){
		def m = ju.loadFiles("/temp/git/cwva/ttl/data/vocab")
		ju.saveModelFile(m, saveModel, "TTL")
	}
	def getModel() {
		ju.loadFiles(saveModel)
		
	}
	
	def initial(term) {
		getInstance(term)
		
	}

	def saveInstance(term,instance) {
		
		def m = ju.loadFiles(saveModel)
		ju.queryExecUpdate(m,prefixes, """

INSERT Data{ 
	$instance
 }
""")
		ju.saveModelFile(m, saveModel, "TTL")

	}
	
	def updateInstance(term,instance) {
		
		def m = ju.loadFiles(saveModel)
		ju.queryExecUpdate(m,prefixes, """

DELETE { $term ?p ?o }
INSERT { 
	$instance
 }
WHERE
  { $term ?p ?o
  } 
""")
		ju.saveModelFile(m, saveModel, "TTL")

	}
	
	def getInstance(work) {
		def concept = qConcept()
		def broader = qTerms("broader",work)
		def inScheme = qTerms("inScheme",work)
		def narrower = qTerms3("broader",work)
		def related = qTerms("related",work)
		def member = qTerms2("member",work)
		def instance = qTerm(work)
		def s = ""
		instance.eachLine{
			if (!it.startsWith("@"))
				s += "$it\n"
		}
		getHtml(concept,broader,inScheme,narrower,related,member,work,s)

	}
	
	def getUri(instance) {
		def n = instance.indexOf(" ")
		def s = instance.substring(0,n)
		s.trim()
	}
	
	def handleQueryParams(m) {
		def html = ""
		
		def concept = m["concept"]
		def broader = m["broader"]?:""
		def inScheme = m["inScheme"]?:""
		def narrower = m["narrower"]?:""
		def related = m["related"]?:""
		def member = m["member"]?:""
		def term = m["term"]
		def instance = m["instance"]
		if (m.containsKey("gotoSave")) {
			updateInstance(term,instance)
			term = getUri(instance)
			html = getInstance(term)
		}
		else if (m.containsKey("gotoNew")) {
			term = "voc:newTerm"
			broader = "voc:visualArtTerm"
			inScheme = "voc:digitalArtTerms"
			instance="""
$term  a            skos:Concept ;
        skos:broader     voc:visualArtTerm ;
        skos:definition  "Definition." ;
        skos:inScheme    voc:digitalArtTerms ;
        skos:notation    "${UUID.randomUUID()}" ;
        skos:prefLabel   "newTerm" .
"""
			saveInstance(term,instance)
			html = getHtml(concept,broader,inScheme,narrower,related,member,term,instance)
		}
		else {
		def s = m.findAll{k,v->
			k in [
				"gotoConcept",
				"gotoBroader",
				"gotoNarrower",
				"gotoInScheme",
				"gotoRelated",
				"gotoMember"
				] && v && v.trim() != ""
		}.each{k,work -> 

//			getInstance(work)
			concept = qConcept()
			broader = qTerms("broader",work)
			inScheme = qTerms("inScheme",work)
			narrower = qTerms3("broader",work)
			related = qTerms("related",work)
			member = qTerms2("member",work)
			instance = qTerm(work)
			def s = ""
			instance.eachLine{
				if (!it.startsWith("@"))
					s += "$it\n"
			}
			html = getHtml(concept,broader,inScheme,narrower,related,member,work,s)
		}
		}
		html
	}
	
	
	def qConcept() {
		def m = getModel()
		def ls = ""
		def l = ju.queryListMap4(m, prefixes, """
select distinct ?s {
		?s a skos:Concept 
} order by ?s
""")
		l.each{
			def s = it.s
			ls += s
			.replaceAll(/[<>]/,"")
			.replaceAll("http://visualartsdna.org/voc#","voc:")
			ls += "\n"
		}
		ls
	}
	
	def qTerm(work) {
		def m = getModel()

		def model = ju.queryDescribe(m, prefixes, """
describe $work 
""")
		
		def ttl = ju.saveModelString(model,"TTL")
		ttl
	}
	
	def qTerms(term,work) {
		def m = getModel()
		def ls = ""
		def query = """
select distinct ?s {
		$work skos:$term ?s 
} order by ?s
"""

		def l = ju.queryListMap4(m, prefixes, query)
		def i=0
		l.each{
			if (i++>0) ls += ","
			def s = it.s
			ls += s
			.replaceAll(/[<>]/,"")
			.replaceAll("http://visualartsdna.org/voc#","voc:")
			
		}
		ls
	}
	
	def qTerms2(term,work) {
		def m = getModel()
		def ls = ""
		def query = """
select distinct ?s {
		?s skos:$term  $work
} order by ?s
"""

		def l = ju.queryListMap4(m, prefixes, query)
		l.each{
			def s = it.s
			ls += s
			.replaceAll(/[<>]/,"")
			.replaceAll("http://visualartsdna.org/voc#","voc:")
			
		}
		ls
	}
	
	def qTerms3(term,work) {
		def m = getModel()
		def ls = ""
		def query = """
select distinct ?s {
		?s skos:$term  $work
} order by ?s
"""

		def l = ju.queryListMap4(m, prefixes, query)
		l.each{
			def s = it.s
			ls += s
			.replaceAll(/[<>]/,"")
			.replaceAll("http://visualartsdna.org/voc#","voc:")
			ls += "\n"
		}
		ls
	}
	
	def parse(query) {
		
		//println "$path\n$query"
		def al = query.split(/&/)
		def m=[:]
		al.each {
			def av = it.split(/=/)
			if (av.size()==2)
				m[av[0]]=java.net.URLDecoder.decode(av[1], StandardCharsets.UTF_8.name())
			 }
//		m.each {k,v->
//			println "$k = $v"
//		}
		
		m

	}
	
	def getHtml(concept,broader,inScheme,narrower,related,member,term,instance) {
		"""

<!-- saved from url=(0031)http://localhost:8082/rdf/entry -->
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style>
table, th, td {
  #border: 1px solid black;
  border-collapse: collapse;
}
th, td {
  padding-top: 10px;
  padding-bottom: 10px;
  padding-left: 10px;
  padding-right: 10px;
}
</style>
</head>
<body style="margin:100;padding:0">
<h2>
VisualArtsDNA Vocabulary Entry (skos)
</h2>
<form id="skosForm" action="http://localhost:8082/vocab.entry" method="get">
<table>
<tr>
<td>
	<button onclick="changeConceptGoto()">Goto</button>
  <label for="concept">Concept</label><br/>
<textarea rows="35" cols="25" id="concept" name="concept"  readonly="">${concept}
</textarea>
</td>
<td>
<table>
<tr>
<td>

<table>
<tr>
</tr>

<tr>
<td>
	<button onclick="changeBroaderGoto()">Goto</button>
	<label for="broader">broader</label><br>
	<input type="text" id="broader" name="broader" size="40" value="${broader}" readonly="">
</td>
<td>
	<button onclick="changeInSchemeGoto()">Goto</button>
	<label for="inScheme">inScheme</label><br>
	<input type="text" id="inScheme" name="inScheme" size="40" value="${inScheme}" readonly="">
</td>
</tr>
<tr>
<td>
	<label for="term">term</label><br>
	<input type="text" id="term" name="term" size="40" value="${term}" readonly="">
</td>
<td>
	<button onclick="changeRelatedGoto()">Goto</button>
	<label for="related">related</label><br>
	<input type="text" id="related" name="related" size="40" value="${related}" readonly="">
</td>
</tr>
<tr>
<td>
	<button onclick="changeNarrowerGoto()">Goto</button>
	<label for="narrower">narrower</label><br>
<textarea rows="7" cols="40" id="narrower" name="narrower"  readonly="">$narrower</textarea>
</td>
<td>
	<button onclick="changeMemberGoto()">Goto</button>
	<label for="member">member</label><br>
	<input type="text" id="member" name="member" size="40" value="${member}" readonly="">
</td>
</tr>
</table>
</td>
</tr>
</table>

<table>
<tr>
<td>
         Instance : <br>
         <textarea rows="10" cols="80" id="instance" name="instance">$instance</textarea>
</td>
</tr>
<tr>
<td>
<input type="hidden" id="changeNew" name="gotoNew" value="">
<input type="hidden" id="changeSave" name="gotoSave" value="">
<input type="hidden" id="changeConcept" name="gotoConcept" value="">
<input type="hidden" id="changeBroader" name="gotoBroader" value="">
<input type="hidden" id="changeNarrower" name="gotoNarrower" value="">
<input type="hidden" id="changeInScheme" name="gotoInScheme" value="">
<input type="hidden" id="changeRelated" name="gotoRelated" value="">
<input type="hidden" id="changeMember" name="gotoMember" value="">
<!--<button onclick="alertme()">Load</button>
<button onclick="display()">Display value</button>
<button onclick="change()">Change</button>
	 <input type="button" name="load" value="Load"> 
	 <input type="button" name="save" value="Save">
	 <input type="submit" name="submit" value="Submit">-->
	<button onclick="changeSaveGoto()">Save</button>
	<button onclick="changeNewGoto()">New</button>
	<input type="button" onclick="resetFunction()" value="Reset">
</td>
</tr>
</table>

</td>
</tr>
</table>
<br><br>
  Configuration:<br>
  <br><label for="dir">Save TTL file:</label>
  <input type="text" id="dir" name="dir" size="40" value="$saveModel"> 
</form>


<script>
function resetFunction() {
  document.getElementById("skosForm").reset();
}
function changeSaveGoto() {
var path = document.getElementById("dir").value;  
document.getElementById("changeSave").value=path;
}
function changeNewGoto() {
document.getElementById("changeNew").value="new";
}
function changeConceptGoto() {
var textarea = document.getElementById("concept");  
var selection = (textarea.value).substring(textarea.selectionStart,textarea.selectionEnd);
  var x = document.getElementById("changeConcept").value=selection;
}
function changeBroaderGoto() {
var textarea = document.getElementById("broader");  
var selection = (textarea.value).substring(textarea.selectionStart,textarea.selectionEnd);
  var x = document.getElementById("changeBroader").value=selection;
}
function changeNarrowerGoto() {
var textarea = document.getElementById("narrower");  
var selection = (textarea.value).substring(textarea.selectionStart,textarea.selectionEnd);
  var x = document.getElementById("changeNarrower").value=selection;
}
function changeInSchemeGoto() {
var textarea = document.getElementById("inScheme");  
var selection = (textarea.value).substring(textarea.selectionStart,textarea.selectionEnd);
  var x = document.getElementById("changeInScheme").value=selection;
}
function changeRelatedGoto() {
var textarea = document.getElementById("related");  
var selection = (textarea.value).substring(textarea.selectionStart,textarea.selectionEnd);
  var x = document.getElementById("changeRelated").value=selection;
}
function changeMemberGoto() {
var textarea = document.getElementById("member");  
var selection = (textarea.value).substring(textarea.selectionStart,textarea.selectionEnd);
  var x = document.getElementById("changeMember").value=selection;
}
</script>

<br/>
<hr/>
Select one concept in a window with a "Goto," click the Goto.  New instance appears for editing.
Save edited instance to server.  New creates a template instance.
<br/>
<br/>
version 1.0

</body></html>
"""
	}
	

}
