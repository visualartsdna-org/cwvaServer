package misc

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import support.StatsReport

class TestJunk2 {

	def ju = new JenaUtils()
	
	
	@Test
	void testMetricReport() {
		
		def sr = new StatsReport()
		def model = sr.getStats(new File("C:/Users/ricks/Downloads/stuff.json").text)
		sr.loadQueries("C:/work/stats/query.txt")
		model.add ju.loadFiles("/temp/git/cwvaContent/ttl")
		def s = sr.reportHtml(model)
		println "${model.size()}"
		println s
	}

	// C:/Users/ricks/Downloads/stuff.json
	@Test
	void test() {
		
		def map=[:]
		def c = new JsonSlurper().parse(new File("C:/Users/ricks/Downloads/stuff (1).json"))
		c.sort().each{k,v->
			println k
			}
		}

	@Test
	void test3() {
		def fs = "C:/stage/server/cwvaContent/ttl/vocab/palette.ttl"
		def md = ju.loadFiles(fs)
		

		def rl =  ju.queryListMap1(md,rdf.Prefixes.forQuery,"""
# colors
select ?rgb ?l ?sym{
	?s skos:broader the:WatercolorPaint ;
		the:order ?o ;
		the:symbol ?sym ;
		the:ARGB	?rgb ;
		rdfs:label ?l .
} order by ?rgb

			""")
		def last = [rgb:"FF000000"]
		def color = "FF"+"929AA3" // paste your color here
		rl.each{
			if (last 
				&& it.rgb > color
				&& it.rgb > last.rgb) {
				println "${last.rgb}\t${last.l} ${last.sym}"
				println "${it.rgb}\t${it.l} ${it.sym}"
				last = null
			}
			else if (last) 
				last = it
		}
		
	}

	@Test
	void test2() {
		def fs = "C:/stage/server/cwvaContent/ttl/vocab/palette.ttl"
		def md = ju.loadFiles(fs)
		

		def rl =  ju.queryListMap1(md,rdf.Prefixes.forQuery,"""
# colors
select ?rgb ?l ?sym{
		bind(strdt("ffBDBEC1", xs:hexBinary) as ?hcolor)
	?s skos:broader the:WatercolorPaint ;
		the:order ?o ;
		the:symbol ?sym ;
		the:ARGB	?rgb ;
		rdfs:label ?l .
		bind(strdt(?rgb, xs:hexBinary) as ?hex)
} order by ?rgb

			""")
				rl.each{
					println "${it.rgb}\t${it.l} ${it.sym}"
				}
		
	}

	@Test
	void test1() {
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
