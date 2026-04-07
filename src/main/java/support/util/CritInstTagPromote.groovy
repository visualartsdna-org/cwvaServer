package support.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import rdf.util.DBMgr
import support.AnnotateWork

class CritInstTagPromote {
	
	def src = "C:/stage/conceptQueue"
	def ju = new JenaUtilities()

	@Test
	void test() {
		
		def model = ju.loadFilesHere(src)
		def lm = loadTags(model)
		def wmap = [:]
		lm.each{
			println "${ju.getCuri(model,it.s)}, ${ju.getCuri(model,it.tag)}"
			
			if (!wmap[ju.getCuri(model,it.s)])
				wmap[ju.getCuri(model,it.s)] = []
			wmap[ju.getCuri(model,it.s)] += ju.getCuri(model,it.tag) 
		}
		
		def rdfs = loadDB()
		
		wmap.each{work,tags->
			new AnnotateWork().linkConceptsToWork(work, tags,rdfs)
		}
		
	}

	def loadTags(model) {
		
		def lm = ju.queryListMap1(model,rdf.Prefixes.forQuery,"""
select ?s ?tag {
?s the:tag ?tag
} order by ?s ?tag
""")
		lm
	}
	
	def loadDB() {
		def content = "C:/stage/server/cwvaContent"
		//	def content = "C:/temp/git/cwvaContent"
			def map = [
				data: "$content/ttl/data",
				vocab: "$content/ttl/vocab",
				tags: "$content/ttl/tags",
				model: "$content/ttl/model",
				dir: "/temp/git/cwva"
				]
			
			def dbm = new DBMgr()
			dbm.cfg = map
			dbm.loadDb()
			dbm.print()
			//new SparqlConsole().show(dbm.rdfs)
			dbm.rdfs
	}
	/**
	 * Link concept CURIs to a work URI in the RDF model.
	 * Override or extend this method to match your triple-store write strategy.
	 *
	 * @param workUri  the CURI or full URI of the work (e.g. "work:abcd")
	 * @param curis    list of thesaurus CURIs (e.g. ["the:tag", "the:draggingPaint"])
	 */
//	def linkConceptsToWork(String workUri, List<String> curis) {
//
//		def model = ju.newModel()
//		curis.each{cpt->
//			def query = """
//CONSTRUCT {
//  ?w ?op ?cpt .
//} WHERE {
//  BIND ($workUri AS ?w)
//  BIND ($cpt AS ?cpt)
//  
//  {
//    ?cpt a ?rt .
//    FILTER (?rt NOT IN (skos:Concept, rdfs:Resource))
//    ?op rdfs:range ?rt .
//    ?op a owl:ObjectProperty .
//    
//    FILTER NOT EXISTS {
//      ?rt2 rdfs:subClassOf+ ?rt .
//      ?cpt a ?rt2 .
//      FILTER (?rt2 != ?rt)
//      ?op2 rdfs:range ?rt2 .
//    }
//  }
//  UNION
//  {
//    ?cpt skos:broader+ ?rtc .
//    ?rtc a ?rt .
//    FILTER (?rt NOT IN (skos:Concept, rdfs:Resource))
//    ?op rdfs:range ?rt .
//    ?op a owl:ObjectProperty .
//    
//    FILTER NOT EXISTS {
//      ?rt2 rdfs:subClassOf+ ?rt .
//      ?rtc a ?rt2 .
//      FILTER (?rt2 != ?rt)
//      ?op2 rdfs:range ?rt2 .
//    }
//    
//    FILTER NOT EXISTS {
//      ?cpt skos:broader+ ?rtc2 .
//      ?rtc2 skos:broader+ ?rtc .
//      ?rtc2 a ?rt .
//    }
//  }
//  
//  # Final filter: exclude if there's a more specific property from either branch
//  FILTER NOT EXISTS {
//    {
//      ?cpt a ?rtX .
//      ?opX rdfs:range ?rtX .
//      ?rtX rdfs:subClassOf+ ?rt .
//      FILTER (?rtX != ?rt)
//    }
//    UNION
//    {
//      ?cpt skos:broader+ ?rtcX .
//      ?rtcX a ?rtX .
//      ?opX rdfs:range ?rtX .
//      ?rtX rdfs:subClassOf+ ?rt .
//      FILTER (?rtX != ?rt)
//    }
//  }
//}
//	"""
//			//println query
//			def m= ju.queryExecConstruct(
//				rdfs,
//				rdf.Prefixes.forQuery,query)
//			//println "${lm[0].w} ${lm[0].op} ${lm[0].cpt}"
//			//println "$cpt, ${m.size()}"
//			model.add m
//		}
//		//println ju.saveModelString(mod,"ttl")
//		
//		// try to get work from DB
//		// add in anno model
//		
//		def guid = workUri.substring(5)
//		def wmod = ju.queryDescribe(
//			rdfs,
//			rdf.Prefixes.forQuery,"""
//			describe $workUri
//""")
//		// if no work in DB
//		// look in /stage/data for incipient work
//		// if exists load work
//		// add in anno model
//		if (wmod.size()==0) {
//			if (new File("/stage/data/${guid}.ttl").exists())
//				wmod = ju.loadFiles("/stage/data/${guid}.ttl")
//			if (wmod.size()==0) {
//			// else throw error
//			// no such work
//				throw new Exception("No existing ${workUri}.ttl")
//				}
//		}
//		
//		
//		// Intended for adding attributes
//		// to work.  For editing,
//		// handle manually
//		
//		
//		// write to /stage/data as work
//		wmod.add model
//		ju.saveModelFile(wmod,"/stage/data/${guid}.ttl","ttl")
//		
//	}
	

}
