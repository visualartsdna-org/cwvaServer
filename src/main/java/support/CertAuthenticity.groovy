package support

import static org.junit.Assert.*

import cwva.Server
import org.apache.jena.rdf.model.Model
import java.nio.charset.StandardCharsets

import groovy.json.JsonSlurper
import rdf.JenaUtils
import rdf.QuerySupport
import org.junit.Test

class CertAuthenticity {

	def ns = [:]
	def defs = [:]
	def target = "/temp/html/ca.html"
	
	// certificate of authenticity for a work
	@Test
	public void testWork() {
		makeCert("394056a2-d79a-4395-a01f-79377241aacc")
	}
	
	// TODO: is there a more generic way to do this
	def handleUpload(query) {
		def dir = "/temp/html"
		def al = query.split(/&/)
		def m=[:]
		al.each {
			def av = it.split(/=/)
			if (av.size()==2)
				m[av[0]]=java.net.URLDecoder.decode(av[1], StandardCharsets.UTF_8.name())
			 }
//		m.each {k,v->
//			println "$k = $v"
//		}
		assert m.guid, "No GUID found"
		//def guid = m.guid // guid only!!
		def guid = (m.guid =~ /([a-f0-9\-]+)$/)[0][1]
		makeCert(guid)
		"file://$target"
	}



	def makeCert(guid) {
		def content = "/temp/git/cwvaContent"
		def host = "test"
		def domain="http://visualartsdna.org"
		def ns="work"
		def base="/work/$guid"
		def port = 80
		def path=parsePath(base)
		def rdfs = cwva.Server.getInstance().dbm.rdfs
		// for debugging
//		def rdfs = new cwva.Server(
//				port: port,
//				dir:"/temp/git/cwva",
//				cloud:[src:"ttl",tgt:content],
//				data: "$content/ttl/data",
//				vocab: "$content/ttl/vocab",
//				tags: "$content/ttl/tags",
//				model: "$content/ttl/model",
//				images: "$content/../../images",
//				domain: "http://visualartsdna.org" ,
//				ns: "work",
//				host: "http://192.168.1.71:$port",
//				verbose: true
//			).dbm.rdfs
	
		def s = new CertAuthenticity().process(rdfs,domain,path,ns,guid,host)
		new File(target).text = s
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
<h4 id="title">
<center>Certificate of Authenticity</center>
<br/>
<a href="${domain}/${path}">$label</a> 
</h4>
"""
			
			int i=0
			ljld.each{map->
				ns = [:]
				defs = [:]
//				sb.append("""
//<br/>
//<h4>${desc[i++]}</h4>
//""")
				buildTables(map["@context"])
				//println map
				sb.append CertAuthTemplate.tableHead("Property","Value")
				printHtml(map,sb)
				sb.append CertAuthTemplate.tableTail
			}
			
		sb.append """
<br/>
<br/>
Signed: _________________________________
<br/>
<br/>
Date: _______________________
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
					if (vc.contains("vad:CreativeWork")) // simplify type
						vc = ["vad:CreativeWork"]
					def s=""
					int i=0
					vc.each{ 
						if (i++>0)	s += ", "
						s += """<a href="${nsLookup(it)}">$it</a>"""
					}
					sb.append """<tr height="${ht}"><td><i>$k</i></td><td>$s</td></tr>\n"""
				}
				else if (k=="image") {
					sb.append """<tr height="${ht}"><td><i>$k</i></td><td><a href="${v}"><img src="${v}" width="50"></a></td></tr>\n"""
				}
				else if (k=="qrcode") {
					sb.append """<tr height="${ht}"><td><i>$k</i></td><td><a href="${v}"><img src="${v}" width="100"></a></td></tr>\n"""
				}
				else if (isUri(nsLookup(v))) { 
					sb.append """<tr height="${ht}"><td><i>$k</i></td><td><a href="${nsLookup(v)}">$v</a></td></tr>\n"""
				}
				else {
					def type=m2["@type"]
					if (type=="@id"
						&& k != "mailto") {
						def id=m2["@id"]
						def vc = v instanceof List ? v : [v]
						vc.each{ 
							if (!it.startsWith("_:b"))
							sb.append """<tr height="${ht}"><td><i>$k</i></td><td><a href="${nsLookup(it)}">$it</a></td></tr>\n"""
						}
					}
					else {
						sb.append """<tr height="${ht}"><td><i>$k</i></td>"""
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
