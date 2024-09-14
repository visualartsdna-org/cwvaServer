package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class TestJunk {
	
	@Test
	void test() {
		def m = [guid:" work:0a8cb92b-39e7-4dc9-972e-25007d8c6efc".trim()]
		def guid = (m.guid =~ /([a-f0-9\-]+)$/)[0][1]
//		def guidM = (m.guid =~ /.*([0-9a-f\-]+)$/)
//		def guid0 = guidM[0]
		println guid
	}

	@Test
	void test1() {
		def sl2 = []
		def sl = ["abcdefg\n123456",
			"this is here\nthat is there"]
		sl.each{
			sl2+= it.replaceAll("\n","<br>")
		}
		println sl2
	}

	@Test
	void test0() {
		def s = "abcdefg\n123456"
		s = s.replaceAll("\n","<br>")
		println s
	}

}
