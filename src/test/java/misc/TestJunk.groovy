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
		def userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/133.0  Mobile/15E148 Safari/605.1.15"
		def isMobile = userAgent ==~ /.*(Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini).*/
		//def isMobile = userAgent ==~ /.*(webOS|iPhone).*/
		println isMobile
	}

}
