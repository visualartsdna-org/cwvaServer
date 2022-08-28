package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test
import rdf.JenaUtils

class ViaToTtl {

	@Test
	void test() {
		def base = "C:/test/via/json"

		def m = new JsonSlurper().parse(new File("$base/via_waterfallStudy.json"))
		//m.each{k,v-> println "$k=$v"}

		def s = process(m)
		new File("test.ttl").text = s
		println s
		def md = new JenaUtils().loadFiles("test.ttl")
		println md.size()
		new JenaUtils().saveModelFile(md,"C:/temp/git/cwvaContent/ttl/data/study/project2.ttl","TTL")
	}

	def domain = "http://localhost:8080"
	def process(m) {

		def ju = new JenaUtils()
		def dt = new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ssX" ).format(new Date())
		def sb = new StringBuilder()
		sb.append rdf.Prefixes.forFile
		sb.append """
work:${UUID.randomUUID()}
	a vad:Project ;
	rdfs:label "${m._via_settings.project.name}" ;
	schema:datePublished "${dt}"^^xs:dateTime ;
	skos:definition "A VIA study named ${m._via_settings.project.name}" ;
	vad:viaProject [
		a vad:ViaProject ;
		rdfs:label "VIA project" ;

		schema:name "${m._via_settings.project.name}.json" ;
	] ;
	the:design  
"""
		int j=0
		m["_via_img_metadata"].each{k,v->

			def work = m["_via_img_metadata"][k].file_attributes.work
			def filename = m["_via_img_metadata"][k].filename
			sb.append """
	${j++>0?",":""}
	[
		a vad:ViaImageAnnotation ;
		rdfs:label "image annotation" ;
		schema:image <${domain + "/images/study/"+ filename}> ;
		vad:filename "${filename}" ;
		${work?"the:work work:$work ;":""
				}
		the:member"""
		int i=0
		m["_via_img_metadata"][k].regions.each{r->

			def tags = ""
			if (r["region_attributes"].type!="none") tags += r["region_attributes"].type
			if (r["region_attributes"].type2!="none") tags += " ,"+ r["region_attributes"].type2
			if (r["region_attributes"].type3!="none") tags += " ,"+ r["region_attributes"].type3

			sb.append """${i++>0?",":""}
			[
			a vad:ViaRegionAnnotation ;
			vad:shape "rect" ;
			vad:x "${r["shape_attributes"].x}"^^xs:int ;
			vad:y "${r["shape_attributes"].y}"^^xs:int ;
			vad:width "${r["shape_attributes"].width}"^^xs:int ;
			vad:height "${r["shape_attributes"].height}"^^xs:int ;
			rdfs:label "${r["region_attributes"].label.trim()}" ;
			skos:definition "${r["region_attributes"].definition.trim()}" ;
			skos:note "${r["region_attributes"].notes.trim()}" ;
			${tags=="" ? "" :"the:tag "+tags + " ;"} 
			]
"""
		}

		sb.append """
		]"""
	}
	sb.append "."
	""+sb
}
}
