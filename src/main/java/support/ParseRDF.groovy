package support

import static org.junit.jupiter.api.Assertions.*

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test
import rdf.JenaUtils

class ParseRDF {

	static def dir="/stage/data"
		
	@Test
	void test() {
		fail("Not yet implemented")
	}
	
	def parse(m) {
		
		verify(m)
		def filename = ""
		if (m.sig in ["right","left"]) {
			
			filename = ImageMgt.makeStampedFile(m.guid,m.fileupload,m.label,m.dir,m.sig)
		}
		else {
		filename = ImageMgt2.makeStampedFile(
			m.guid,
			m.fileupload,
			m.label,
			m.dir,
			m.sig)
	}
		m.fileupload = filename
		def ttl = printTtl(m)
		println ttl
		//dir=m.dir
		new File("${m.dir}/${m.guid}.ttl").text = ttl
		
		printQRC(m.guid)
		
		def mdl = new JenaUtils().loadFiles("${m.dir}/${m.guid}.ttl")
		"triples: ${mdl.size()}"

	}
	def printQRC(guid) {
		ImageMgt.qrcode(guid,dir)
	}

	def printTtl(m) {
		"""
# Generated ${new Date()}
${rdf.Prefixes.forFile}
${m.id}
	a vad:${m.type} ;
	rdfs:label "${m.label}" ;
	vad:media	${getList(m,"media")} ;
	schema:height      "${m.height}"^^xs:float ;
	schema:width       "${m.width}"^^xs:float ;
	schema:identifier        "${m.guid}" ;
	schema:location "${m.location}" ;
	schema:description "${m.description}" ;
	skos:note "${m.note}" ;
	schema:datePublished "${m.recordedDateTime}"^^xs:dateTime ;
	schema:dateCreated "${m.completedDateTime}"^^xs:dateTime ;
	vad:hasPaper "${m.hasPaper}" ;
	vad:hasPaperFinish "${m.hasPaperFinish}" ;
	vad:hasPaperWeight "${m.hasPaperWeight}"^^xs:int ;
	vad:workOnSite <${m.workOnSite}> ;
	vad:hasArtistProfile ${m.hasArtistProfile} ;
	vad:qrcode <${m.qrcode}> ;
	schema:image <http://visualartsdna.org/images/${m.fileupload}> ;
	.
"""
	}
	
	def verify(m) {
	assert m.type				, "no m.type			 "	
	assert m.label				, "no m.label			 "	
	assert m.media1|| m.media2|| m.media3|| m.media4            , 
	"no m.media            "
	assert m.height             , "no m.height           "
	assert m.width              , "no m.width            "
	assert m.guid               , "no m.guid             "
	//assert m.location           , "no m.location         "
	assert m.description        , "no m.description      "
	//assert m.note               , "no m.:note            "
	assert m.recordedDateTime   , "no m.recordedDateTime "
	assert m.completedDateTime  , "no m.completedDateTime"
	assert m.hasPaper           , "no m.hasPaper         "
	assert m.hasPaperFinish     , "no m.hasPaperFinish   "
	assert m.hasPaperWeight     , "no m.hasPaperWeight   "
	assert m.workOnSite         , "no m.workOnSite       "
	assert m.hasArtistProfile   , "no m.hasArtistProfile "
	assert m.qrcode             , "no m.qrcode           "
	assert m.fileupload         , "no m.fileupload       "
	}
	
	def getList(m,key) {
		def l = []
		m.each { k,v->
			if (k.startsWith(key)) {
				l += v
			}
		}
		def s=""
		int i=0
		l.each{
			if (i++) s += ","
			s += "'$it'"
		}
		s
	}
	

}
