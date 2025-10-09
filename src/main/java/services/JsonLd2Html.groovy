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
	def qs
	def pfxNsMap
	def host
	
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

	def parseClass(path) {
		(path =~ /^\/model\/([0-9A-Za-z\-_]+)$/)[0][1]
	}
	
	
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
		if (r instanceof List) {
			def r2 = []
			r.each{
				r2 += it.replaceAll("http://visualartsdna.org",host)
			}
			return r2
		} else
		r.replaceAll("http://visualartsdna.org",host)
	}
	
	def isUri(s) {
		if (!(s instanceof String)) return false
		s.startsWith("http://") || s.startsWith("https://")
	}

	def process(rdfs, domain,path, ns, guid, host) {
		pfxNsMap = rdfs.getNsPrefixMap()
		if (!qs) qs = new QuerySupport(rdfs)
		this.host = host
		def ljld = []
		def desc = []
		def mmdl = qs.query(ns,guid)
		def label = mmdl.label
		mmdl.remove("label")
		mmdl.each{k,m->
			desc += k
			
			def baos = new ByteArrayOutputStream()
			m.write(baos,"JSON-LD")
			ljld += new JsonSlurper().parseText(""+baos)
				

		}
		def tm = new TreeMap<>(ljld[0])
		ljld[0] = new LinkedHashMap(tm)
		def s = printHtml(ljld, desc, domain,path, ns, guid, label)
	}

	// Under the About: title, add any label, e.g., scos:label
	def printHtml(ljld, desc, domain,path, ns, guid, label) {
		def sb = new StringBuilder()
		sb.append HtmlTemplate.head(host)
		
		// model-viewer
		sb.append """
<!--required-->
<style> 
.mvDiv {
  border: 1px solid;
  margin-left: 0;
  padding: 7px; 
  width: 500px;
  height: 300px;
  resize: both;
  overflow: auto;
}
model-viewer {
  width: 100%;
  height: 100%;
}
</style>
<script type="module" src="https://ajax.googleapis.com/ajax/libs/model-viewer/4.0.0/model-viewer.min.js"></script>
"""
		sb.append """
		<h3 id="title">
		About:
		<a href="${domain}/${path}">$label</a>
		</h3>
		"""
		buildAI(ljld[0],sb)
			
			int i=0
			ljld.each{map->
				ns = [:]
				defs = [:]
				sb.append("""
<h5>${desc[i++]}</h5>
""")
				buildTables(map["@context"])
				//println map
				sb.append HtmlTemplate.tableHead("Property","Value")
				printHtml(map,sb)
				sb.append HtmlTemplate.tableTail
			}
			
			sb.append("""
<br/>
<p style="font-size:12px">
<i>*If you add the query "?format=ttl" to the URL for this page 
the TTL for the items's instance is returned.  Other formats supported include: 
"RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE", (and "TTL") and "N3"</i>
</p>
""")
				
		sb.append HtmlTemplate.tail
		return ""+sb
	}
	
	def printHtml(m, sb) {
		def collection=false
		def work
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
					if (v =~ /_:b[0-9]+/) {
						sb.append """<tr height="50"></tr>\n"""
					} else {
						sb.append """<tr height="50"><td>ID</td><td><a href="${nsLookup(v)}">$v</a></td></tr>\n"""
					}
					work = v
				}
				else if (k=="@type"
						&& (v=="skos:Collection"
						|| (v instanceof List
						&&	"skos:Collection" in v))) {
						collection=true
					def vc = v instanceof List ? v : [v]
					def s=""
					int i=0
					vc.each{ 
						if (i++>0)	s += ", "
						s += """<a href="${nsLookup(it)}">$it</a>"""
					}
					sb.append """<tr height="50"><td><i>$k</i></td><td>$s</td></tr>\n"""
				}
				else if (k=="@type"
						|| k=="tag"
						|| k=="subClassOf"
						|| k=="contains"
						|| k=="related"
						|| k=="broader"
						|| k=="narrower") {
					def vc = v instanceof List ? v : [v]
					def s=""
					int i=0
					vc.each{ 
						if (i++>0)	s += ", "
						s += """<a href="${nsLookup(it)}">$it</a>"""
					}
					sb.append """<tr height="50"><td><i>$k</i></td><td>$s</td></tr>\n"""
				}
				else if (k=="media"
						|| k=="keywords") {
					def vc = v instanceof List ? v : [v]
					def s=""
					int i=0
					vc.each{ 
						if (i++>0)	s += ", "
						s += """$it"""
					}
					sb.append """<tr height="50"><td><i>$k</i></td><td>$s</td></tr>\n"""
				}
				else if (k=="image") {
					if (v instanceof List) {
						v.each{
								sb.append """<tr height="50"><td><i>$k</i></td><td><a href="${rehost(it)}"><img src="${rehost(it)}" width="500"></a></td></tr>\n"""
							}
					} else 	sb.append """<tr height="50"><td><i>$k</i></td><td><a href="${rehost(v)}"><img src="${rehost(v)}" width="500"></a></td></tr>\n"""

				}
				else if (k=="image3d") {
					def bkgndImage
					if (m.background)	{
						bkgndImage = qs.queryOnePropertyFromInstance(m.background, "schema:image")
					}
					if (v instanceof List) {
						v.each{
								sb.append """<tr height="50"><td><i>$k</i></td><td>${do3d(rehost(v),rehost(bkgndImage),work)}</td></tr>\n"""
							}
					} else 	sb.append """<tr height="50"><td><i>$k</i></td><td>${do3d(rehost(v),rehost(bkgndImage),work)}</td></tr>\n"""

				}
				else if (k=="qrcode") {
					sb.append """<tr height="50"><td><i>$k</i></td><td><a href="${rehost(v)}"><img src="${rehost(v)}" width="100"></a></td></tr>\n"""
				}
				else if (isUri(nsLookup(v))) { 
					sb.append """<tr height="50"><td><i>$k</i></td><td><a href="${nsLookup(v)}">$v</a></td></tr>\n"""
				}
				else {
					def type=m2["@type"]
					if (type=="@id"
						&& k == "member"
						&& collection) {
						collection = false
						def id=m2["@id"]
						def vc = v instanceof List ? v : [v]
						// create map of id:label then iterate on map
						def l = qs.queryCollection(vc)
						l.each{map->
							if (v =~ /_:b[0-9]+/) {
							} else {
								def uri=rehost(nsLookup(map.s))
								sb.append """<tr height="50"><td><i>$k</i></td><td><a href="${uri}">${map.l}</a></td></tr>\n"""
							}
						}
					}
					else if (type=="@id"
						&& k != "mailto") {
						def id=m2["@id"]
						def vc = v instanceof List ? v : [v]
						vc.each{ 
							if (v =~ /_:b[0-9]+/) {
							} else 
								sb.append """<tr height="50"><td><i>$k</i></td><td><a href="${nsLookup(it)}">$it</a></td></tr>\n"""
						}
					}
					else {
//						if (k=="skos:Collection")
//							println "here"
						sb.append """<tr height="50"><td><i>$k</i></td>"""
						if (v instanceof List) {
							def v2=[]
							v.each{
								v2+= it.replaceAll("\n","<br>")
							}
							printHtml(v2,sb)
						} else {
							v = v.replaceAll("\n","<br>")
							printHtml(v,sb)
						}
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
	
	def do3d(fs,bkgnd,work) {
		
		def site = qs.queryOnePropertyFromInstance(work, "vad:workOnSite")
		
		def bg = """
	environment-image=$bkgnd
	skybox-image=$bkgnd
"""
		
		// removed AR mode from model-viewer: ar ar-modes="webxr scene-viewer quick-look"
		// to get rid of AR button in model-viewer on IOS
		// TODO: need .glb to .usdz convert
		// ios-src="/images/female-t3-prism3.usdz" 
		"""<div class="mvDiv">
    <model-viewer src="$fs"  
	camera-controls tone-mapping="neutral" shadow-intensity="0"
	${bkgnd ? bg : ""}
	style="flex-grow: 1; height: 100%; background-color: lightgray;">
     </model-viewer>
     </div>
	<table><tr><td>
	<a href="${rehost("http://visualartsdna.org/modelviewer?work=$work&site=$site")}">3D Viewer</a>
	</td><td style="width:50%">
	<img style='display:inline;' src="images/left-click.png" width="20px" height="20px">drag
	<img style='display:inline;' src="images/right-click.png" width="20px" height="20px">pan
	<img style='display:inline;' src="images/scroll.png" width="20px" height="20px">zoom
	</td></tr></table>
"""
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
	
	def buildAI(aiMap,sb) {
		if (!("vad:CreativeWork" in aiMap["@type"])) return
		
		sb.append """
<p align="right" style="font-family:verdana">
<table><tr><td>
<form id="myFormAI" action="/aiInterpretation" method="get">
AI Interpretation 
<select name="kind" id="kind">
<option value="Criticism ">Criticism</option>
<option value="Evaluation">Evaluation</option>
<option value="Assessment ">Assessment</option>
<option value="Influences">Influences</option>
<option value="Gallery Description">Gallery Description</option>
<option value="Technical Assessment">Technical Assessment</option>
</select>
"""
		if ("vad:ComputerArt" in aiMap["@type"]) {
			sb.append """
<input type="hidden" id="type" name="type" value="vad:ComputerArt">
<input type="hidden" id="media" name="media" value="${aiMap.media}">
<input type="hidden" id="image" name="image" value="${aiMap.image}">
<input type="hidden" id="label" name="label" value="${aiMap.label}">
<input type="hidden" id="height" name="height" value="${aiMap.height}">
<input type="hidden" id="width" name="width" value="${aiMap.width}">
<input type="hidden" id="description" name="description" value="${aiMap.description}">
"""
		} 
		else if ("vad:Painting" in aiMap["@type"]) {
			sb.append """
<input type="hidden" id="type" name="type" value="vad:Painting">
<input type="hidden" id="media" name="media" value="${aiMap.media}">
<input type="hidden" id="image" name="image" value="${aiMap.image}">
<input type="hidden" id="label" name="label" value="${aiMap.label}">
<input type="hidden" id="hasPaperWeight" name="hasPaperWeight" value="${aiMap.hasPaperWeight}">
<input type="hidden" id="hasPaperFinish" name="hasPaperFinish" value="${aiMap.hasPaperFinish}">
<input type="hidden" id="height" name="height" value="${aiMap.height}">
<input type="hidden" id="width" name="width" value="${aiMap.width}">
<input type="hidden" id="description" name="description" value="${aiMap.description}">
"""
		}
			sb.append """
<input type = "submit" name = "submit" value = "Ask" />
<a href="/AiInformationPage">more...</a>
</form>
</td></tr></table>
</p>
"""
	}
	
}
