package cwva

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class TestNotes {

	@Test
	void test() {
		
		def directory = new File("C:/Users/ricks/iCloudDrive/Anotes")
		def contents = directory.listFiles() // Returns an array of File objects
		
		def m = [:]
		contents.findAll{file ->
			file.name =~ /[0-9\.]+.txt|cwa[0-9\.]+.txt/
		}.each { file ->
//	        println "${file.name}"
			def v = (file.name =~ /\d+(?:\.\d+)+/)[0]
//	        println "$v"
			if (!m[v]) m[v] = []
			file.eachLine{line->
				if (line =~ /^x.*/)
//					println "\t${remX(line)}"
					m[v] += remX(line)
			}
	    }
		
		// Sort by converting each part to an integer
		def sorted = m.keySet().sort { a, b ->
			def aParts = a.split(/\./).collect { it as Integer }
			def bParts = b.split(/\./).collect { it as Integer }
			
			for (int i = 0; i < Math.min(aParts.size(), bParts.size()); i++) {
				if (aParts[i] != bParts[i]) {
					return aParts[i] <=> bParts[i]
				}
			}
			return aParts.size() <=> bParts.size()
		}
		
		sorted.each{k->
			println k
			m[k].each{
				println "\t$it"
			}
			println ""
		}
	}
	@Test
	void test0() {
		
		def directory = new File("C:/Users/ricks/iCloudDrive/Anotes")
		def contents = directory.listFiles() // Returns an array of File objects
		
		contents.findAll{file ->
			file.name =~ /[0-9\.]+.txt|cwa[0-9\.]+.txt/
		}.each { file ->
//	        println "${file.name}"
			def v = (file.name =~ /\d+(?:\.\d+)+/)[0]
			println "$v"
			file.eachLine{line->
				if (line =~ /^x.*/)
					println "\t${remX(line)}"
			}
		}
	}

	def remX(s) {
		s.substring(2)
	}
}
