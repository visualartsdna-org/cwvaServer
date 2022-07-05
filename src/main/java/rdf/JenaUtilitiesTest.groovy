package rdf

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class JenaUtilitiesTest {

	@Test
	void test1() {
		def spec = "C:/test/cwva/ttl/data/dig"
		def m = new JenaUtilities().loadFiles(spec)
		println new JenaUtilities().saveModelString(m)
		
	}

	@Test
	void test() {
		def spec = "C:/test/cwva/ttl"
		def m = new JenaUtilities().loadFiles(spec)
		println m.size()
		
	}

}
