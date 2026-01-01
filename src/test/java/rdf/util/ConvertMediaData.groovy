package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.jena.reasoner.rulesys.BasicForwardRuleReasoner
import org.apache.jena.rdf.model.ModelFactory
import org.junit.jupiter.api.Test
import rdf.JenaUtilities

class ConvertMediaData {
	
	def ju = new JenaUtilities()
	def src = "/stage/server/cwvaContent/ttl/data"
	def tgt = "/test/data"
	
	def medMap = [
		
		
		]
	
	@Test
	void test() {
//		fix()
//		def m= ju.loadFiles("/stage/tmp")
//		new rdf.tools.SparqlConsole().show(m)
		
		track2()
	}
	
	/**
	 */
	def track2() {
		int i=0
		new File(src).eachFile { file ->
			println file.name
			
			// this is a pattern for 
			// managing model data replacements
			// in JSON rather than SPARQL
			// ttl->json collection
			def m = ju.loadFiles(file.path)
			def js = ju.saveModelString(m,"json-ld")
			def jc = new JsonSlurper().parseText(js)
			
			// work
			
			// json collection -> ttl
			def js2 = new JsonOutput().toJson(jc)
			def m2 = ju.saveStringModel(js2, "json-ld")
			ju.saveModelFile(m2, "$tgt/${file.name}","ttl")
			
//				println "$i"
			i++
		}
		println "count = $i"
	}

	def track() {
		int i=0
		new File(src).eachFile { file ->
			//println file.name
			
			def m = ju.loadFiles(file.path)
			
			def c = ju.queryListMap1(m,rdf.Prefixes.forQuery,"""
select ?t ?m {
	?s a 	?t ;
			vad:media ?m ;
			.
			filter (?t in (
vad:BlenderComposition,
vad:DigitalImage,
vad:Drawing,
vad:EggTempera,
vad:LindenmayerSystemImage,
vad:PaintApplicationImage,
vad:Watercolor,
vad:SoftwareApplication
			))
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
