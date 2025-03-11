package misc

import static org.junit.jupiter.api.Assertions.*
import groovy.io.FileType

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test

class TestJArchivoCvt {
	
	def lfilter = [
		"domOSCommonOntology.json",
		"TheOntologyOfObstetricAndNeonatalDomain.json",
		"RadiationBiologyOntology.json",
		"PATO-ThePhenotypeAndTraitOntology.json",
		"META-SHAREOntology.json",
		"IDSInformationModel.json",
		"GenomicsCohortsKnowledgeOntology.json",
		"CellOntology.json",
		"BIBFRAMEVocabulary.json"
		]

	@Test
	void test() {
		def ttl = toTtl("/test/images/archivo/json")
		new File("/test/images/archivo/archivo.ttl").text = ttl
		new File("C:/stage/tmp/archivo.ttl").text = ttl
	}
	
	// BEWARE the filename PokémonOntology.jpg -> PokemonOntology
	def toTtl(fname) {
		def ttl= """
# Generated ${new Date()}
${rdf.Prefixes.forFile}
"""

		def dir = new File(fname)
		dir.eachFileRecurse (FileType.FILES) { file ->
			
			// filter
			if (!(file.name in lfilter)) return
			
			
			def col = new JsonSlurper().parse(file)
			def s = (col[5].graph.rules =~ /x=@\(([A-Za-z0-9\:\- _,\.]+)\)/)[0][1]
			def gf = s.split(", ")
			def date = gf[0].split(" ")[0]
			def time = gf[0].split(" ")[1]
			def guid = gf[1]
			
			def border = (col[0].border =~ /([0-9]+)/)[0][1] as int
			def height = (col[0].yspan =~ /([0-9]+)/)[0][1] as int
			def width = (col[0].xspan =~ /([0-9]+)/)[0][1] as int
			height += border
			width += border
			
			def desc = ""
			try {
				desc = (col[3].graph.rules =~ /x=@\(([A-Za-z0-9\:\- _,\.]+)\)/)[0][1]
				
			} catch (Exception e) {
				return
			}
			def imageFile = file.name - ~/\.\w+$/
			ttl += """
work:$guid
	rdfs:label "${desc}" ;
		a vad:LindenmayerSystemImage ;
	vad:media	"Digital Imagery" ;
	schema:height      "$height"^^xs:float ;
	schema:width       "$width"^^xs:float ;
	schema:identifier        "$guid" ;
	schema:description "$desc" ;
	schema:datePublished "2025-03-04T08:02:34"^^xs:dateTime ;
	schema:dateCreated "${date}T${time}"^^xs:dateTime ;
	schema:image <http://visualartsdna.org/images/${imageFile}.jpg> ;
	.
"""
		}
		ttl
	}

	@Test
	void test0() {

		def dir = new File("/test/images/archivo/json")
		dir.eachFileRecurse (FileType.FILES) { file ->
			def col = new JsonSlurper().parse(file)
			[3,5].each{n->
			def m = col[n]
			def s = m.graph.rules
			def desc = (s =~ /x=@\(([A-Za-z0-9\:\- _,\.]+)\)/)[0][1]
			println desc
			}
		}
	}

}
