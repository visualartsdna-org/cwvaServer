package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtils

class TestJunk2 {

	def ju = new JenaUtils()
	
	@Test
	void test() {
		def fs = "C:/stage/server/cwvaContent/ttl/vocab/palette.ttl"
		def md = ju.loadFiles(fs)
		

		def rl =  ju.queryListMap1(md,rdf.Prefixes.forQuery,"""
# colors
select ?o ?l ?sym{
	?s skos:broader the:WatercolorPaint ;
		the:order ?o ;
		the:symbol ?sym ;
		rdfs:label ?l .
} order by ?o

			""")
				rl.each{
					println "${it.o}\t${it.l} ${it.sym}"
				}
		
	}

	@Test
	void test0() {
		def fs = "C:/stage/server/cwvaContent/ttl/vocab/palette.ttl"
		def md = ju.loadFiles(fs)
		

		def rl =  ju.queryListMap1(md,rdf.Prefixes.forQuery,"""
# colors
select ?o ?l ?sym{
	?s skos:broader the:WatercolorPaint ;
		the:order ?o ;
		the:symbol ?sym ;
		rdfs:label ?l .
} order by ?l

			""")
				rl.each{
					println "${it.o}\t${it.l} ${it.sym}"
				}
		
	}

}
