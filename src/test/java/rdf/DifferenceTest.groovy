package rdf

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class DifferenceTest {
	
	def ju = new JenaUtilities()

	@Test
	void test() {
		difference(
			"/stage/server/cwvaContent/ttl/vocab/watercolorPaper.ttl",
			"/stage/tmp/graph.ttl")
	}
	
	@Test
	void testBulk() {
		int i=0
		vocabList.each{
			
			println ""
			println "$it vs graph${i}.ttl"
			difference(
				"/stage/server/cwvaContent/ttl/vocab/$it",
				"/stage/tmp/graphTtl/graph${i++}.ttl")
			}
	}
	
	def difference(f1,f2) {
		def m1 = ju.loadFiles(f1)
		def m2 = ju.loadFiles(f2)
		def md = m1.difference(m2)
		println """
difference(m), Create a new, independent, model 
containing all the statements 
in this model which are not in another."""
		println "In m1 not in m2 difference size = ${md.size()}"
		println ju.saveModelString(md,"ttl")
		def md2 = m2.difference(m1)
		println "In m2 not in m1 difference size = ${md2.size()}"
		println ju.saveModelString(md2,"ttl")
	}
	
def vocabList = [
"concepts_20260120.ttl",
"digital.ttl",
"digitalProcess.ttl",
"digitalTerms.ttl",
"interpretation.ttl",
"paintingTerms.ttl",
"palette.ttl",
"pigments.ttl",
"process.ttl",
"sculpture.ttl",
"tensegrityVocab.ttl",
"vocab2.ttl",
"watercolor.ttl",
"watercolor_criticism.ttl",
"watercolorPaper.ttl",
"watercolorProcess.ttl",
	]
}

