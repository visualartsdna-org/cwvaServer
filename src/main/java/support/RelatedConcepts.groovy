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
	def concepts
	def conceptModel
	
	RelatedConcepts(concepts){
		this.concepts = concepts
		conceptModel = ju.loadFiles("${concepts}")
	}

	def qConcept() {
		def m = conceptModel
		def ls = []
		def l = ju.queryListMap4(m, prefixes, """
select distinct ?s {
		?s a skos:Concept ;
			skos:broader ?cp .
		filter (?cp in (the:WatercolorPaint, the:watercolorTechnique, the:watercolorMaterial))
} order by ?s
""")
		l.each{
			def s = it.s
			ls += s
			.replaceAll(/[<>]/,"")
			.replaceAll("http://visualartsdna.org/thesaurus/","the:")
			//ls += "\n"
		}
		ls
	}
	

	
	def handleQueryParams(m) {
		def l=[]
		def reset = false
		m.each{k,v->
			if (k.startsWith("the:")
				&& v == "on")
				l += k
			if (k.startsWith("reset")
				&& v == "Reset")
				reset = true
		}
		if (reset) process([])
		else process(l)
	}

	def process() {
		process([])
	}
		
	def process(lr) {
		
		def selected = "[related="
		int i=0
		lr.each{
			if (i++) selected += ", "
			selected += it
		}
		selected += "]"
		
		def lc = qConcept()
		
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
Select paints to include as related to the note.
<br>
<form id="myForm" action="/related.entry" method="get">
<textarea id="related" name="relateds"  rows="4" cols="60">
$selected
</textarea>
<!--<input type="text" id="related" name="relateds" size="120" value="$selected">-->
<button onclick="myFunction2()">Copy</button>

<br>
<br>

<div id="checkboxes">
  <ul>
"""
		def html1 = ""
		lc.each{
			
		if (lr.contains (it))
			html1 += "<li><input type=\"checkbox\" id=\"$it\" name=\"$it\" checked>$it</li>"
		else
			html1 += "<li><input type=\"checkbox\" id=\"$it\" name=\"$it\" >$it</li>"
		
		}
		
		def html2="""
  </ul>
</div>
<br>
<br>
	 <input type = "submit" name = "submit" value = "Submit" />
	 <input type = "submit" name = "reset" value = "Reset" />
<br>
V 1.0
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
  navigator.clipboard.writeText(copyText.value);

  // Alert the copied text
  //alert("Copied the text: " + copyText.value);
}
</script>


</body>
</html>
"""
		html + html1 + html2
	}
	
	
		
	def parse(query) {
		
		//println "$path\n$query"
		def al = query.split(/&/)
		def m=[:]
		al.each {
			def av = it.split(/=/)
			av[0] = av[0].replaceAll("%3A",":")
			if (av.size()==2)
				m[av[0]]=java.net.URLDecoder.decode(av[1], StandardCharsets.UTF_8.name())
			 }
//		m.each {k,v->
//			println "$k = $v"
//		}
		
		m

	}
	


	@Test
	void test() {
		fail("Not yet implemented")
	}

}
