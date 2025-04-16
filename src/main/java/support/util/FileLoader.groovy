package support.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtilities

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

}
