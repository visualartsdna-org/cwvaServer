package misc

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import java.nio.charset.CharacterCodingException
import java.nio.charset.CharsetDecoder
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import support.StatsReport
import groovy.io.FileType

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
	
	@Test
	void testRelatedConcepts() {
		def related = "[related=the:AlizarinCrimsonDD, the:AlizarinCrimsonLCS]"
		
		def l0=related.split(/[=\]]/)
		def l=l0[1].split(",")
		l.each{
			println it.trim()
		}
	}

	static boolean isValidUTF8(def s) {
		def input = s.getBytes()
		CharsetDecoder cs = StandardCharsets.UTF_8.newDecoder();
		try {
			cs.decode(ByteBuffer.wrap(input));
			return true;
		} catch (CharacterCodingException e) {
			return false;
		}
	}
	static boolean isValidASCII(def s) {
		def input = s.getBytes()
		CharsetDecoder cs = StandardCharsets.US_ASCII.newDecoder();
		try {
			cs.decode(ByteBuffer.wrap(input));
			return true;
		} catch (CharacterCodingException e) {
			return false;
		}
	}
	@Test
	void testDetectInvalidUtf8Str() {
		def ba = [(byte) 0xC0, (byte) 0x41] as byte[]
		def valStr = "abc"
		def invStr = new String(ba)

		println("Is valid UTF-8: " + isValidUTF8(valStr));
		println("Is valid UTF-8: " + isValidUTF8(invStr));

	}

	@Test
	void testDetectInvalidUtf8File() {
		def i=0
		new File("C:/temp/git/cwvaContent/ttl/vocab/vocabulary.ttl").eachLine{
			i++
			if (!isValidUTF8(it)) {
				println "$i: $it"
			}
		}

	}

	@Test
	void testDetectInvalidUtf8FileInDir() {
		def dir = new File("C:/temp/git/cwvaContent/ttl/vocab")
		dir.eachFileRecurse (FileType.FILES) { file ->
			println "$file"
		def i=0
		file.eachLine{
			i++
			if (!isValidASCII(it)) {
				println "$i: $it"
			}
		}
		}
	}


}
