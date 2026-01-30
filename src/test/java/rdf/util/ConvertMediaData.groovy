package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.jena.reasoner.rulesys.BasicForwardRuleReasoner
import org.apache.jena.rdf.model.Model
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
	
		track5()
	}
	
	
	//
	def correctRdf0(m, ip, io, op, oo) {
		def s = """
delete {
	?s ?p ?o
}
insert {
	?s $op $oo
} where {
	?s a vad:Watercolor ;
		?p ?o .
	filter( ?p = $ip && ?o = $io)
}
"""
		ju.queryExecUpdate(m,rdf.Prefixes.forQuery,s)
	}
	

	//
	def correctRdf1(m, ip, iol, op, oo) {
		def s = """
delete {
	?s ?p ?o
}
insert {
	?s $op $oo
} where {
	?s a vad:Watercolor ;
		?p ?o .
	filter( ?p = $ip && ?o in ($iol))
}
"""
		ju.queryExecUpdate(m,rdf.Prefixes.forQuery,s)
	}
	

	//
	def correctRdf2(m, ip, iol, op, oo, types) {
		def s = """
delete {
	?s ?p ?o
}
insert {
	?s $op $oo
} where {
	?s a ?t ;
		?p ?o .
	filter( ?t in ($types))
	filter( ?p = $ip && ?o in ($iol))
}
"""
		ju.queryExecUpdate(m,rdf.Prefixes.forQuery,s)
	}
	
	//
	def correctRdfType(m, ip, io, op, oo) {
		def s = """
delete {
	?s $ip $io
}
insert {
	?s $op $oo
} where {
	?s $ip $io .
}
"""
		ju.queryExecUpdate(m,rdf.Prefixes.forQuery,s)
	}
	


	def fixList = [
		["vad:media","'Watercolor','watercolor'", "vad:hasMedia", "vad:Watercolor"],
		["vad:media","'WatercolorPencil','watercolor pencil'", "vad:hasMedia", "vad:WatercolorPencil"],
		["vad:media","'pencil'", "vad:hasMedia", "vad:Pencil"],
		["vad:media","'Digital Imagery','DigitalMedia','Graphics'", "vad:hasMedia", "vad:DigitalImagery"],
		["vad:media","'Egg Tempera','EggTempera'", "vad:hasMedia", "vad:EggTempera"],
		]
	def typeFixList = [
		["rdf:type","vad:Watercolor", "rdf:type", "vad:WatercolorWork"],
		["rdf:type","vad:Drawing", "rdf:type", "vad:DrawingWork"],
		["rdf:type","vad:EggTempera", "rdf:type", "vad:EggTemperaWork"],
		["rdf:type","vad:LindenmayerSystemImage", "rdf:type", "vad:LindenmayerSystemImageWork"],
		["rdf:type","vad:PaintApplication", "rdf:type", "vad:PaintApplicationWork"],
		["rdf:type","vad:BlenderComposition", "rdf:type", "vad:BlenderCompositionWork"],
		]
		
	def types = "vad:Watercolor,vad:Drawing,vad:EggTempera,vad:LindenmayerSystem,vad:PaintApplication,vad:BlenderComposition"
	
	/**
	 * Test using sparql to 
	 * edit data
	 */
	def track5() {
		int i=0,c=0
		new File(src).eachFile { file ->
			println file.name
			//if (file.name != "046d99ed-db31-4a55-8fc9-81759859623f.ttl") return
//			println new File("$src/${file.name}").text

			def m = ju.loadFiles(file.path)
			def m0 = ju.newModel().add m
			
			fixList.each{
				correctRdf2(m, it[0],it[1],it[2],it[3], types)
			}

			typeFixList.each{
				correctRdfType(m, it[0],it[1],it[2],it[3])
			}

			if (m.difference(m0).size() > 0) {
				ju.saveModelFile(m, "$tgt/${file.name}","ttl")
				c++
			}
			
//			println new File("$tgt/${file.name}").text
			i++
		}
		println "fixed $c / $i"
	}

	/**
	 * Test using sparql to 
	 * edit data
	 */
	def track4() {
		int i=0
		new File(src).eachFile { file ->
			println file.name
			if (file.name != "046d99ed-db31-4a55-8fc9-81759859623f.ttl") return
			println new File("$src/${file.name}").text

			def m = ju.loadFiles(file.path)
			
			
			correctRdf(m, "vad:media",
				 "'Watercolor'", "vad:hasMedia", "vad:Watercolor")


			ju.saveModelFile(m, "$tgt/${file.name}","ttl")
			
			println new File("$tgt/${file.name}").text
			i++
		}
		println "count = $i"
	}

	/**
	 * Test using sparql to 
	 * edit data
	 */
	def track3() {
		int i=0
		new File(src).eachFile { file ->
			println file.name
			if (file.name != "046d99ed-db31-4a55-8fc9-81759859623f.ttl") return
			println new File("$src/${file.name}").text
			// this is a pattern for 
			// managing model data replacements
			// in JSON rather than SPARQL
			// ttl->json collection
			def m = ju.loadFiles(file.path)
			
			// work
			
//			ju.queryExecUpdate(m,rdf.Prefixes.forQuery,"""
//
//delete {
//	?s ?p ?o
//}
//insert {
//	?s vad:hasMedia vad:Watercolor
//} where {
//
//	?s a vad:Watercolor ;
//		?p ?o .
//
//	filter( ?p = vad:media && ?o = "Watercolor")
//
//}
//""")

			ju.queryExecUpdate(m,rdf.Prefixes.forQuery,"""
delete {
	?s ?p ?o
}
insert {
	?s vad:hasMedia vad:Watercolor
} where {
	?s a vad:Watercolor ;
		?p ?o .
	filter( ?p = vad:media && ?o = "Watercolor")
}
""")

			ju.queryExecUpdate(m,rdf.Prefixes.forQuery,"""
delete {
	?s ?p ?o
}
insert {
	?s vad:hasMedia vad:WatercolorPencil
} where {
	?s a vad:Watercolor ;
		?p ?o .
	filter( ?p = vad:media && ?o = "WatercolorPencil")
}
""")

			ju.saveModelFile(m, "$tgt/${file.name}","ttl")
			
			println new File("$tgt/${file.name}").text
			i++
		}
		println "count = $i"
	}

	/**
	 * Test using json-ld derived collection to 
	 * edit data
	 */
	def track2() {
		int i=0
		new File(src).eachFile { file ->
			println file.name
			
			// this is a pattern for 
			// managing model data replacements
			// in JSON rather than SPARQL
			// ttl->json collection
//			def m = ju.loadFiles(file.path)
//			def js = ju.saveModelString(m,"json-ld")
//			def jc = new JsonSlurper().parseText(js)
			def jc = fromTtl2JsonldCol(file.path)
			
			// work
			//println js
			
			if (jc["@type"] == "vad:Watercolor") {
				jc["@type"] = "vad:WatercolorWork"
				if (jc["media"].contains("Watercolor")) {
					if (!jc["hasMedia"]) jc["hasMedia"]= []
					jc["hasMedia"] += "vad:Watercolor"
				}
				if (jc["media"].contains("WatercolorPencil")) {
					jc["hasMedia"] += "vad:WatercolorPencil"
				}
				if (jc["media"]) {
					jc.remove("media")
					jc["@context"]["hasMedia"] = ["@id":"http://visualartsdna.org/model/hasMedia","@type":"@id"]
				}
			}
			fromJsonldCol2Ttl(jc, "$tgt/${file.name}")
			
			// json collection -> ttl
//			def js2 = JsonOutput.prettyPrint(new JsonOutput().toJson(jc))
//			println js2
//			def m2 = ju.saveStringModel(js2, "json-ld")
//			ju.saveModelFile(m2, "$tgt/${file.name}","ttl")
			
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
	// this is a pattern for
	// managing model data replacements
	// in JSON collection rather than with SPARQL
	// ttl->json collection
	// TTL to JSON-LD as Collection
	def fromTtl2JsonldCol(path) {
		def m = ju.loadFiles(path)
		def js = ju.saveModelString(m,"json-ld")
		def jc = new JsonSlurper().parseText(js)
		println jc.media ? jc.media : "n/a"
		jc
	}
	
	// JSON-LD as Collection to TTL
	def fromJsonldCol2Ttl(jc, filename) {
		def js2 = JsonOutput.prettyPrint(new JsonOutput().toJson(jc))
		println js2
		def m2 = ju.saveStringModel(js2, "json-ld")
		ju.saveModelFile(m2, filename,"ttl")
	}
	
	
	
	@Test
	void testDiff() {
		difference()
	}
	
	/**
	 */
	def difference() {
		int i=0,c=0
		new File(tgt).eachFile { file ->
			println "\n${file.name}"
			//if (file.name != "046d99ed-db31-4a55-8fc9-81759859623f.ttl") return
//			println new File("$src/${file.name}").text

			def m1 = ju.loadFiles(file.path)
			def m0 = ju.loadFiles("$src/${file.name}")
			
			def md = m1.difference(m0)
			def s = ju.saveModelString(md,"ttl")
			println "$s"
			
		}
	}



}
