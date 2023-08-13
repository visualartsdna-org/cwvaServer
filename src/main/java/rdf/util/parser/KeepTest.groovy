package rdf.util.parser

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KeepTest {

	@Test
	void test() {
		
//		def base = "C:/temp/Takeout/Takeout/Keep"
		def base = "C:/temp/rsart/Takeout/Keep"
//		def m0= new TkoExtract().process("$base/Test Extinction.json")
		def m0= new TkoExtract().process("$base")
		
		def s = new TtlBuilder().process(m0,"$base/test")
				
		//println s
	}
	
	static def verbose=false

	@Test
	void test1() {
		
//		def base = "C:/temp/Takeout/Takeout/Keep/"
		def base = "C:/temp/rsart/Takeout/Keep"
		def m0= new TkoExtract().process("$base/Test Extinction.json")
//		def m0= new TkoExtract().process("$base")
		
		m0.each{k1,v1->
			

			if (verbose) println "$k1"
		def m =new Keep().parseKeepConcepts(v1)
			if (m.topConcept) if (verbose) println "${m.topConcept}\n"
		
		try {
		m.each{k,v->
			if (k=="topConcept") return
			if (verbose) println "$k\n"

			if (v.containsKey("ann"))
			v.ann.each{k2,v2->
				if (verbose) println "\t$k2=$v2"
			}
			if (verbose) println "${v.text}\n"
		}
		} catch (Exception e) {
			println e
		}
		}
	}

	@Test
	void test0() {
		
		def fn = "./tkoTest2.txt"
		def s = new File(fn).text
		def m = new Keep().parseKeepConcepts(s)
		
		m.each{k,v->
			//println "$k\n\n${v.ann?v.ann:''}\n${v.text}\n"
			println "$k\n"
			v.ann.each{k2,v2->
				println "\t$k2=$v2"
			}
			println "${v.text}\n"
		}
	}

	@Test
	void testExtract() {
		def base = "C:/temp/Takeout/Takeout/Keep/"
		def m= new TkoExtract().process("$base/Asheville Trip Suggestions.json")
		m.each{k,v->
			println "$k=$v\n"
		}
	}
		
	// support reference to list of URIs
	@Test
	void testUriList() {
		[
			member:"tko:abc,tko:def",
			member2:"<abc>,<def>"
			].each{k2,v2->
		println """
		${TtlBuilder.nsMap[k2]}:$k2 ${v2=~/^<[A-Za-z_0-9\-\.]+>$|^[a-z]+:.*$/?v2:"\"$v2\""} ;
		"""
		}
	}
	
	@Test
	void testUSeconds() {
		def i = 1656256466731000
		def inst = Util.getInstantFromMicros(i)		
		println inst
		
	}

}
