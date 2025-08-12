package support.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import java.nio.charset.CharacterCodingException
import java.nio.charset.CharsetDecoder
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

class FileLoader {

	@Test
	void test() {
		def fs = "C:/test/topics.ttl"
		def fs2 = "C:/test/copse1.json"
		//def fs = " "
//		println loadTtl(fs)
//		println loadJson(fs2)
		println loadAny(fs2)
	}
	
	@Test
	void test2() {
		def fs = "C:/test"
		//def fs = " "
//		println loadTtl(fs)
//		println loadJson(fs2)
		println assertTtl(fs)
	}
	
	static def loadAny(fs) {
		//detectInvalidCharsFile(fs)
		switch ((fs =~ /.*\.([a-z]+)$/)[0][1]) {
			case "json":
				loadJson(fs)
			break
			
			case "ttl":
				loadTtl(fs)
			break
		}
	}
	
	static def loadTtl(fs) {
		assert fs.trim() != "", "no file specified"
		try {
			def data = new JenaUtilities().loadFiles(fs)
				return "$fs, TTL model size=${data.size()}"
			} catch (org.apache.jena.riot.RiotException re) {
				return "$fs, $re"
			} catch (Exception e) {
				return "$fs, $e"
			}
	}
	
	static def assertTtl(fs) {
		assert fs.trim() != "", "no file specified"
		try {
			def data = new JenaUtilities().loadFiles(fs)
				return true
			} catch (org.apache.jena.atlas.RuntimeIOException rioe) {
				if ((""+rioe).contains("MalformedInputException"))
					throw new RuntimeException("$fs, MalformedInputException: +$rioe")
				else throw new RuntimeException("$fs, $rioe")
			} catch (org.apache.jena.riot.RiotException re) {
				throw new RuntimeException("$fs, $re")
			} catch (Exception e) {
				throw new RuntimeException("$fs, $e")
			}
	}
	
	def static loadJson(fs) {
		try {
			def c = util.Rson.load(fs)
			return "$fs, outer size=${c.size()}"
		//new JsonSlurper().parse(new File(fs))
		} catch (Exception e) {
			return "$fs, $e"
		}
	}

	static detectInvalidCharsFile(fs) {
		def s = ""
		def i=0
		new File(fs).eachLine{
			i++
			if (!isValidASCII(it)) {
				s += "$i: $it"
			}
		}
		if (s != "")
			throw new RuntimeException("$fs, $s")
	}
	
	// standard is ASCII
	// UTF8 should work but problems with DOT loading
	static boolean isValidChars( s) {
		def input = s.getBytes()
		CharsetDecoder cs = StandardCharsets.US_ASCII.newDecoder();
		//CharsetDecoder cs = StandardCharsets.UTF_8.newDecoder();
		try {
			cs.decode(ByteBuffer.wrap(input));
			return true;
		} catch (CharacterCodingException e) {
			return false;
		}
	}
	static boolean isValidASCII(s) {
		def input = s.getBytes()
		CharsetDecoder cs = StandardCharsets.US_ASCII.newDecoder();
		try {
			cs.decode(ByteBuffer.wrap(input));
			return true;
		} catch (CharacterCodingException e) {
			return false;
		}
	}

}
