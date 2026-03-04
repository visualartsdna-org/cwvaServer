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
	
}
