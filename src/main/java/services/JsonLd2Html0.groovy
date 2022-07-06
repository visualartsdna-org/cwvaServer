package services

import static org.junit.Assert.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import rdf.JenaUtils

import org.junit.Test

class JsonLd2Html0 {

	def testPath = "C:/stage/february2022/node/ttl"
	@Test
	public void test() {
		def src = "$testPath/data"
		def domain="http://visualartsdna.org"
		def ns="work"
		def base="/work/ce9dfb4a-afdf-497a-a12f-c22f736df3f6"
		def path=parsePath(base)
		def guid=parseGuid(base)
		def s = new JsonLd2Html0().process(src,domain,path,ns,guid)
		println s
	}

	@Test
	public void test3() {
		def domain="http://visualartsdna.org"
		def path="work/6360c068-de32-4917-80a3-0dfd8b0175c9"
		def ns="work"
		def guid="6360c068-de32-4917-80a3-0dfd8b0175c9"
		def s = new JsonLd2Html0().process("/temp/data",domain,path,ns,guid)
		println s
	}

	@Test
	public void test2() {
		def s = new JsonLd2Html0().parseGuid("/work/6360c068-de32-4917-80a3-0dfd8b0175c9")
		println s
	}
	
	@Test
	public void test1() {
		def s = new JsonLd2Html0().parsePath("/work/6360c068-de32-4917-80a3-0dfd8b0175c9")
		println s
	}
	
	def parsePath(path) {
		(path =~ /^\/(.*)$/)[0][1]
	}

	def parseGuid(path) {
		(path =~ /^\/work\/([0-9A-Fa-f\-]+)$/)[0][1]
	}
	
	def getOneInstanceModel(m,ns,guid) {
		// work:6360c068-de32-4917-80a3-0dfd8b0175c9
		new JenaUtils().queryDescribe(m, """
prefix vad:	<http://visualartsdna.org/2020/04/painting/>
prefix work:	<http://visualartsdna.org/work/>
""", """
describe ${ns}:${guid}
"""
)

	}

	def process(ttl, domain,path, ns, guid, host) {
		File temp = File.createTempFile("jsontohtml", ".json-ld")
		temp.deleteOnExit()
		def m = new JenaUtils().loadDirModel(ttl)
		def m2 = getOneInstanceModel(m,ns,guid)

		m2.write(temp.newOutputStream(),"JSON-LD")
		def map = new JsonSlurper().parse(temp)
		
		buildTables(map["@context"])
		
		def s = printHtml(map, domain,path, ns, guid, host)
		
	}

	// Under the About: title, add any label, e.g., scos:label
	def printHtml(m, domain,path, ns, guid, host) {
		def sb = new StringBuilder()
		sb.append HtmlTemplate.head(host)
		sb.append HtmlTemplate.title(
			"${domain}/${path}",
			"${ns}:${guid}"
			) 
		sb.append HtmlTemplate.tableHead("Property","Value")
			
		printHtml(m,sb,host)
		
		sb.append HtmlTemplate.tableTail
		sb.append HtmlTemplate.tail
		return ""+sb
	}
	def printHtml(m,sb,host) {
		if (m instanceof Map) {
			m.each{k,v->
//				println "here"
				if (k=="@context") return
				if (k=="@graph") {
//					println "here"
					v.each{
						printHtml(it,sb,host)
					}
					return
				}
				def m2=defs[k]
				if (k=="@id") 
					sb.append """<tr height="50"><td>ID</td><td><a href="${nsLu(v)}">$v</a></td></tr>\n"""
				else if (k=="@type") 
					sb.append """<tr height="50"><td>type</td><td><a href="${nsLu(v)}">$v</a></td></tr>\n"""
//				else if (k=="hasArtistProfile") 
//					sb.append """<tr height="50"><td>$k</td><td><a href="${nsLu(v)}">$v</a></td></tr>\n"""
				else if (k in [
					"artist",
					"hasArtistProfile",
					"pseudonymFor",
					"maker",
					]) { 
					def uv0 = v.replaceAll(/^work:/,"http://visualartsdna.org/work/")
					def uv = uv0.replaceAll("http://visualartsdna.org",host)
					sb.append """<tr height="50"><td>$k</td><td><a href="${uv}">$v</a></td></tr>\n"""
				}
				else if (k=="image") {
					def uv = v.replaceAll("http://visualartsdna.org",host)
					sb.append """<tr height="50"><td>$k</td><td><a href="$uv"><img src="$uv" width="500"></a></td></tr>\n"""
				}
				else if (k=="qrcode") {
					def uv = v.replaceAll("http://visualartsdna.org",host)
					sb.append """<tr height="50"><td>$k</td><td><a href="$uv"><img src="$uv" width="100"></a></td></tr>\n"""
				}
				else {
					def type=m2["@type"]
					if (type=="@id"
						&& k != "mailto") {
						//def m2=defs[k]
						def id=m2["@id"]
						sb.append """<tr height="50"><td>$k</td><td><a href="${v}">$v</a></td></tr>\n"""
					}
					else {
						sb.append """<tr height="50"><td>$k</td>"""
						printHtml(v,sb,host)
					}
				}
			}
		}
		else if (m instanceof List) {
			int i=0
			m.each {
				if (i++)
					sb.append """<tr height="50"><td></td>"""
				printHtml(it,sb,host)
			}
		}
		else 
			sb.append "<td>$m</td></tr>\n"
		
	}
	
	def ns = [:]
	def defs = [:]
	def buildTables(cm) {
		cm.each{k,v->
			def m = [:]
			if (v instanceof Map) {
				v.each{k1,v1->
					m[k1] = v1
				}
				defs["$k"] = m
			}
			else ns[k]=v
		}
		//defs["@graph"]=[]
	}
	
	def nsLu(s) {
		def pre = (s =~ /([a-z_]+):(.*)/)[0]
		"${ns[pre[1]]?:"_:"}${pre[2]}"
		
	}
	@Test
	public void test0() {
		println nsLu("work:6360c068-de32-4917-80a3-0dfd8b0175c9")
	}
	def printMap(m) {
		printMap(m,0)
	}
	def printMap(m,t) {
		for (int i=0;i<t;i++) {
			print "  "
		}
		if (m instanceof Map) {
			m.each{k,v->
				println "$k"
				printMap(v,t+1)
			}
		}
		else if (m instanceof List) {
			m.each {
				printMap(it,t+1)
			}
		}
		else 
			println "$m"
		
	}
}
