package misc

import static org.junit.jupiter.api.Assertions.*

import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test
import util.Tmp
class TestJunk3 {

	@Test
	void testDate() {
		def formatPattern = "MM/dd/yyyy, HH:mm:ss a"
		def ts = "10/6/2025, 8:09:54 PM"
		def newDate = new SimpleDateFormat(formatPattern).parse(ts)
		println newDate
	}

	
	@Test
	void testDelete2() {
		
		def svg = Tmp.getTemp(".svg")
		def dot = Tmp.getTemp(".dot")
		
		println svg
		println dot
		
		Tmp.delTemp(svg)
		Tmp.delTemp(dot)
		
		println Tmp.temps
	}

	@Test
	void testHost() {
		println containsHost("r=http://192.168.1.71/thesaurus/visualArtsProcess")
	}
	def ip="192.168.1.119"
	def host="http://192.168.1.71:80"
	def containsHost(s) {
		def h0 = new URL(host)
		s.contains(h0.getHost()) || s.contains(ip)
		
	}
		

}
