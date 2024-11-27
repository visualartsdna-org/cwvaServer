package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class QueryMgrTest {

	@Test
	void test() {
		
		loadQueries().each {
			println "$it"
		}
	}
	
	def loadQueries() {
		def qm = [:]
		def ql = new File("/temp/test/queries.txt").text
		int i=0
		def s=""
		ql.eachLine{
			if (it.startsWith("#")) return
			if (it.trim() == "") {
				qm["s${i++}"] = s
				s = ""
			}
			else {
				s += it + "\n"
			}
		}
		qm
	}
}
