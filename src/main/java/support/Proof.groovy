package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes
import util.Guid
import groovy.io.FileType
import org.apache.commons.io.FileUtils

class Proof {
	
	static def prefixes = Prefixes.forQuery
	static def data = "c:/stage/data"
	static def base = "c:/stage/proof"
	
	def ju = new JenaUtilities()
	
	Proof(){
	}

	def handleQueryParams(m) {
		process(m)
	}
	
	def extractResults(m) {
		def rm=[:]
		rm.Transaction = m.blockchainTransactionLink
		rm.Timestamp = m.timestamp
		rm.DataHash = m.dataHash
		rm.RootHash = m.rootHash
		rm
	}
	
	def extractGuid(s) {
		// http://visualartsdna.org/work/12345
		s.substring("http://visualartsdna.org/work/".length())
	}
	
	def convertTS(ts) {
		// "10/2/2025, 8:27:04 PM"
//		def formatPattern = "MM/dd/yyyy, HH:mm:ss a"
		// "Feb 4, 2026, 07:12:03 PM"
		def formatPattern = "MMM dd, yyyy, HH:mm:ss a"

		// Parse the string into a Date object
		def newDate = new SimpleDateFormat(formatPattern).parse(ts)
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").format(newDate)
	}

	def process(m) {
		
		if (m.process) {
			def guid = extractGuid(m.selectIncipient)
			// load selected incipient
			// repopulate with result info
			// remove from /stage/proof
			// write to /stage/data
			def m2 = ju.loadFiles("$base/${guid}.ttl")
			
			// process result into ttl
			def rm = extractResults(m)
			def lm = ju.queryListMap1(m2,prefixes,"""
select ?a{
work:$guid vad:asset ?a
}
""")
			def asset = lm[0].a
			def hash = calculateSHA256FromURL("${cwva.Server.rehost(asset)}") 
			ju.queryExecUpdate(m2,prefixes, """
			insert data {
				<${m.selectIncipient}> vad:timestamp "${convertTS(rm.Timestamp)}"^^xs:dateTime .
				<${m.selectIncipient}> vad:transaction "${rm.Transaction}" .
				<${m.selectIncipient}> vad:rootHash "${rm.RootHash}" .
				<${m.selectIncipient}> vad:dataHash "${rm.DataHash}" .
				<${m.selectIncipient}> vad:sha256 "$hash" .
			}
""")
			ju.saveModelFile(m2, "$data/${guid}.ttl", "ttl")
			
			new File("$base/${guid}.ttl").delete()
		}
		
		def model = ju.loadFiles(base)
	
		def lm = ju.queryListMap1(model,prefixes, """
select ?s ?a {
		?s a vad:Proof ;
			vad:asset ?a .
}
""")

		def opts = ""
		lm.each{
			opts += """<option value="${it.s}" >${it.a}</option>"""
		}
		
		def html = """
<html>
<head>
</head>
<body>
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<br>
<h3>Proof</h3>
<br>
<br>
<b>
<label for="myForm">SELECT INCIPIENT (Carefully!)</label><br>
</b>
 <form id="myForm" action="/proof" method="get">
<select name="selectIncipient" id="selectIncipient">
$opts
</select>
<br>
<br>
For generated proof, drop asset into proof site, 
<a href="https://originstamp.com/solutions/timestamp/en">OriginStamp</a>
<br>
<br>
Copy result to results fields and hit process.<br/>


<br/>
<br/>
         Results<br/><br/>
<!--
<select name="certificate" size="1">
  <option selected>Bitcoin</option>
  <option>Eth</option>
  <option>Dogecoin</option>
</select>
-->
<table><tr><td>
		 <label for="dataHash">Data Hash:</label></td><td>
		 <input type="text" id="dataHash" name="dataHash" size="70" value=""></td></tr><tr><td>
		 <label for="timestamp">Timestamp:</label></td><td>
		 <input type="text" id="timestamp" name="timestamp" size="30" value=""></td></tr><tr><td>
		 <label for="blockchainTransactionLink">Transaction:</label></td><td>
		 <input type="text" id="blockchainTransactionLink" name="blockchainTransactionLink" size="70" value=""></td></tr><tr><td>
		 <label for="rootHash">Root Hash:</label></td><td>
		 <input type="text" id="rootHash" name="rootHash" size="70" value=""></td></tr>
</table>
<br/>

<input type = "submit" name="process" id="process" value="Process" />

</form>

</body>
</html>
"""
		html 
	}
	
	static def service = "https://originstamp.com/solutions/timestamp/en"
	static def blockchain = "Bitcoin"
	static def imagesUrl = "http://visualartsdna.org/images"
	
	static def createProof(workGuid,asset) {
		
		def guid = new Guid().get()
		def ttl = """
${rdf.Prefixes.forFile}

work:${guid}
	a vad:Proof ;
	rdfs:label "Proof" ;
	vad:assetOf work:$workGuid ;
	vad:asset <$imagesUrl/$asset> ;
	vad:created	"${getNow()}"^^xsd:dateTime ;
	vad:service <$service> ;
	vad:blockchain "$blockchain" ;
	.
"""
		new File("$base/${guid}.ttl").text = ttl
	}
	
	static def getNow() {
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
	}

	String calculateSha256(File file) {
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException("File not found: ${file.path}")
		}

		MessageDigest digest = MessageDigest.getInstance("SHA-256")
		file.eachByte(4096) { buffer, bytesRead ->
			digest.update(buffer, 0, bytesRead)
		}

		// Convert the resulting byte array to a hexadecimal string
		return digest.digest().encodeHex().toString()
	}

	/**
	 * Calculate SHA256 hash of a file from a URL
	 * @param urlString The URL of the file to hash
	 * @return The SHA256 hash as a hexadecimal string
	 */
	String calculateSHA256FromURL(String urlString) {
		try {
			// Create URL object and open connection
			def url = new URL(urlString)
			def connection = url.openConnection()
			
			// Create MessageDigest instance for SHA-256
			def digest = MessageDigest.getInstance("SHA-256")
			
			// Read the file in chunks and update digest
			connection.inputStream.withStream { stream ->
				def buffer = new byte[8192]
				int bytesRead
				while ((bytesRead = stream.read(buffer)) != -1) {
					digest.update(buffer, 0, bytesRead)
				}
			}
			
			// Get the hash bytes and convert to hex string
			def hashBytes = digest.digest()
			def hexString = hashBytes.collect { byte b ->
				String.format('%02x', b)
			}.join('')
			
			return hexString
			
		} catch (Exception e) {
			println "Error calculating SHA256: ${e.message}"
			throw e
		}
	}
}
