package rdf

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.util.RCode

class QueryRewrite {
	def prefixes ="""
prefix dct: <http://purl.org/dc/terms/> 
prefix foaf: <http://xmlns.com/foaf/0.1/> 
prefix owl:   <http://www.w3.org/2002/07/owl#> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
prefix schema: <https://schema.org/> 
prefix skos: <http://www.w3.org/2004/02/skos/core#> 
prefix tko:   <http://visualartsdna.org/takeout#> 
prefix vad: <http://visualartsdna.org/2021/07/16/model#> 
prefix work:	<http://visualartsdna.org/work/> 
prefix xs: <http://www.w3.org/2001/XMLSchema#> 
prefix z0:	<http://visualartsdna.org/system#> 
"""
	def query="""
describe ?s 
where {
?s a ?type .
filter (?type in (vad:Query,vad:QueryCollection))
}
"""
	def ju = new JenaUtils()
	
	// cfg folders, run test for update
	@Test
	void test() {
		def src = "C:/test/cwva/ttl/data"
		//def tgt = "C:/test/cwva/ttl/data/queries2.ttl"
		def tgt = "C:/temp/junk/queries3.ttl"
		update(src,tgt)
	}

	// Roundtrip through ttl and json-ld
	// to update identifiers
	/*
	 * param src, model data
	 * param tgt, the new queries ttl
	 */
	def update(src,tgt) {
		def bkup = "/temp/queriesBackup.ttl"
		new File("/temp/queriesBackup.ttl") << new File("$src/queries.ttl").text
		println "backed up to $bkup"
		
		def m0 = ju.loadFiles(src)
		def m1 = ju.queryDescribe(m0,prefixes ,query )

		def ld = ju.saveModelString(m1, "JSON-LD")
		//println ld
		def c = new JsonSlurper().parseText(ld)

		def hl = []
		c["@graph"].findAll {m->
			"vad:Query" in m["@type"]
		}.each { m->
			def oldid = m["identifier"]
			def dc = m["query"].hashCode()
			def hc = new RCode().dec2hex2(dc )
			if (oldid != hc) 
				println "identifier/hc mismatch old=$oldid, new=$hc"
			hl += hc
			m["@id"] = "work:$hc"
			m["identifier"] = hc
			assert m["description"], "query description is missing"
			assert m["description"].trim() !="", "query description is blank"
			
		}
		def m9=c["@graph"].find {m->
			"vad:QueryCollection" in m["@type"]
		}
		def l = []
		hl.each {
			l += "work:$it"
		}
		m9["member"] = l

		def ld2 = new JsonOutput().toJson(c)
		def m2 = ju.saveStringModel(ld2,"JSON-LD")

		def s0 = new JenaUtils().saveModelString(m2)
		// change quotes to triple quotes
		def s1 = s0.replaceAll(/(?<!\\)"(.*)?(?<!\\)"/,"\"\"\"\$1\"\"\"")
		// de-jsonize/unescape text to preserve query
		def s2 = org.apache.commons.text.StringEscapeUtils.unescapeJava(s1)
		new File(tgt).text = s2
	}

	@Test
	void test1() {
		def m0 = new JenaUtils().loadFiles("C:/test/cwva/ttl/data")
		def m = new JenaUtils().queryDescribe(m0,prefixes ,query )

		// update model schema:identifier   "123456"
		def m1 = ju.queryExecUpdate(m,prefixes, """
DELETE { ?s schema:identifier ?o }
INSERT { ?s schema:identifier ?o2 }
WHERE
  { 
		bind("987654" as ?o2)
		?s schema:identifier ?o
		filter (?o = "123456")
  }
""")

		// update model work:123456

		println new JenaUtils().saveModelString(m)
	}
	@Test
	void test0() {
		def m0 = new JenaUtils().loadFiles("C:/test/cwva/ttl/data")
		def m = new JenaUtils().queryDescribe(m0,prefixes ,query )
		println new JenaUtils().saveModelString(m)
	}

}
