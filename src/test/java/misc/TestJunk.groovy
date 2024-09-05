package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class TestJunk {
	
	@Test
	void test() {
		new util.Exec().execVerbose("lsl")
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
