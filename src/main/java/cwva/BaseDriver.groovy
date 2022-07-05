package cwva

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class BaseDriver {

	// create array of strings for main()
	def toArgs(s) {
		
		def inquote
		def l=[]
		def sb=new StringBuilder()
		s.trim().each { 
			if (it=='"' && !inquote) {
				inquote=true
			}
			else if (it=='"' && inquote) {
				inquote=false
			}
			else if (it =~ /[ \t\n]/ && !inquote) {
				l.add sb.toString()
				sb=new StringBuilder()
			}
			else {
				sb.append it
			}
		}
		l.add sb.toString()
		l  as String[]
	}
	
	@Test
	void test() {
		def ttl = "art.ttl"
		def img = "temp.jpg"
		def html = "temp.html"
		
		[
"""
				   -inFile $ttl -types "vad:Drawing, vad:Watercolor" -scope basic -imgFile $img
""",
"""
				   -ttl $ttl -html $html -types "vad:Drawing" -scope basic"""
			].each{
			println it
		toArgs( it
			).each { s->
				println s
			}
		}
			
	}

}
