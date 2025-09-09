package support

import static org.junit.Assert.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import rdf.JenaUtils

import org.junit.Test

class HtmlForm2DigitalTtl {

	def testPath = "ttl"
	@Test
	public void test() {
		def guid="ce9dfb4a-afdf-497a-a12f-c22f736df3f6"
		def s = new HtmlForm2DigitalTtl().printHtml(guid)
		println s
	}
	

	def artistSite = "https://www.rspates.art/"
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
VADNA RDF Digital Entry
</h2>
<h3>About: work:$guid</h3>
<form id="myForm" action="/rdf_digital.entry" method="get">
<table><tr><td>
  <br><label for="id">ID:</label><br>
  <input type="text" id="id" name="id"  size="$inSize" value="work:$guid">
</td><td>
  <br><label for="id">Type:</label><br>

  <input type="radio" id="type2" name="type" value="PaintApplicationImage">
  <label for="type2">PaintApplicationImage</label><br>
  <input type="radio" id="type1" name="type" value="LindenMayerSystemImage">
  <label for="type1">LindenMayerSystemImage</label><br>
  <input type="radio" id="type0" name="type" value="BlenderComposition">
  <label for="type0">BlenderComposition</label><br>
</td></tr><tr><td>
jpeg: 
         <input type = "file" name = "fileupload" id = "file" accept = "image/*" />
<br>
<br>
gLTF 2.0 (.glb):
         <input type = "file" name = "fileuploadglb" id = "file2" accept = ".glb" />
</td></tr><tr><td>
  <input type="radio" id="sig1" name="sig" value="rickspates.art">
  <label for="type1">rickspates.art</label><br>
  <input type="radio" id="sig2" name="sig" value="rspates.art" checked>
  <label for="type2">rspates.art</label><br>
  <input type="radio" id="sig3" name="sig" value="left">
  <label for="type2">left</label><br>
  <input type="radio" id="sig4" name="sig" value="right">
  <label for="type2">right</label><br>
</td><td>
  <label for="id">Digital media:</label><br>
  <input type="checkbox" id="media1" name="media1" value="Graphics">
  <label for="media1">Graphics</label><br>
  <input type="checkbox" id="media2" name="media2" value="Video">
  <label for="media2">Video</label><br>
  <input type="checkbox" id="media3" name="media3" value="Audio">
  <label for="media3">Audio</label><br>
  <input type="checkbox" id="media4" name="media4" value="Text">
  <label for="media4">Text</label><br>
</td></tr><tr><td>
  <br><label for="hasArtistProfile">hasArtistProfile:</label><br>
  <input type="text" id="hasArtistProfile" name="hasArtistProfile" size="$inSize" value="work:6d5746c7-3ffb-485f-a51c-0f652b9cd2d3"> 
</td><td>
  <br><label for="guid">guid:</label><br>
  <input type="text" id="guid" size="$inSize" name="guid" value="$guid">
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
  <br><label for="height">height (pixels):</label><br>
  <input type="text" id="height" name="height" size="$inSize" value=""> 
</td><td>
  <br><label for="fileFormat">fileFormat:</label><br>
  <select id="fileFormat" name="fileFormat"> 
<option value="JPEG" >JPEG</option>
<option value="TIFF" >TIFF</option>
<option value="PNG" >PNG</option>
<option value="GIF" >GIF</option>
<option value="OBJ" >OBJ</option>
  </select>
</td></tr><tr><td>
  <br><label for="width">width (pixels):</label><br>
  <input type="text" id="width" name="width" size="$inSize" value=""> 
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
