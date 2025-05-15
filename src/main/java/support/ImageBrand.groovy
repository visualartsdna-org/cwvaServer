package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes

class ImageBrand {
	
	static def dir="/stage/data"
	def prefixes = Prefixes.forQuery

	def ju = new JenaUtilities()
	
	ImageBrand(){
	}
	

	def handleQueryParams(m) {

		def s = ""
		m.each{k,v->
			s += "$k = $v\n"
		}
		
		if (m.guid) {
			m.guid = m.guid.trim()
		}
		if (m.guid ==~ /work:.*/) {
			def n = m.guid.indexOf(":") +1
			m.guid = m.guid.substring(n)
		}
		
		if (!m.label) {
			m.label = m.fileupload - ~/\.\w+$/
		}
		else m.label = m.label.trim()
			
		verify(m)
		
		def filename = ""
		if (m.sig in ["right","left"]) {
			
			filename = ImageMgt.makeStampedFile(m.guid,m.fileupload,m.label,m.dir,m.sig)
		}
		else {
			filename = ImageMgt2.makeStampedFile(m.guid,m.fileupload,m.label,m.dir,m.sig)
			
		}
		if (m.guid) printQRC(m.guid)
		
		
		"""Branded
$s"""
	}
	
	def verify(m) {
//		assert m.guid				, "no m.guid			 "
		assert m.label				, "no m.label"
		assert m.fileupload			, "file not present"
	}

	def printQRC(guid) {
		ImageMgt.qrcode(guid,dir)
	}


	def printHtml() {
				
		 """
<html>
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
<h2>Brand Image</h2>
<br/>
<form id="myForm" action="/imageBrand.entry" method="get">
<table >
<tr><td>
  <input type = "file" name = "fileupload" id = "file" accept = "image/*" />
  <br>
  <br><label for="id">Signature:</label><br>
  <input type="radio" id="sig1" name="sig" value="rickspates.art" checked>
  <label for="type1">rickspates.art</label><br>
  <input type="radio" id="sig2" name="sig" value="rspates.art">
  <label for="type2">rspates.art</label><br>
  <input type="radio" id="sig3" name="sig" value="left">
  <label for="type2">left</label><br>
  <input type="radio" id="sig4" name="sig" value="right">
  <label for="type2">right</label><br>
</td><td>
  <br><label for="guid">guid (if making QRcode):</label><br>
  <input type="text" id="guid" size="44" name="guid" value="">

<br>
<!--
-->
  <br><label for="label">label (when renaming file):</label><br>
  <input type="text" id="label" name="label" size="44" value=""> 

</td></tr>
</table>
<br><input type = "submit" name = "submit" value = "Brand" />
<br>
<br>
  Configuration:
  <br><label for="dir">TTL temp dir:</label>
  <input type="text" id="dir" name="dir" size="44" value="${dir}"> 
</form>

</body>
</html>
"""
	}
	
			
}
