package services

import static org.junit.Assert.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import rdf.JenaUtils
import rdf.QuerySupport
import org.junit.Test

class JsonLd2Html2 {

	def ns = [:]
	def defs = [:]
	
	// html for anartist from queries
	@Test
	public void testArtist() {
		def host = "test"
		def src = "C:/test/cwva/ttl/data"
		def domain="http://visualartsdna.org"
		def ns="work"
		def base="/work/c35a3e70-7a05-4844-81b7-70d7a537797d"
		//def base="/work/ce9dfb4a-afdf-497a-a12f-c22f736df3f6"
		def path=parsePath(base)
		def guid=parseGuid(base)
		def s = new JsonLd2Html2().process(src,domain,path,ns,guid,host)
		println s
	}

	// html for a work from queries
	@Test
	public void testWork() {
		def host = "test"
		def src = "C:/test/cwva/ttl/data"
		def domain="http://visualartsdna.org"
		def ns="work"
		def base="/work/d8554014-f473-40ac-9a4e-363ac733ab06"
		//def base="/work/ce9dfb4a-afdf-497a-a12f-c22f736df3f6"
		def path=parsePath(base)
		def guid=parseGuid(base)
		def s = new JsonLd2Html2().process(src,domain,path,ns,guid,host)
		println s
	}

	def parsePath(path) {
		(path =~ /^\/(.*)$/)[0][1]
	}

	def parseGuid(path) {
		(path =~ /^\/work\/([0-9A-Fa-f\-]+)$/)[0][1]
	}
	
	def pfxNsMap
	def host
	def nsLookup(s) {
		def sl = s.split(":")
		def r="${pfxNsMap[sl[0]]}${sl[1]}"
		def r2 = r.replaceAll("http://visualartsdna.org",host)
		println "\tin: $s, out: $r2"
		r2
	}

	def process(ttl, domain,path, ns, guid, host) {
		def qs = new QuerySupport(ttl)
		pfxNsMap = qs.getPrefixNsMap()
		this.host = host
		def scls = qs.getType(ns,guid)
		def lmdl = qs.queriesByType("<$scls>",ns,guid)
		
		def ljld = []
		lmdl.each { m->
			def baos = new ByteArrayOutputStream()
			m.write(baos,"JSON-LD")
			ljld += new JsonSlurper().parseText(""+baos)
			
		}
		def s = printHtml(ljld, domain,path, ns, guid, host)
	}

	// Under the About: title, add any label, e.g., scos:label
	def printHtml(ljld, domain,path, ns, guid, host) {
		def sb = new StringBuilder()
		sb.append HtmlTemplate.head(host)
		sb.append HtmlTemplate.title(
			"${domain}/${path}",
			"${ns}:${guid}"
			) 
			
			ljld.each{map->
				ns = [:]
				defs = [:]
				buildTables(map["@context"])
				//println map
				sb.append HtmlTemplate.tableHead("Property","Value")
				printHtml(map,sb,host)
				sb.append HtmlTemplate.tableTail
			}
			
		sb.append HtmlTemplate.tail
		return ""+sb
	}
	def printHtml(m, sb, host) {
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
				else if (k=="@type") {
					//sb.append """<tr heght="50"><td>type</td><td><a href="${nsLu(v)}">$v</a></td></tr>\n"""
					def vc = v instanceof List ? v : [v]
					vc.each{ 
						def uv0 = it.replaceAll(/^work:/,"http://visualartsdna.org/work/")
						def uv = uv0.replaceAll("http://visualartsdna.org",host)
						sb.append """<tr height="50"><td>$k</td><td><a href="${nsLookup(uv)}">$it</a></td></tr>\n"""
					}
				}
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
						def vc = v instanceof List ? v : [v]
						vc.each{ 
							def uv0 = it.replaceAll(/^work:/,"http://visualartsdna.org/work/")
							def uv = uv0.replaceAll("http://visualartsdna.org",host)
							sb.append """<tr height="50"><td>$k</td><td><a href="${uv}">$it</a></td></tr>\n"""
						}
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
}
