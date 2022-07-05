package support

import static org.junit.jupiter.api.Assertions.*

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test
import rdf.JenaUtils

class ParseQuery {

	static def dir="/stage/temp"
		
	@Test
	void test() {
		fail("Not yet implemented")
	}
	
	def parse(query) {
		
		//println "$path\n$query"
		def al = query.split(/&/)
		def m=[:]
		al.each {
			def av = it.split(/=/)
			if (av.size()==2)
				m[av[0]]=java.net.URLDecoder.decode(av[1], StandardCharsets.UTF_8.name())
			 }
		m.each {k,v->
			println "$k = $v"
		}
		verify(m)
		def ttl = printTtl(m)
		println ttl
		dir=m.dir
		new File("$dir/${m.guid}.ttl").text = ttl
		
		printQRC(m.guid)
		
		def mdl = new JenaUtils().loadFiles("$dir/${m.guid}.ttl")
		"triples: ${mdl.size()}"

	}
	def printQRC(guid) {
		new QRCode().qrcode(guid,dir)
	}

	def printTtl(m) {
		"""
# Generated ${new Date()}
${rdf.Prefixes.forFile}
${m.id}
	a vad:${m.type} ;
	skos:label "${m.label}" ;
	vad:media	${getList(m,"media")} ;
	vad:height      "${m.height}"^^xs:float ;
	vad:width       "${m.width}"^^xs:float ;
	vad:guid        "${m.guid}" ;
	vad:location "${m.location}" ;
	vad:description "${m.description}" ;
	skos:note "${m.note}" ;
	vad:recordDateTime "${m.recordedDateTime}"^^xs:dateTime ;
	vad:completedDateTime "${m.completedDateTime}"^^xs:dateTime ;
	vad:hasPaper "${m.hasPaper}" ;
	vad:hasPaperFinish "${m.hasPaperFinish}" ;
	vad:hasPaperWeight "${m.hasPaperWeight}"^^xs:int ;
	vad:workOnSite <${m.workOnSite}> ;
	vad:hasArtistProfile ${m.hasArtistProfile} ;
	vad:qrcode <${m.qrcode}> ;
	vad:image <http://visualartsdna.org/images/${m.fileupload}> ;
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
	assert m.location           , "no m.location         "
	assert m.description        , "no m.description      "
	assert m.note               , "no m.:note            "
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
