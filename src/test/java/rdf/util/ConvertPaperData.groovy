package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.reasoner.rulesys.BasicForwardRuleReasoner
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Test
import rdf.JenaUtilities

class ConvertPaperData {
	
	def ju = new JenaUtilities()
	def src = "/stage/server/cwvaContent/ttl/data"
	def tgt = "/stage/data"
	
	@Test
	void testUpdArtFile() {
			def m = ju.loadFiles("/stage/tmp/art.ttl")
			ju.saveModelFile(m, "$tgt/art.ttl","ttl" )
	}


	@Test
	void test() {
		fix()
//		def m= ju.loadFiles("/stage/tmp")
//		new rdf.tools.SparqlConsole().show(m)
		
		//track2()
	}
	/**
	 * Discrete Storage: Adding the T-Box via 
	 * addSubModel does not copy the triples 
	 * into the A-Box. Changes made to the 
	 * original tBox or aBox objects will be 
	 * reflected in the virtualModel 
	 * because it maintains references to them.
	 * But, you can save just the modified A-Box
	 * data alone.
	 * @return
	 */
	def fix() {
		def mm = ju.loadFiles("/stage/tmp/materials.ttl")
		println mm.size()
		int i=0
		new File(src).eachFile { file ->
			//println file.name
			
			def dm = ju.loadFiles(file.path)
			def m = ModelFactory.createInfModel(new BasicForwardRuleReasoner( []),
 mm,
 dm)
			println dm.size()
			println mm.size()
			println m.size()
			
			def c = ju.queryListMap1(m,rdf.Prefixes.forQuery,"""
select ?s ?p ?w ?f {
	?s a 	?t ;
			vad:hasPaper ?p ;
			vad:hasPaperFinish ?f ;
			vad:hasPaperWeight ?w ;
			.
filter (?t in (	vad:Watercolor, vad:Drawing))
}
""")
			c.each{ 
				println it
				ju.queryExecUpdate(m,rdf.Prefixes.forQuery,"""
DELETE { 
	?s 			vad:hasPaper ?p ;
			vad:hasPaperFinish ?f ;
			vad:hasPaperWeight ?w ;
			.
}
INSERT { 
	?s vad:hasPaper ?cp .
}
WHERE
  { 
	?s a 	?t ;
			vad:hasPaper ?p ;
			vad:hasPaperFinish ?f ;
			vad:hasPaperWeight ?w ;
			.
	?cp a vad:Paper ;
			rdfs:label ?cpl ;
			vad:hasPaperFinish ?cf ;
			vad:hasPaperWeight ?cw ;
			.
	filter ( regex(?cpl, ?p, "i"))
	filter (?f = ?cf)
	filter (?w = ?cw)
			
filter (?t in (	vad:Watercolor, vad:Drawing))
  } 
""")
				
				i++

				//println ju.saveModelString(dm)
				ju.saveModelFile(dm, "$tgt/${file.name}", "ttl")
				println "$i"
				}
		}
		println "count = $i"
	}

	def track() {
		int i=0
		new File(src).eachFile { file ->
			//println file.name
			
			def m = ju.loadFiles(file.path)
			
			def c = ju.queryListMap1(m,rdf.Prefixes.forQuery,"""
select ?s ?p ?w ?f {
	?s a 	?t ;
			vad:hasPaper ?p ;
			vad:hasPaperFinish ?f ;
			vad:hasPaperWeight ?w ;
			.
filter (?t in (	vad:Watercolor, vad:Drawing))
}
""")
			c.each{ 
				println it 
				i++
				}
		}
		println "count = $i"
	}

	def track2() {
		int i=0
		new File(src).eachFile { file ->
			//println file.name
			
			def m = ju.loadFiles(file.path)
			
			def c = ju.queryListMap1(m,rdf.Prefixes.forQuery,"""
select ?s ?p ?w ?f {
	?s a 	?t ;
			vad:hasPaper ?p ;
			.
	Optional {
	?s a 	?t ;
			vad:hasPaperFinish ?f ;
			vad:hasPaperWeight ?w ;
			.
			}
filter (?t in (	vad:Watercolor, vad:Drawing))
}
""")
			c.each{ 
				println it 
				i++
				}
		}
		println "count = $i"
	}

	def trackPaperOnly() {
		int i=0
		new File(src).eachFile { file ->
			//println file.name
			
			def m = ju.loadFiles(file.path)
			
			def c = ju.queryListMap1(m,rdf.Prefixes.forQuery,"""
select ?s ?p {
	?s a 	?t ;
			vad:hasPaper ?p ;
			.
filter (?t in (	vad:Watercolor, vad:Drawing))
}
""")
			c.each{ 
				println it 
				i++
				}
		}
		println "count = $i"
	}

}
