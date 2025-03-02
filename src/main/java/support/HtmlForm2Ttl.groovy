package support

import static org.junit.Assert.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import rdf.JenaUtils

import org.junit.Test

class HtmlForm2Ttl {

	def testPath = "C:/temp/generatedFiles"
	@Test
	public void test() {
		def guid="ce9dfb4a-afdf-497a-a12f-c22f736df3f6"
		def s = new HtmlForm2Ttl().printHtml(guid)
		println s
	}
	

	def artistSite = "https://rickspates.art"
	/*
	 * Some thoughts:
	 * Make hasPaper, hasPaperFinish dropdowns, ensure inclusive
	 * On submit, create qrcode in images folder
	 */
	def printHtml(guid) {
		def inSize=40
		def page = """
<html>
<head>
</head>
<body style="margin:100;padding:0">
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<br>
<h2>
VADNA RDF Entry
</h2>
<h3>About: work:$guid</h3>
<!--file:///C:/rdf/form.gro?fname=John&lname=Doe&vehicle1=Bike&vehicle2=Car&vehicle3=Boat&fav_language=JavaScript
-->
<form id="myForm" action="/rdf_page.entry" method="get">
<table><tr><td>
  <br><label for="id">ID:</label><br>
  <input type="text" id="id" name="id"  size="$inSize" value="work:$guid" readonly>
</td><td>
  <br><label for="id">Type:</label><br>

  <input type="radio" id="type1" name="type" value="Watercolor" checked>
  <label for="type1">Watercolor</label><br>
  <input type="radio" id="type2" name="type" value="Drawing">
  <label for="type2">Drawing</label><br>
  <input type="radio" id="type2" name="type" value="Egg Tempera">
  <label for="type2">Egg Tempera</label><br>
  <input type="radio" id="type2" name="type" value="Sculpture">
  <label for="type2">Sculpture</label><br>
</td></tr><tr><td>
         <input type = "file" name = "fileupload" id = "file" accept = "image/*" />
<!--  <br>
  <br><label for="id">Signature:</label><br>
  <input type="radio" id="sig1" name="sig" value="right" checked>
  <label for="type1">Right</label><br>
  <input type="radio" id="sig2" name="sig" value="left">
  <label for="type2">Left</label><br>
-->
</td><td>
  <label for="id">Media:</label><br>
  <input type="checkbox" id="media1" name="media1" value="Watercolor" checked>
  <label for="media1">Watercolor</label><br>
  <input type="checkbox" id="media2" name="media2" value="Drawing">
  <label for="media2">Drawing</label><br>
  <input type="checkbox" id="media3" name="media3" value="WatercolorPencil">
  <label for="media3">WatercolorPencil</label><br>
  <input type="checkbox" id="media4" name="media4" value="EggTempera">
  <label for="media4">EggTempera</label><br>
  <input type="checkbox" id="media4" name="media4" value="Marble">
  <label for="media4">Marble</label><br>
  <input type="checkbox" id="media4" name="media4" value="Limestone">
  <label for="media4">Limestone</label><br>
  <input type="checkbox" id="media4" name="media4" value="Steel">
  <label for="media4">Steel</label><br>
</td></tr><tr><td>
  <br><label for="hasArtistProfile">hasArtistProfile:</label><br>
  <input type="text" id="hasArtistProfile" name="hasArtistProfile" size="$inSize" value="work:ebab5e0c-cc32-4928-b326-1ddb4dd62c22"> 
</td><td>
  <br><label for="guid">guid:</label><br>
  <input type="text" id="guid" size="$inSize" name="guid" value="$guid" readonly>
</td></tr><tr><td>
  <br><label for="workOnSite">workOnSite:</label><br>
  <input type="text" id="workOnSite" name="workOnSite" size="$inSize" value="$artistSite"> 
</td><td>
  <br><label for="qrcode">qrcode:</label><br>
  <input type="text" id="qrcode" name="qrcode" size="68" value="http://visualartsdna.org/images/qrc_${guid}.jpg" readonly> 
</td></tr><tr><td>
  <br><label for="completedDateTime">completedDateTime:</label><br>
  <input type="text" id="completedDateTime" name="completedDateTime" size="$inSize" value="${getNow()}"> 
</td><td>
  <br><label for="recordedDateTime">recordedDateTime:</label><br>
  <input type="text" id="recordedDateTime" name="recordedDateTime" size="$inSize" value="${getNow()}"> 
</td></tr><tr><td>
  <br><label for="hasPaper">hasPaper:</label><br>
  <select id="hasPaper" name="hasPaper"> 
<option value="Fabriano Artistico Grana Fina" >Fabriano Artistico Grana Fina</option>
<option value="Canson Mix Media" >Canson Mix Media</option>
<option value="Strathmore Mixed Media" >Strathmore Mixed Media</option>
<option value="Strathmore Toned Gray" >Strathmore Toned Gray</option>
<option value="Strathmore Watercolor cold press, block" >Strathmore Watercolor cold press, block</option>
<option value="Strathmore Watercolor" >Strathmore Watercolor</option>
<option value="Strathmore heavyweight mixed media vellum finish" >Strathmore heavyweight mixed media vellum finish</option>
  </select>
</td><td>
  <br><label for="hasPaperFinish">hasPaperFinish:</label><br>
  <select id="hasPaperFinish" name="hasPaperFinish"> 
<option value="Cold press" >Cold press</option>
<option value="Hot press" >Hot press</option>
<option value="Smooth" >Smooth</option>
<option value="Vellum surface" >Vellum surface</option>
<option value="medium surface" >medium surface</option>
<option value="smooth" >smooth</option>
  </select>
</td></tr><tr><td>
  <br><label for="height">height:</label><br>
  <input type="text" id="height" name="height" size="$inSize" value=""> 
</td><td>
  <br><label for="hasPaperWeight">hasPaperWeight:</label><br>
  <select id="hasPaperWeight" name="hasPaperWeight"> 
<option value="300" >300</option>
<option value="140" >140</option>
<option value="350" >350</option>
<option value="80" >80</option>
<option value="90" >90</option>
<option value="98" >98</option>
  </select>
</td></tr><tr><td>
  <br><label for="width">width:</label><br>
  <input type="text" id="width" name="width" size="$inSize" value=""> 
</td><td>
  <br><label for="location">location:</label><br>
  <input type="text" id="location" name="location" size="$inSize" value=""> 
</td></tr><tr><td>
  <br><label for="label">label:</label><br>
  <input type="text" id="label" name="label" size="$inSize" value=""> 
</td><td>
  <br><label for="note">note:</label><br>
  <input type="text" id="note" name="note" size="$inSize" value=""> 
</td></tr></table>
<!--
<br><br>
         <select name = "dropdown">
            <option value = "Maths" selected>Maths</option>
            <option value = "Physics">Physics</option>
         </select>
-->
<br><br>
         Description : <br />
         <textarea rows = "5" cols = "80" id="description" name = "description"></textarea>
<br><br>
	 <input type = "hidden" name = "pagename" value = "10" />
	 <input type = "submit" name = "submit" value = "Submit" />
	<input type="button" onclick="myFunction()" value="Reset form">
<br><br>
  Configuration:<br>
  <br><label for="dir">TTL & QRC temp dir:</label>
  <input type="text" id="dir" name="dir" size="$inSize" value="${ParseRDF.dir}"> 
<!--
<table><tr><td>
	 <input type = "hidden" name = "pagename" value = "10" />
	 <input type = "submit" name = "submit" value = "Submit" />
	<input type="button" onclick="myFunction()" value="Reset form">
</td><td style="text-align:right">Configuration parameters</td><td>
  <br><label for="dir">TTL & QRC temp dir:</label><br>
  <input type="text" id="dir" name="dir" size="$inSize" value="${ParseRDF.dir}"> 
</td></tr></table>-->
</form>

<script>
function myFunction() {
  document.getElementById("myForm").reset();
}
</script>

</body>
</html>
"""
	}
	
	static def getNow() {
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
	}

}
