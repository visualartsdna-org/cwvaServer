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
		
		verify(m)
		def filename = ImageMgt.makeStampedFile(m.guid,m.fileupload,m.label,m.dir,m.sig)

		
		"""Branded
$s"""
	}
	
	def verify(m) {
		assert m.guid				, "no m.guid			 "
		assert m.label				, "no m.label			 "
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
  <input type="radio" id="sig1" name="sig" value="right" checked>
  <label for="type1">Right</label><br>
  <input type="radio" id="sig2" name="sig" value="left">
  <label for="type2">Left</label><br>
</td><td>
  <br><label for="guid">guid:</label><br>
  <input type="text" id="guid" size="44" name="guid" value="">

<br>
  <br><label for="label">label:</label><br>
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
