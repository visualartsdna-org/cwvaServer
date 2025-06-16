package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes

class RelatedConcepts {
	
	def prefixes = Prefixes.forQuery

	def ju = new JenaUtilities()
//	def data
//	def conceptModel
	
	RelatedConcepts(){
	}

	Model getConceptModel() {
		cwva.Server.getInstance().dbm.vocab
	}

def qConcept(type) {
		def m = getConceptModel() 
		def ms = [:]
		def l = ju.queryListMap1(m, prefixes, """
select distinct ?s ?label ?alt {
		?s a skos:Concept ;
			rdfs:label ?label ;
			skos:broader ?cp .
		optional {
			?s 	schema:brand/skos:altLabel ?alt .
			}
		filter (?cp = $type)
} order by ?s
""")
		l.each{
			def s = it.s
			.replaceAll(/[<>]/,"")
			.replaceAll("http://visualartsdna.org/thesaurus/","the:")
			ms[s] = it.label
			if (type == "the:WatercolorPaint")
				ms[s] += it.alt ? ", ${it.alt}" :""
		}
		ms
	}
	

	
	def handleQueryParams(m) {
		def l=[]
		def l2=[]
		def reset = false
		m.each{k,v->
			if (k.startsWith("the:")
				&& v == "on")
				l += k
			else if (k.startsWith("reset")
				&& v == "Reset")
				reset = true
			else if (k == "relateds") {
				
				def l0=v.split(/[=\]]/)
				def l3=l0[1].split(",")
				l3.each{ l2 += it.trim()}
			}
		}
		if (reset) process([],[])
		else process(l,l2)
	}

	def process() {
		process([],[])
	}
		
	def process(lr,lagain) {
		
		def selected = "[related="
		int i=0
		def m = [:] 
		lagain.each{if(it) m[it]=null} //build a set
		lr.each{if(it) m[it]=null} //build a set
		m.each{k,v->
			if (i++) selected += ", "
			selected += k
		}
		selected += "]"
		
		
		def html = """
<html>
<!--
Submit prints result for those checked to 
copy and paste in keep notes, e.g,
[related=the:CadmiumYellowDD, the:IndigoBlueLCS]
-->
<head>
<style>
#checkboxes label {
  float: left;
}
#checkboxes ul {
  margin: 0;
  list-style: none;
  float: left;
}
</style>
</head>
<body>
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<br>
<h3>Watercolor Concepts</h3>
Select related paints and techniques to include in the note for the work.

<button onclick="myFunction2()">Copy</button><br/>
Place "relateds" string in textarea to auto-check boxes.
<form id="myForm" action="/related.entry" method="get">
<textarea id="related" name="relateds"  rows="4" cols="60">
$selected
</textarea>
<!--<input type="text" id="related" name="relateds" size="120" value="$selected">-->

	 <input type = "submit" name = "submit" value = "Submit" />
	 <input type = "submit" name = "reset" value = "Reset" />

<br>
<br>
<table><tr>
"""
		
		def html1 = ""
		//
		for (type in [
			"the:WatercolorPaint", 
			"the:watercolorTechnique", 
			"the:watercolorTextureTechnique",
			"the:watercolorMaterial",
			"the:brushingPaint"
			]) {
		def mc = qConcept(type)
		html1 += """
<td>
<div id="checkboxes">
  <ul>
"""

		mc.each{k,v->
			
		if (lr.contains(k)
			|| lagain.contains(k))
			html1 += "<li><input type=\"checkbox\" id=\"$k\" name=\"$k\" checked>$v</li>"
		else
			html1 += "<li><input type=\"checkbox\" id=\"$k\" name=\"$k\" >$v</li>"
		
		}
		html1 += """
  </ul>
</div>
</td>
"""
		
		}

		
		def html2="""
</tr></table>
<br>
V 2.0
</form>

<script>
function myFunction() {
  document.getElementById("myForm").reset();
}

function myFunction2() {
  // Get the text field
  var copyText = document.getElementById("related");

  // Select the text field
  copyText.select();
  copyText.setSelectionRange(0, 99999); // For mobile devices

   // Copy the text inside the text field
//  navigator.clipboard.writeText(copyText.value);
  document.execCommand('copy');
//  window.getSelection().removeAllRanges();

  // Alert the copied text
 // alert("Copied the text: " + copyText.value);
}
</script>


</body>
</html>
"""
		html + html1 + html2
	}
	
	
		
}
