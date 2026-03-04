package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import rdf.tools.SparqlConsole
import rdf.util.DBMgr

class TestRelatedConcepts {

	def content = "C:/temp/git/cwvaContent"
//	def content = "C:/stage/cwvaContent"
	def prefixes = rdf.Prefixes.forQuery
	def ju = new JenaUtilities()

	// std rdfs db load
	@Test
	void test() {
		def cptFile = "/test/conceptTest2.txt"
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
		
		def model = ju.newModel()
		def workUri
		int i=0
		new File(cptFile).eachLine{cpts->
			def mod = ju.newModel()
			def cptList
			if (i++ % 2 ==0) {
				workUri = cpts
				return
			}
			else 
				cptList = cpts.split(/[ ,;\.\n\t]+/) 
			cptList.each{cpt->
				def query = """
CONSTRUCT {
  ?w ?op ?cpt .
} WHERE {
  BIND ($workUri AS ?w)
  BIND ($cpt AS ?cpt)
  
  {
    ?cpt a ?rt .
    FILTER (?rt NOT IN (skos:Concept, rdfs:Resource))
    ?op rdfs:range ?rt .
    
    FILTER NOT EXISTS {
      ?rt2 rdfs:subClassOf+ ?rt .
      ?cpt a ?rt2 .
      FILTER (?rt2 != ?rt)
      ?op2 rdfs:range ?rt2 .
    }
  }
  UNION
  {
    ?cpt skos:broader+ ?rtc .
    ?rtc a ?rt .
    FILTER (?rt NOT IN (skos:Concept, rdfs:Resource))
    ?op rdfs:range ?rt .
    
    FILTER NOT EXISTS {
      ?rt2 rdfs:subClassOf+ ?rt .
      ?rtc a ?rt2 .
      FILTER (?rt2 != ?rt)
      ?op2 rdfs:range ?rt2 .
    }
    
    FILTER NOT EXISTS {
      ?cpt skos:broader+ ?rtc2 .
      ?rtc2 skos:broader+ ?rtc .
      ?rtc2 a ?rt .
    }
  }
  
  # Final filter: exclude if there's a more specific property from either branch
  FILTER NOT EXISTS {
    {
      ?cpt a ?rtX .
      ?opX rdfs:range ?rtX .
      ?rtX rdfs:subClassOf+ ?rt .
      FILTER (?rtX != ?rt)
    }
    UNION
    {
      ?cpt skos:broader+ ?rtcX .
      ?rtcX a ?rtX .
      ?opX rdfs:range ?rtX .
      ?rtX rdfs:subClassOf+ ?rt .
      FILTER (?rtX != ?rt)
    }
  }
}
	"""
				//println query
				def m= ju.queryExecConstruct(dbm.rdfs,prefixes,query)
				//println "${lm[0].w} ${lm[0].op} ${lm[0].cpt}"
				println "$cpt, ${m.size()}"
				mod.add m
			}
			model.add mod
			println ju.saveModelString(mod,"ttl")
		}
		ju.saveModelFile(model,"/test/annotations.ttl","ttl")
	}
			
	
	// std rdfs db load
	@Test
	void test2() {
		def cptFile = "/test/conceptTest.txt"
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
		
		
		
		new File(cptFile).eachLine{cpts->
			def model = ju.newModel()
			def uri = "work:368e138c-bfdf-4165-8e8f-f2e5eb761d19"
			
			def cptList = cpts.split(/[ ,;\.\n\t]+/) 
			cptList.each{cpt->
				def m= ju.queryExecConstruct(dbm.rdfs,prefixes,"""
	construct {
	?w ?op ?cpt .
	} where {
	bind ($uri as ?w)
	bind ($cpt as ?cpt)
	
	?cpt skos:broader* ?rtc .
	?rtc a ?rt .
	
	filter (?rt not in (skos:Concept,rdfs:Resource))
	?op rdfs:range ?rt
	
	}
	""")
				//println "${lm[0].w} ${lm[0].op} ${lm[0].cpt}"
				println "$cpt, ${m.size()}"
				model.add m
			}
			println ju.saveModelString(model,"ttl")
		}
	}
			
	
	// std rdfs db load
	@Test
	void test1() {
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
		
		def model = ju.newModel()
		def cpts = """the:draggingPaint , the:CadmiumYellowDD , 
the:maskingFluid , the:wash , the:UltramarineLCS , the:PenNibMask , 
the:CadmiumGreenLCS , the:wetIntoDryWash , the:coldPressedPaper , 
the:watercolorGlazing , the:roughPaper , the:overPainting , 
the:PenNibPainting , the:transparentPaint , the:ChromeGreenDD, 
the:FabrianoArtisticoGranaFinaColdPress"""
		def uri = "work:368e138c-bfdf-4165-8e8f-f2e5eb761d19"
		
		def cptList = cpts.split(/[ ,\n\t]+/) 
		cptList.each{cpt->
			println cpt
			def m= ju.queryExecConstruct(dbm.rdfs,prefixes,"""
construct {
?w ?op ?cpt .
} where {
bind ($uri as ?w)
bind ($cpt as ?cpt)

?cpt skos:broader* ?rtc .
?rtc a ?rt .

filter (?rt not in (skos:Concept,rdfs:Resource))
?op rdfs:range ?rt

}
""")
			//println "${lm[0].w} ${lm[0].op} ${lm[0].cpt}"
			println m
			model.add m
		}
		
			println ju.saveModelString(model,"ttl")
	}
			
	
	@Test
	void test0() {
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
		new SparqlConsole().show(dbm.rdfs)
	}
	

}
