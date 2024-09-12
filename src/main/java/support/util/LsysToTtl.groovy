package support.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtils
import groovy.io.FileType
import groovy.json.JsonSlurper
import services.HtmlForm2Ttl
import java.nio.*
import java.nio.file.*
import support.ImageMgt

class LsysToTtl {

	def printPrefixes() {
		"""
# Generated ${new Date()}
${rdf.Prefixes.forFile}
"""
		
	}
	def printTtl(m) {
		"""
work:${m.guid}
	a vad:${m.type} ;
	rdfs:label "${m.label}" ;
	vad:media	"${m.media}" ;
	vad:height      "${m.height}"^^xs:float ;
	vad:width       "${m.width}"^^xs:float ;
	vad:guid        "${m.guid}" ;
	vad:description "${m.description}" ;
	skos:note "${m.note}" ;
	vad:recordDateTime "${m.recordedDateTime}"^^xs:dateTime ;
	vad:completedDateTime "${m.completedDateTime}"^^xs:dateTime ;
	vad:fileFormat "${m.fileFormat}" ;
	vad:workOnSite <${m.workOnSite}> ;
	vad:artist ${m.artist} ;
	vad:hasArtistProfile ${m.hasArtistProfile} ;
	vad:qrcode <http://visualartsdna.org/images/qrc_${m.guid}.jpg> ;
	vad:image <http://visualartsdna.org/images/${m.fileupload.replaceAll(" ","%20")}> ;
	.
"""
		
	}
	
	// TODO: copy graphic jpg from origin to outdir

	@Test
	void testDigBase() {
		def dir = "C:/test/nalsGallery/tratsiBase"
		def file = "digitalBase.ttl"
		def outdir = "C:/stage/data"
		printTtl(dir,outdir,file)
		moveFiles(dir,outdir)
	}
	
	@Test
	void testDig() {
		def dir = "C:/test/nalsGallery/tratsi"
		def file = "digital.ttl"
		def outdir = "C:/stage/data"
		printTtl(dir,outdir,file)
		moveFiles(dir,outdir)
	}	
	def printTtl(dir,outDir,file) {
		new File("$outDir/$file").text = printPrefixes()
		def lr = extract(dir)
		lr.each {
			 new File("$outDir/$file").append printTtl(it)
			 ImageMgt.qrcode(it.guid,outDir)
		}
		def l = getJson(dir)
		def mdl = new JenaUtils().loadFiles("$outDir/$file")
		println "triples: ${mdl.size()}"

	}
	
	def moveFiles(dir,outDir) {
		// move files
		// TODO: refactor
		def l2 = getFiles(dir,".jpg")
		l2.each{
			def name = it.name
			def src = Paths.get("$it")
			def dest = Paths.get("$outDir/$name")
			Files.copy(src, dest)
		}
	}
	

	def baseMap = [
		type : "vad:LindenMayerSystemImage",
		label : ""                         ,
		media : "Graphics"                 ,
		height : ""                        ,
		width : ""                         ,
		description : ""                   ,
		note : ""                          ,
		recordedDateTime : ""              ,
		completedDateTime : ""             ,
		fileFormat : "JPEG"                    ,
		workOnSite : "https://tratsi314.wixsite.com/tra-tsi-art"                    ,
		hasArtistProfile : "work:6a94c8c0-34bf-4e56-991a-15b3600d05e7"              ,
		artist : "work:c35a3e70-7a05-4844-81b7-70d7a537797d"                        ,
		qrcode : ""                        ,
		]

	def extract(dir) {
		def lr = []
		def l = getJson(dir)
		l.each{
			//println it
			def jm = new JsonSlurper().parse(it)
			def rm=baseMap.clone()
			lr << rm
			
			def config = jm.find{m->
				m.type == "config"
			}
			rm.height = config.yspan
			rm.width = config.xspan
			
			def legend3 = jm.find{m->
				m.name == "legend3"
			}
			rm.completedDateTime = (legend3.graph.rules =~ /x=@\(([0-9\-]+ [0-9\:]+),.*/)[0][1]
			rm.completedDateTime = rm.completedDateTime.replaceAll(" ","T")
			def legend = jm.find{m->
				m.name == "legend"
			}
			rm.fileupload = "${(it.name =~ /(.*).json/ )[0][1]}.jpg"
			
			rm.guid = legend.id.substring("http://visualartsdna.org/work/".size())
			
			
			// ns:x=@(Namespace: http://schema.org)]
			rm.ns = (legend.graph.rules =~ /.*@\(Namespace: (.*)\)/ )[0][1]

			def graph = jm.find{m->
				m.name == "generated from TTL file"
			}
			rm.enhanced = !graph.graph.rules.contains(",f = ff,X=X")

			if (rm.enhanced) {
				rm.label = "Enhanced NALS graph of the ${rm.ns} ontology"
				rm.description = "Generated with Nodes and Arcs Lindenmayer System. Enhanced with fractal-plant rules."
			} else {
				rm.label = "NALS graph of the ${rm.ns} ontology"
				rm.description = "Generated with Nodes and Arcs Lindenmayer System."
			}
			rm.note = "${rm.ns}"
			rm.recordedDateTime = HtmlForm2Ttl.getNow()
		}
		lr
	}

	// gets json files recursively?
	def getJson(dir) {
		getFiles(dir,".json")
	}

	// gets json files recursively?
	def getFiles(dir,ext) {
		def l=[]
		new File(dir).eachFileRecurse(FileType.FILES){
			if (it.name.endsWith(ext))
				l << it
		}
		l
	}

	@Test
	void test3() {
		//def dir = "C:/test/nalsGallery/tratsi"
		def dir = "C:/test/nalsGallery/tratsiBase"
		def lr = extract(dir)
		lr.each { println it }

	}

	@Test
	void test2() {
		def lr = []
		def dir = "C:/test/nalsGallery/tratsiBase"
		def l = getJson(dir)
		l.each{
			println it
			def jm = new JsonSlurper().parse(it)
			def rm=[:]
			lr << rm
			def legend = jm.find{m->
				m.name == "legend"
			}

			rm.id = legend.id.substring("http://visualartsdna.org/work/".size())
			// ns:x=@(Namespace: http://schema.org)]
			rm.ns = (legend.graph.rules =~ /.*@\(Namespace: (.*)\)/ )[0][1]
		}
		lr.each { println it }
	}

	@Test
	void test1() {
		def dir = "C:/test/nalsGallery/tratsiBase"
		def l = getJson(dir)
		l.each{
			println it
		}
	}

	@Test
	void test0() {
		def dir = "C:/test/nalsGallery/tratsiBase"
		def l=[]
		new File(dir).eachFile(FileType.FILES){
			if (it.name.endsWith(".json"))
				l << it
		}
		l.each{
			println it
		}
	}

}
