package support

import static org.junit.jupiter.api.Assertions.*

import java.nio.charset.StandardCharsets
import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import rdf.Prefixes
import rdf.util.BackupFiles
import rdf.util.Transaction

class ConceptModel {
	def prefixes = Prefixes.forQuery

	def ju = new JenaUtilities()
	def concepts
	def conceptModel
	Transaction tx
	
	ConceptModel(concepts){
		this.concepts = concepts
		conceptModel = ju.loadFiles("$concepts/vocabulary.ttl")
		tx = new Transaction(conceptModel,"$concepts/vocabulary.ttl")
	}
	
	def initial(term) {
		getInstance(term)
	}

	def saveInstance(term,instance) {
		
		ju.queryExecUpdate(conceptModel,prefixes, """

INSERT Data{ 
	$instance
 }
""")
		tx.save()

	}
	
	def updateInstance(term,instance) {
		
		ju.queryExecUpdate(conceptModel,prefixes, """

DELETE { $term ?p ?o }
INSERT { 
	$instance
 }
WHERE
  { $term ?p ?o
  } 
""")
		tx.save()

	}
	
	def updateInstance(term,prop,value) {
		
		ju.queryExecUpdate(conceptModel,prefixes, """

DELETE { $term ${prop} ?o }
INSERT { $term ${prop} \"\"\"$value\"\"\" }
WHERE
  { $term ${prop} ?o
  } 
""")
		tx.save()

	}
	
	def getInstance(work) {
		def concept = qConcept()
		def broader = qTerms("broader",work)
		def inScheme = qTerms("inScheme",work)
		def narrower = qTerms3("broader",work)
		def related = qTerms("related",work)
		def member = qTerms2("member",work)
		def instance = qTerm(work)
		def textMap = [:]
		textMap.definition = qText("definition",work)
		textMap.label = qText("rdfs","label",work)
		getHtml(concept,broader,inScheme,narrower,related,member,work,instance,textMap)

	}
	
	def getUri(instance) {
		def n = instance.indexOf(" ")
		def s = instance.substring(0,n)
		s.trim()
	}
	
	def handleQueryParamsText(m) {
		def html = ""
		def instance = m["instance"]
		
		def value = ""
		def prop = ""
		m.each{k,v->
			switch (k) {
				case "definition":
					prop = "skos:$k"
					value = v.trim()
					break;
				case "label":
					prop = "rdfs:$k"
					value = v.trim()
					break;
			}
		}
		if (m.submit == "Save") {
			def term = getUri(instance)
			updateInstance(term,prop,value)
			html = getInstance(term)
		}
		html
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
		def textMap = [:]
		textMap.definition = qText("definition",term)
		textMap.label = qText("rdfs","label",term)
		if (m.containsKey("gotoSave")) {
			updateInstance(term,instance)
			term = getUri(instance)
			html = getInstance(term)
		}
		else if (m.containsKey("gotoNew")) {
			term = "the:newTerm"
			broader = "the:visualArtTerm"
			inScheme = "the:digitalArtTerms"
			instance="""
$term  a            skos:Concept ;
        skos:broader     the:visualArtTerm ;
        skos:definition  "Definition." ;
        skos:inScheme    the:digitalArtTerms ;
        schema:identifier    "${UUID.randomUUID()}" ;
        rdfs:label   "newTerm" .
"""
			saveInstance(term,instance)
			html = getHtml(concept,broader,inScheme,narrower,related,member,term,instance,textMap)
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
		textMap.definition = qText("definition",work)
		textMap.label = qText("rdfs","label",work)
			def s = ""
			instance.eachLine{
				if (!it.startsWith("@"))
					s += "$it\n"
			}
			html = getHtml(concept,broader,inScheme,narrower,related,member,work,s,textMap)
		}
		}
		html
	}
	
	
	def qConcept() {
		def m = conceptModel
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
			.replaceAll("http://visualartsdna.org/thesaurus/","the:")
			ls += "\n"
		}
		ls
	}
	
	def qTerm(work) {
		def m = conceptModel

		def model = ju.queryDescribe(m, prefixes, """
describe $work 
""")
		
		def ttl = ju.saveModelString(model,"TTL")
		
		// remove prefixes
		def s = "" 
		ttl.eachLine{
			if (!it.startsWith("@"))
				s += "$it\n"
		}

		s
	}
	
	def qTerms(term,work) {
		qTerms("skos",term,work)
	}
	
	def qTerms(ns,term,work) {
		def m = conceptModel
		def ls = ""
		def query = """
select distinct ?s {
		$work $ns:$term ?s 
} order by ?s
"""

		def l = ju.queryListMap4(m, prefixes, query)
		def i=0
		l.each{
			if (i++>0) ls += ","
			def s = it.s
			ls += s
			.replaceAll(/[<>]/,"")
			.replaceAll("http://visualartsdna.org/thesaurus/","the:")
			
		}
		ls
	}
	
	def qText(term,work) {
		qText("skos",term,work)
	}
	
	def qText(ns,term,work) {
		def m = conceptModel
		def ls = ""
		def query = """
select distinct ?s {
		$work $ns:$term ?s 
} order by ?s
"""

		def l = ju.queryListMap2(m, prefixes, query)
		def i=0
		l.each{k,v->
			if (i++>0) ls += ". "
			ls += v
		}
		ls
	}
	
	def qTerms2(term,work) {
		def m = conceptModel
		def ls = ""
		def query = """
select distinct ?s {
		?s the:$term  $work
} order by ?s
"""

		def l = ju.queryListMap4(m, prefixes, query)
		l.each{
			def s = it.s
			ls += s
			.replaceAll(/[<>]/,"")
			.replaceAll("http://visualartsdna.org/thesaurus/","the:")
			
		}
		ls
	}
	
	def qTerms3(term,work) {
		def m = conceptModel
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
			.replaceAll("http://visualartsdna.org/thesaurus/","the:")
			ls += "\n"
		}
		ls
	}
	
	def getHtml(concept,broader,inScheme,narrower,related,member,term,instance,textMap) {
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
body {font-family: Arial, Helvetica, sans-serif;}

/* The Modal (background) */
.modal {
  display: none; /* Hidden by default */
  position: fixed; /* Stay in place */
  z-index: 1; /* Sit on top */
  left: 0;
  top: 0;
  width: 100%; /* Full width */
  height: 100%; /* Full height */
  overflow: auto; /* Enable scroll if needed */
  background-color: rgb(0,0,0); /* Fallback color */
  background-color: rgba(0,0,0,0.4); /* Black w/ opacity */
  -webkit-animation-name: fadeIn; /* Fade in the background */
  -webkit-animation-duration: 0.4s;
  animation-name: fadeIn;
  animation-duration: 0.4s
}

/* Modal Content */
.modal-content {
  position: fixed;
  bottom: 0;
  background-color: #fefefe;
  width: 100%;
  -webkit-animation-name: slideIn;
  -webkit-animation-duration: 0.4s;
  animation-name: slideIn;
  animation-duration: 0.4s
}

/* The Close Button */
.close {
  color: white;
  float: right;
  font-size: 28px;
  font-weight: bold;
}

.close:hover,
.close:focus {
  color: #000;
  text-decoration: none;
  cursor: pointer;
}

/* The Close Button */
.close2 {
  color: white;
  float: right;
  font-size: 28px;
  font-weight: bold;
}

.close2:hover,
.close2:focus {
  color: #000;
  text-decoration: none;
  cursor: pointer;
}

.modal-header {
  padding: 2px 16px;
  background-color: #808080;
  color: white;
}

.modal-body {padding: 2px 16px;}

.modal-footer {
  padding: 2px 16px;
  background-color: #808080;
  color: white;
}

/* Add Animation */
@-webkit-keyframes slideIn {
  from {bottom: -300px; opacity: 0} 
  to {bottom: 0; opacity: 1}
}

@keyframes slideIn {
  from {bottom: -300px; opacity: 0}
  to {bottom: 0; opacity: 1}
}

@-webkit-keyframes fadeIn {
  from {opacity: 0} 
  to {opacity: 1}
}

@keyframes fadeIn {
  from {opacity: 0} 
  to {opacity: 1}
}
</style>
</head>
<body style="margin:100;padding:0">
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<br>
<h2>
VisualArtsDNA Concept Entry
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
</form>

<!-- Trigger/Open The Modal -->
<center>Edit text: 
<button id="myBtn">definition</button>
<button id="myBtn2">label</button>
</center>
 <form id="myForm" action="http://localhost:8082/vocab.entry.text" method="get">
<!-- The Modal -->
<input type="hidden" id="instance" name="instance" value="${URLEncoder.encode(instance, "UTF-8")}">
<div id="myModal" class="modal">

  <!-- Modal content -->
 <div class="modal-content">
    <div class="modal-header">
      <span class="close">&times;</span>
      <h2>definition</h2>
    </div>
    <div class="modal-body">
      <p><textarea id="definition" name="definition" rows="4" cols="60">
${textMap.definition}
</textarea></p>
<p>
	 <input type = "submit" name = "submit" value = "Save" />
</p>
    </div>
    <div class="modal-footer">
      <h5>Save edited text or cancel</h5>
    </div>
  </div>
</div>
</form>

 <form id="myForm" action="http://localhost:8082/vocab.entry.text" method="get">
<!-- The Modal -->
<input type="hidden" id="instance" name="instance" value="${URLEncoder.encode(instance, "UTF-8")}">
<div id="myModal2" class="modal">

  <!-- Modal content -->
 <div class="modal-content">
    <div class="modal-header">
      <span class="close2">&times;</span>
      <h2>comment</h2>
    </div>
    <div class="modal-body">
      <p><textarea id="label" name="label" rows="2" cols="30">
${textMap.label}
</textarea></p>
<p>
	 <input type = "submit" name = "submit" value = "Save" />
</p>
    </div>
    <div class="modal-footer">
      <h5>Save edited text or cancel</h5>
    </div>
  </div>
</div>
</form>


<script>
function resetFunction() {
  document.getElementById("skosForm").reset();
}
function changeSaveGoto() {
document.getElementById("changeSave").value="save";
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
// Get the modal
var modal = document.getElementById("myModal");
var modal2 = document.getElementById("myModal2");

// Get the button that opens the modal
var btn = document.getElementById("myBtn");
var btn2 = document.getElementById("myBtn2");

// Get the <span> element that closes the modal
var span = document.getElementsByClassName("close")[0];
var span2 = document.getElementsByClassName("close2")[0];

// When the user clicks the button, open the modal 
btn.onclick = function() {
  modal.style.display = "block";
}

// When the user clicks on <span> (x), close the modal
span.onclick = function() {
  modal.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
  if (event.target == modal) {
    modal.style.display = "none";
  }
  if (event.target == modal2) {
    modal2.style.display = "none";
  }
}
// When the user clicks the button, open the modal 
btn2.onclick = function() {
  modal2.style.display = "block";
}

// When the user clicks on <span> (x), close the modal
span2.onclick = function() {
  modal2.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
//window.onclick = function(event) {
//  if (event.target == modal2) {
//    modal2.style.display = "none";
//  }
//}
</script>

<br/>
<hr/>
Select one concept in a window with a "Goto," click the Goto.  New instance appears for editing.
Save edited instance to server.  New creates a template instance.
<br/>
<br/>
version 2.0

</body></html>
"""
	}
	

}
