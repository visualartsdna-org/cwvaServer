package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes

class Interpretation {
	
	static def dir="/stage/metadata/tags"
	def prefixes = Prefixes.forQuery

	def ju = new JenaUtilities()
	def dataModel
	
	Interpretation(data){
		dataModel = ju.loadFiles(data)
	}
	

	def handleQueryParams(m) {
		m.label = qLabel(m.tag)
		m.quid = makeGuid(m.tag,m.kind,m.label)
		def s = ""
		m.each{k,v->
			s += "$k = $v\n"
		}
		// create instance
		def dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
		def date = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
		
		def ttl = """
${rdf.Prefixes.forFile}
$m.quid
	    a                the:Interpretation ;
        tko:created      "$date"^^xsd:date ;
        tko:edited       "$dt"^^xsd:dateTime ;
        the:tag          ${m.tag} ;
		skos:related	the:${m.kind} ;
		skos:related 	the:${util.Text.camelCase(m.source)} ;
        rdfs:label       "${m.kind.trim()} of ${m.label}" ;
        skos:definition  \"\"\"${m.definition}\"\"\" ;
		.
"""
		new File("${m.dir}/${m.quid.substring(4)}.ttl").text = ttl
		ttl
	}

		
	def printHtml() {
				
		 """
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
<br/>
<h2>Interpretation</h2>
<br/>
<form id="myForm" action="/interpretation.entry" method="get">
<table >
<tr><td>
label
</td><td>
<select name="kind" id="kind">
<option value="Criticism ">Criticism</option>
<option value="Evaluation">Evaluation</option>
<option value="Assessment ">Assessment</option>
<option value="Influences">Influences</option>
<option value="Opinion ">Opinion</option>
<option value="Other ">Other</option>
<option value="Gallery Description">Gallery</option>
<option value="Technical Assessment">Technical</option>
</select>
</td></tr>
<tr><td>
source
</td><td>
<select name="source" id="source">
<option value="Google Gemini">Gemini</option>
<option value="Chat GPT">ChatGPT</option>
<option value="Other AI">OtherAI</option>
<option value="Person">Person</option>
</select>
</td></tr>
<tr><td>
definition
</td><td>
<textarea id="definition" name="definition" rows="4" cols="50">
</textarea>
</td></tr>
<tr><td>
tag
</td><td>
<input type="text" id="tag" name="tag" size="44" value="">
</td></tr>
<tr><td>
type
</td><td>
<input type="text" id="type" name="type" size="44" value="the:Interpretation">
</td></tr>
</table>
<br><input type = "submit" name = "submit" value = "Create" />
<br>
  Configuration:<br>
  <br><label for="dir">TTL temp dir:</label>
  <input type="text" id="dir" name="dir" size="44" value="${dir}"> 
</form>

</body>
</html>
"""
	}
	
	
	def qLabel(tag) {
		def m = ju.queryListMap2(dataModel, prefixes, """
select ?l {
		${tag} rdfs:label ?l .
}
""")
		m.l
	}
	

	
	def makeGuid(tag,kind,label) {
		label = util.Text.camelCase(label)
		def date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
		
		"the:${label}${kind.trim()}$date"
	}

	@Test
	void test() {
		def tag = "work:da4bccc6-dfd3-4062-ae5b-3756e4eed354"
		def kind = "Criticism"
		def s = makeGuid(tag,kind)
		println s
	}

}
