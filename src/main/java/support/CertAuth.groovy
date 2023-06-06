package support

import static org.junit.Assert.*

import cwva.Server
import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import rdf.JenaUtils
import rdf.QuerySupport
import org.junit.Test

class CertAuth {

	def ns = [:]
	def defs = [:]
	
	// certificate of authenticity for a work
	@Test
	public void testWork() {
		def guid = "58d34da1-0201-46d2-9675-e10eda536893"
		//def guid = "d8554014-f473-40ac-9a4e-363ac733ab06"
		
		def host = "test"
		def src = "C:/test/cwva/ttl/data"
		def mdl = "C:/test/cwva/ttl/model/model.ttl"
		def domain="http://visualartsdna.org"
		def ns="work"
		def base="/work/$guid"
		//def base="/work/ce9dfb4a-afdf-497a-a12f-c22f736df3f6"
		def path=parsePath(base)
		//def guid=parseGuid(base)
		def rdfs = new Server().dbm.rdfs
		def s = new CertAuth().process(rdfs,domain,path,ns,guid,host)
		new File("/temp/html/ca.html").text = s
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
		def mmdl = qs.queryCertAuth(ns,guid)
		def label = mmdl.label
		mmdl.remove("label")
		mmdl.each{k,m->
			desc += k
			def baos = new ByteArrayOutputStream()
			m.write(baos,"JSON-LD")
			ljld += new JsonSlurper().parseText(""+baos)

		}
		def s = printHtml(ljld, desc, domain,path, ns, guid, label)
	}

	// Under the About: title, add any label, e.g., scos:label
	def printHtml(ljld, desc, domain,path, ns, guid, label) {
		def sb = new StringBuilder()
		//sb.append CertAuthTemplate.head(host)
		sb.append """
<h2 id="title">
<center>Certificate of Authenticity</center>
<br/>
<br/>
<a href="${domain}/${path}">$label</a> 
</h2>
"""
			
			int i=0
			ljld.each{map->
				ns = [:]
				defs = [:]
				sb.append("""
<br/>
<h3>${desc[i++]}</h3>
""")
				buildTables(map["@context"])
				//println map
				sb.append CertAuthTemplate.tableHead("Property","Value")
				printHtml(map,sb)
				sb.append CertAuthTemplate.tableTail
			}
			
		sb.append """
<br/>
<br/>
<br/>
Signed: _________________________________
<br/>
<br/>
<br/>
Date: _______________________
<br/>
<br/>
"""
		
		sb.append CertAuthTemplate.tail
		
		return ""+sb
	}
	def printHtml(m, sb) {
		def ht = 20
		if (m instanceof Map) {
			m.each{k,v->
				
				if (k=="@context") return
				if (k=="@graph") {
					v.each{
						printHtml(it,sb)
					}
					return
				}
				def m2=defs[k]
				if (k=="@id") {
					if (!v.startsWith("_:b"))
					sb.append """<tr height="${ht}"><td>ID</td><td><a href="${nsLookup(v)}">$v</a></td></tr>\n"""
				}
				else 
					if (k=="@type") {
					def vc = v instanceof List ? v : [v]
					def s=""
					int i=0
					vc.each{ 
						if (i++>0)	s += ", "
						s += """<a href="${nsLookup(it)}">$it</a>"""
					}
					sb.append """<tr height="${ht}"><td>$k</td><td>$s</td></tr>\n"""
				}
//				else if (k=="image") {
//					sb.append """<tr height="${ht}"><td>$k</td><td><a href="${v}"><img src="${v}" width="400"></a></td></tr>\n"""
//				}
				else if (k=="qrcode") {
					sb.append """<tr height="${ht}"><td>$k</td><td><a href="${v}"><img src="${v}" width="100"></a></td></tr>\n"""
				}
				else if (isUri(nsLookup(v))) { 
					sb.append """<tr height="${ht}"><td>$k</td><td><a href="${nsLookup(v)}">$v</a></td></tr>\n"""
				}
				else {
					def type=m2["@type"]
					if (type=="@id"
						&& k != "mailto") {
						def id=m2["@id"]
						def vc = v instanceof List ? v : [v]
						vc.each{ 
							if (!it.startsWith("_:b"))
							sb.append """<tr height="${ht}"><td>$k</td><td><a href="${nsLookup(it)}">$it</a></td></tr>\n"""
						}
					}
					else {
						sb.append """<tr height="${ht}"><td>$k</td>"""
						printHtml(v,sb)
					}
				}
			}
		}
		else if (m instanceof List) {
			int i=0
			m.each {
				if (i++)
					sb.append """<tr height="${ht}"><td></td>"""
				printHtml(it,sb)
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
	}
	
}
