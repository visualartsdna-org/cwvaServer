package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes

class EntityEntry {
	
	static def dir="/stage/metadata/tags"
	def prefixes = Prefixes.forQuery

	def ju = new JenaUtilities()
	
	EntityEntry(){
	}
	
	def validate(m) {
			assert m.type				, "no m.type"
			assert m.label				, "no m.label"
			assert m.media				, "no m.media"
			assert m.definition			, "no m.definition"
			assert m.recordedDateTime	, "no m.recordedDateTime"
			
	}
	
	def handleQueryParams(m) {
		if (m.isEmpty()) {
			m.guid = new Guid().get()
			def date = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
			m.recordedDateTime = date
			printHtml m
		}
		else {
			
			
		// collect media
			m.media
			int i=0
			for (int j=1;j<=15;j++) {
				def k = "media$j"
				if (m[k]) {
					if (!m.media) m.media = ""
					if (i++) m.media += ","
					m.media += "\"${m[k]}\""
				}
			}
			
			validate(m)
		//  support format <http://visualartsdna.org/images/NearElSoplador.jpg>
			def il = m.image.split(",")
			def is = ""
			def j=0
			il.each{
				if (j++>0) is += ","
				is += "<http://visualartsdna.org/images/${it.trim()}>"
			}
		def ttl = """
${rdf.Prefixes.forFile}
work:${m.guid}
		a the:${m.type} ;
		rdfs:label "${m.label}" ;
		the:media	${m.media} ;
		skos:definition  \"\"\"${m.definition}\"\"\" ;
${m.keywords?"":"#"}		schema:keywords "${m.keywords}" ;
${m.document?"":"#"}		the:document <${m.document}> ;
${m.primarySite?"":"#"}		schema:sameAs <${m.primarySite}> ;
${m.wikipedia?"":"#"}		the:wikipedia	<${m.wikipedia}> ;
${m.dbpedia?"":"#"}		the:dbpedia	<${m.dbpedia}> ;
${m.image?"":"#"}		schema:image	$is ;
${m.tag?"":"#"}		the:tag	${m.tag} ;
		schema:datePublished "${m.recordedDateTime}"^^xs:date ;
		skos:inScheme	the:entities ;
		.
"""
		new File("${m.dir}/${m.guid}.ttl").text = ttl
		ttl
		}
	}
		
	def printHtml(m) {
				
		 """

<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body style="margin:100;padding:0">
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<h2>
Art Entity work:${m.guid}</h2>
<form id="myForm" action="/entity_page.entry" method="get">
 <input type="hidden" id="guid" name="guid" value="${m.guid}">
<table><tbody><tr><td>
  <label for="label">Label:</label>
  <input type="text" id="label" name="label" size="40" value=""> 
</td></tr><tr><td><br>
Type:
</td></tr><tr><td>
  <input type="radio" id="type1" name="type" value="Artist">
  <label for="type1">Artist</label>
  <input type="radio" id="type2" name="type" value="Work">
  <label for="type2">Work</label>
  <input type="radio" id="type2" name="type" value="Study">
  <label for="type2">Study</label>
  <input type="radio" id="type2" name="type" value="Topic">
  <label for="type2">Topic</label>
  <input type="radio" id="type2" name="type" value="Organization">
  <label for="type2">Organization</label>
  <input type="radio" id="type2" name="type" value="Concept">
  <label for="type2">Concept</label>
  <input type="radio" id="type2" name="type" value="Entity">
  <label for="type2">Entity</label>
</td></tr><tr><td><br>
 Media:
<table><tr><td>
  <input type="checkbox" id="media1" name="media1" value="Watercolor">
  <label for="media1">Watercolor</label><br>
  <input type="checkbox" id="media2" name="media2" value="Drawing">
  <label for="media2">Drawing</label><br>
  <input type="checkbox" id="media3" name="media3" value="MixedMedia">
  <label for="media3">MixedMedia</label><br>
</td><td>
  <input type="checkbox" id="media4" name="media4" value="EggTempera">
  <label for="media4">EggTempera</label><br>
  <input type="checkbox" id="media5" name="media5" value="Oil">
  <label for="media5">Oil</label><br>
  <input type="checkbox" id="media6" name="media6" value="Acrylic">
  <label for="media6">Acrylic</label><br>
</td><td>
  <input type="checkbox" id="media7" name="media7" value="Sculpture">
  <label for="media7">Sculpture</label><br>
  <input type="checkbox" id="media8" name="media8" value="PrintMaking">
  <label for="media8">PrintMaking</label><br>
  <input type="checkbox" id="media9" name="media9" value="Digital">
  <label for="media9">Digital</label><br>
</td><td>
  <input type="checkbox" id="media10" name="media10" value="Photography">
  <label for="media10">Photography</label><br>
  <input type="checkbox" id="media11" name="media11" value="Architecture">
  <label for="media11">Architecture</label><br>
  <input type="checkbox" id="media12" name="media12" value="Decorative">
  <label for="media12">Decorative</label><br>
</td><td>
  <input type="checkbox" id="media13" name="media13" value="Historical">
  <label for="media13">Historical</label><br>
  <input type="checkbox" id="media14" name="media14" value="Other">
  <label for="media14">Other</label><br>
  <input type="checkbox" id="media15" name="media15" value="None">
  <label for="media15">None</label><br>
</td></tr></table>
</td></tr><tr><td><br>
         Definition : <br>
         <textarea rows="5" cols="70" id="definition" name="definition"></textarea>
</td></tr><tr><td>
<table cellspacing="1" cellpadding="5"><tr><td align="right">
  <label for="keywords">Keywords:</label></td><td>
  <input type="text" id="keywords" name="keywords" size="60" value=""> 
</td></tr><tr><td align="right">
  <label for="document">Document:</label></td><td>
  <input type="text" id="document" name="document" size="60" value=""> 
</td></tr><tr><td align="right">
  <label for="primarySite">PrimarySite:</label></td><td>
  <input type="text" id="primarySite" name="primarySite" size="60" value=""> 
</td></tr><tr><td align="right">
  <label for="wikipedia">Wikipedia:</label></td><td>
  <input type="text" id="wikipedia" name="wikipedia" size="60" value=""> 
</td></tr><tr><td align="right">
  <label for="dbpedia">Dbpedia:</label></td><td>
  <input type="text" id="dbpedia" name="dbpedia" size="60" value=""> 
</td></tr><tr><td align="right">
  <label for="image">Image Files:</label><br>filenames<br>w/commas
</td><td>
  <!--<input type="text" id="image" name="image" size="60" value="">-->
 	<textarea rows="3" cols="60" id="image" name="image"></textarea>
</td></tr><tr><td align="right">
  <label for="tag">Tags:</label><br>prefix URIs<br>w/commas
</td><td>
  <!--<input type="text" id="tag" name="tag" size="60" value="">-->
 	<textarea rows="3" cols="60" id="tag" name="tag"></textarea>
</td></tr><tr><td align="right">
  <label for="recordedDateTime">Recorded:</label></td><td>
  <input type="text" id="recordedDateTime" name="recordedDateTime" size="20" value="${m.recordedDateTime}">
</td></tr></table>
</td></tr></tbody></table>
<br>
	 <input type="hidden" name="pagename" value="10">
	 <input type="submit" name="submit" value="Submit"></td><td>
	<input type="button" onclick="myFunction()" value="Reset form">
<br><br>
  Configuration:<br>
  <br><label for="dir">TTL &amp; QRC temp dir:</label>
  <input type="text" id="dir" name="dir" size="40" value="/stage/data"> 
<!--
<table><tr><td>
	 <input type = "hidden" name = "pagename" value = "10" />
	 <input type = "submit" name = "submit" value = "Submit" />
	<input type="button" onclick="myFunction()" value="Reset form">
</td><td style="text-align:right">Configuration parameters</td><td>
  <br><label for="dir">TTL & QRC temp dir:</label><br>
  <input type="text" id="dir" name="dir" size="40" value="/stage/data"> 
</td></tr></table>-->
</form>

<script>
function myFunction() {
  document.getElementById("myForm").reset();
}
</script>

</body></html>
"""
	}
	
	
}
