package util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities

class TestURI2CURI {

	def ju = new JenaUtilities()
	
	@Test
	void test() {
		def m = ju.loadFiles("/stage/server/cwvaContent/ttl/model")
		
		println makeCuri (m,"http://visualartsdna.org/thesaurus/stuff")
		println makeCuri (m,"http://visualartsdna.org/model/stuff")
	}
	def makeCuri(m,s) {
		
	def list = ju.getPrefix(m,s)
	"${list[0]}${list[1]}"
	}

}
