package rdf

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class JenaUtilitiesTest {

	def ju = new JenaUtilities()
	@Test
	void test() {
		
		def instances = "/test/rdf/test0.ttl"
		def data = ju.loadFiles(instances);
		def l= ju.getListData(data,"skos:definition","the:list",
			["work:0b2ea129-63eb-421b-9ec0-c622e365a6da88",
			"work:0b2ea129-63eb-421b-9ec0-c622e365a6da99"
				])
			l.each{println it}
		
	}
	
	@Test
	void test5() {
		
		def instances = "/test/rdf/test0.ttl"
		def data = ju.loadFiles(instances);
		def m = ju.queryDescribe(data, """
prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix work:  <http://visualartsdna.org/work/> 
prefix the:   <http://visualartsdna.org/thesaurus/> 
""", """
									
			describe work:0b2ea129-63eb-421b-9ec0-c622e365a6da99
									""")
		def l= ju.getList(m,"the:list")
		def m2= ju.setList(m,"the:list",l)
		ju.getList(m2,"the:list").each{println it}
		
	}
	
	@Test
	void test1() {
		def spec = "C:/test/cwva/ttl/data/dig"
		def m = new JenaUtilities().loadFiles(spec)
		println new JenaUtilities().saveModelString(m)
		
	}

	@Test
	void test2() {
		def spec = "C:/test/cwva/ttl"
		def m = new JenaUtilities().loadFiles(spec)
		println m.size()
		
	}

}
