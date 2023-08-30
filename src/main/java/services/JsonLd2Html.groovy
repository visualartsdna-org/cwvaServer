package services

import static org.junit.Assert.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import rdf.JenaUtils
import rdf.QuerySupport
import org.junit.Test

class JsonLd2Html {

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
		def s = new JsonLd2Html().process(src,domain,path,ns,guid,host)
		println s
	}

	// html for a work from queries
	@Test
	public void testWork() {
		def host = "test"
		def src = "C:/test/cwva/ttl/data"
		def mdl = "C:/test/cwva/ttl/model/model.ttl"
		def domain="http://visualartsdna.org"
		def ns="work"
		def base="/work/d8554014-f473-40ac-9a4e-363ac733ab06"
		//def base="/work/ce9dfb4a-afdf-497a-a12f-c22f736df3f6"
		def path=parsePath(base)
		def guid=parseGuid(base)
		def s = new JsonLd2Html().process(src,mdl,domain,path,ns,guid,host)
		println s
	}

	def parsePath(path) {
		(path =~ /^\/(.*)$/)[0][1]
	}

	def parseGuid(path) {
		(path =~ /^\/work\/([0-9A-Fa-f\-]+)$/)[0][1]
	}
	
	def parseConcept(path) {
		(path =~ /^\/thesaurus\/([0-9A-Za-z\-_]+)$/)[0][1]
	}
	
	def pfxNsMap
	def host
	
	def nsLookup(s) {
		if (!(s instanceof String)) return s
		def sl = s.split(":")
		if (sl.size()!=2) return s
		if (sl[0]=="http"
			|| sl[0]=="https"
			|| sl[0]=="mailto"
			) return s
		def r="${pfxNsMap[sl[0]]}${sl[1]}"
		rehost(r)
	}
	def rehost(r) {
		r.replaceAll("http://visualartsdna.org",host)
	}
	
	def isUri(s) {
		if (!(s instanceof String)) return false
		s.startsWith("http://") || s.startsWith("https://")
	}

	def process(rdfs, domain,path, ns, guid, host) {
		pfxNsMap = rdfs.getNsPrefixMap()
		def qs = new QuerySupport(rdfs)
		this.host = host
		def ljld = []
		def desc = []
		def mmdl = qs.query(ns,guid)
		def label = mmdl.label
		mmdl.remove("label")
		mmdl.each{k,m->
			desc += k
			
//			println "\n$k"
//			println new JenaUtils().saveModelString(m,"ttl")
			//label
			def baos = new ByteArrayOutputStream()
			m.write(baos,"JSON-LD")
			ljld += new JsonSlurper().parseText(""+baos)

		}
		def s = printHtml(ljld, desc, domain,path, ns, guid, label)
	}

	// Under the About: title, add any label, e.g., scos:label
	def printHtml(ljld, desc, domain,path, ns, guid, label) {
		def sb = new StringBuilder()
		sb.append HtmlTemplate.head(host)
		sb.append """
<h3 id="title">
About:
<a href="${domain}/${path}">$label</a> 
</h3>
"""
			
			int i=0
			ljld.each{map->
				ns = [:]
				defs = [:]
				sb.append("""
<br/>
<h5>${desc[i++]}</h5>
<br/>
""")
				buildTables(map["@context"])
				//println map
				sb.append HtmlTemplate.tableHead("Property","Value")
				printHtml(map,sb)
				sb.append HtmlTemplate.tableTail
			}
			
		sb.append HtmlTemplate.tail
		return ""+sb
	}
	def printHtml(m, sb) {
		if (m instanceof Map) {
			m.each{k,v->
				
//				if (k=="contentUrl") {
//					println "here"
//				}

				if (k=="@context") return
				if (k=="@graph") {
					v.each{
						printHtml(it,sb)
					}
					return
				}
				def m2=defs[k]
				if (k=="@id") 
					sb.append """<tr height="50"><td>ID</td><td><a href="${nsLookup(v)}">$v</a></td></tr>\n"""
				else 
					if (k=="@type") {
					def vc = v instanceof List ? v : [v]
					def s=""
					int i=0
					vc.each{ 
//						if (it.contains("Thing")
//							|| it.contains("Resource")) return
						if (i++>0)	s += ", "
						s += """<a href="${nsLookup(it)}">$it</a>"""
					}
					sb.append """<tr height="50"><td>$k</td><td>$s</td></tr>\n"""
//					vc.each{ 
//						sb.append """<tr height="50"><td>$k</td><td><a href="${nsLookup(it)}">$it</a></td></tr>\n"""
//					}
				}
				else if (k=="image") {
					sb.append """<tr height="50"><td>$k</td><td><a href="${rehost(v)}"><img src="${rehost(v)}" width="500"></a></td></tr>\n"""
				}
				else if (k=="qrcode") {
					sb.append """<tr height="50"><td>$k</td><td><a href="${rehost(v)}"><img src="${rehost(v)}" width="100"></a></td></tr>\n"""
				}
				else if (isUri(nsLookup(v))) { 
					sb.append """<tr height="50"><td>$k</td><td><a href="${nsLookup(v)}">$v</a></td></tr>\n"""
				}
				else {
					def type=m2["@type"]
					if (type=="@id"
						&& k != "mailto") {
						def id=m2["@id"]
						def vc = v instanceof List ? v : [v]
						vc.each{ 
							sb.append """<tr height="50"><td>$k</td><td><a href="${nsLookup(it)}">$it</a></td></tr>\n"""
						}
					}
					else {
						sb.append """<tr height="50"><td>$k</td>"""
						printHtml(v,sb)
					}
				}
			}
		}
		else if (m instanceof List) {
			int i=0
			m.each {
				if (i++)
					sb.append """<tr height="50"><td></td>"""
				printHtml(it,sb)
			}
		}
		else 
			sb.append "<td>$m</td></tr>\n"
		
	}
	
	def buildTables(cm) {
		cm.each{k,v->
			//println "$k = $v"
			def m = [:]
			if (v instanceof Map) {
				v.each{k1,v1->
					//println "\t$k1 = $v1"
					m[k1] = v1
				}
				defs["$k"] = m
			}
			else ns[k]=v
			
		}
		//defs["@graph"]=[]
	}
	
}
