package drawing

import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import java.awt.Color
import drawing.translate.GenConfig
import drawing.translate.Translate
import drawing.lsys.LSystem
import drawing.QueryGraphDriver
import drawing.pen.*
import util.Rson
import util.Tmp
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import rdf.JenaUtils
import com.icafe4j.image.gif.GIFTweaker;
import org.junit.jupiter.api.Test

class QueryGraphDriverLocal extends QueryGraphDriver{
	
	
	def queryC = """
# Watercolors
${rdf.Prefixes.forQuery}
construct {
?w ?p ?s .
}{
	#bind (work:da79b4be-3442-4b6b-bdb4-107b2682c560 as ?w)
	?w ?p ?s .
	?w a vad:Watercolor .
	#?w a vad:Drawing .
	#?w vad:hasNFT ?bn .
	#filter(?s="pencil" || ?s=vad:Drawing)
}
"""
	
	def queryD = """
# Drawings
${rdf.Prefixes.forQuery}
describe ?w
{
	#bind (work:da79b4be-3442-4b6b-bdb4-107b2682c560 as ?w)
	?w ?p ?s .
	#?w a vad:Watercolor .
	?w a vad:Drawing .
	#?w vad:hasNFT ?bn .
	#filter(?s="pencil" || ?s=vad:Drawing)
}
"""
	
	@Test
	void test() {
		def infile = "C:/test/cwva/ttl/data"
		def ruleFile = "lsys.json" // lsys config
		def imgFile = "lsys.jpg"

		translate(infile,ruleFile,"""
# Store
${rdf.Prefixes.forQuery}

construct {
	?s a schema:OnlineStore .
	?s schema:offers ?offer .
	?offer rdfs:label ?offLabel .
	?offer skos:member ?member .
	?offer schema:about ?about .
	?member rdfs:label ?memLabel .
	?offer vad:hasArtistProfile ?prof .
	?prof foaf:maker ?sw .
	?sw schema:about ?swAbout .
} {
	?s a schema:OnlineStore .
	?s schema:offers ?offer .
	?offer rdfs:label ?offLabel .
	?offer skos:member ?member .
	?offer schema:about ?about .
	?member rdfs:label ?memLabel .
	?offer vad:hasArtistProfile ?prof .
	?prof foaf:maker ?sw .
	?sw schema:about ?swAbout .
	} order by ?offLabel ?memLabel
""",6000)
		new DataGraphDriver().driver(ruleFile,imgFile,0.02)
	}
	

	@Test
	void test0() {
		def infile = "C:/stage/march2022/node/ttl/art.ttl"
//		def infile = "C:/stage/plannedSeptember/node/ttl/cwva.ttl"
		def ruleFile = "lsys.json" // lsys config
		def imgFile = "lsys.jpg"

//		def infile = "artGraphTest.ttl"
//		def ruleFile = "lsysArt.json" // lsys config
//		def imgFile = "lsysArt.jpg"
		
//		def infile = "cwvaGrfTst.ttl"
//		def ruleFile = "lsysCwva.json" // lsys config
//		def imgFile = "lsysCwva.jpg"
		
		translate(infile,ruleFile,queryC,6000)
		new DataGraphDriver().driver(ruleFile,imgFile,0.02)
	}
	

	def translate(infile,ruleFile,query,size) {
		def trans = new Translate()
		
		def jsonld = Tmp.getTemp("jsonld",".txt")
		def tree = Tmp.getTemp("tree",".txt")
		def root = "query"
		
		def jena = new JenaUtils()
		def model = jena.loadFiles(infile)
		def m2
		if (query.contains("construct"))
			m2 = jena.queryExecConstruct(model,"", query)
		else if (query.contains("describe"))
			m2 = jena.queryDescribe(model,"", query)
		jena.saveModelFile(m2, jsonld, "JSON-LD")
		
		def strLimit = 100
		trans.jsonld2TreeInst(jsonld,tree,root)
		
//		println jsonld
//		println tree
		
		def ts = trans.tree2Turtle(tree,root)
		def desc = getQueryDesc(query)
		
		new GenConfig().write(ruleFile, ts, desc, size, "X=X", "realtime", "VisualArtsDNA.org")
		//write(ruleFile,ts,desc,size)
		Tmp.delTemp(jsonld)
		Tmp.delTemp(tree)
	}
	
	def getQueryDesc(q) {
		def desc
		def l = q.split(/\n/)
		desc = l.find {
			it =~ /^#/
		}
		return desc.replaceAll(/^#[ ]*/,"")
	}
	
	static def driverSelectType(type) {
		def sql = """
# A selection of basic data for works of type $type
${rdf.Prefixes.forQuery}			
construct {
?s rdfs:label ?label .
?s schema:description ?desc .
?s a ?type .
} {
		?s a ?type .
		FILTER ( ?type in (${type}))
		?s rdfs:label ?label .
		?s schema:description ?desc .
		}
"""
		sql
	}
		
	static def driverSelectTypeNFT(type) {
		def sql = """
# A selection of NFT data for works of type $type
${rdf.Prefixes.forQuery}			
construct {
?s rdfs:label ?label .
?s schema:description ?desc .
?s vad:hasNFT ?nft .
?nft vad:hasNFTTokenID ?tid .
?nft vad:hasNFTContractAddress ?ca .
?s a ?type .
} {
		?s a ?type .
		FILTER ( ?type in (${type}))
		?s rdfs:label ?label .
		?s schema:description ?desc .
		?s vad:hasNFT ?nft .
		?nft vad:hasNFTTokenID ?tid .
		?nft vad:hasNFTContractAddress ?ca .
		}
"""
		sql
	}
		
	static def driverSelectTypePhysical(type) {
		def sql = """
# A selection of physical data for works of type $type
${rdf.Prefixes.forQuery}			
construct {
?s rdfs:label ?label .
?s schema:description ?desc .
?s vad:hasPaperFinish ?finish .
?s vad:hasPaperWeight ?weight .
?s schema:dateCreated ?compdt .
?s schema:height ?len .
?s schema:width ?wid .
?s a ?type .
} {
		?s a ?type .
		FILTER ( ?type in (${type}))
		?s rdfs:label ?label .
		?s schema:description ?desc .
		?s vad:hasPaper ?paper .
		?s vad:hasPaperFinish ?finish .
		?s vad:hasPaperWeight ?weight .
		?s schema:dateCreated ?compdt .
		?s schema:height ?len .
		?s schema:width ?wid .
		}
"""
		sql
	}
		
	static def driverAll() {
		def sql = """
# A selection of all data
${rdf.Prefixes.forQuery}
SELECT ?s ?p ?o {
		?s ?p ?o .
		# FILTER ( !isBlank(?s))
		} order by ?s
"""
		sql
	}
	
	static def driverKind(kind) {
		def sql = """
${rdf.Prefixes.forQuery}			
SELECT ?s ?p ?o {
		?s ?p ?o .
		?s a ?type .
		FILTER ( ?type in ($kind))
		} order by ?s
"""
		sql
	}


}
