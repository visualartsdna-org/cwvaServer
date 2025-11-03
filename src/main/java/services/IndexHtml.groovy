package services

import org.junit.Test

import groovy.json.JsonSlurper
import rdf.JenaUtils

class IndexHtml {
	
	@Test
	public void test() {
		//def ttl = "test.ttl"
		//def ttl = "/temp/art.ttl"
		def ttl = "C:/stage/planned/node/ttl/art.ttl"
		def s = new IndexHtml().get()
		println s
		new File("C:/test/vadna/junk.html").text = s
	}
	
	def cfg=[:]
	
	IndexHtml(cfg){
		this.cfg = cfg
	}
	
	def get() {
		
	
	def sb = new StringBuilder()
	
// <tr><td><a href="${cfg.host}/d3.all">all data</a></td></tr>
// <tr><td><a href="${cfg.host}/lsys.all">all data</a></td></tr>

	
	sb.append HtmlTemplate.head(cfg.host)
	sb.append """
<style>
p {
  margin-left: 4%;
}
table {
  margin-left: 10%;
}
</style>
<center><h2>
VisualArtsDNA
</h2></center>
<p/>
VisualArtsDNA organizes the details of the visual arts creative process into an 
information model expressed in the
<a href="https://en.wikipedia.org/wiki/Web_Ontology_Language">Web Ontology Language (OWL)</a>,
a type of
<a href="https://en.wikipedia.org/wiki/Resource_Description_Framework">RDF model</a>.  
<br/>
<p/>
This ontology is inspired by the 
<a href="https://mc.movielabs.com">Creative Works Ontology for the Film &amp; TV Industry</a>.
<br/>
<br/>The ontology is available in
<a href="${cfg.host}/model.graph">graphical form&#42;</a>
and in an 
<a href="${cfg.host}/model/">RDF file (TTL/text)</a>.
View the <a href="https://w3id.org/lode/owlapi/http://visualartsdna.org/model/">ontology documentation via LODE server</a>.</br>
<p>
<p/>
A thesaurus of visual arts terms is available in
<a href="${cfg.host}/vocab.graph">graphical form&#42;</a>
and in an 
<a href="${cfg.host}/vocab">RDF file (TTL/text)</a>.
<!--(See  
<a href="${cfg.host}/html/graphInstructions.html">pan and zoom SVG</a> for more information.)
-->
<p>
<p/>
The current instance data can be browsed by selecting "Browse" at the top of the page.  
${cwva.Server.getInstance().cfg.sparql ? "Query the model, vocabulary and instance data by selecting \"More\" at the top of the page, then \"SPARQL\"" : ""}
<p>
<p/>
See AI platform reviews of the ontology and thesaurus by top AI platforms.  Select \"More\" at the top of the page, then \"AI Reviews\".  

""" + // comment out the next section to remove D3 and lsys graphics from index page
"""
<p/>
<p/>
<p/>
""" +
"""Development of the VisualArtsDNA ontology is motivated by
a need to organize the <a href="http://rickspates.art">author's art information</a>,
other artists may benefit from using the model for their own art information, and
collaborating artists may need a common information model over the visual arts creative-process domain.
This ontology is not definitive or complete.
<p/>
<p/>
<p/>
<font size="3">
&#42;See  
<a href="${cfg.host}/html/graphInstructions.html">pan and zoom SVG</a> for more information.
</font>
"""
//	The SVG graph is generated following this <a href="http://visualartsdna.org/graphInstructions.html">process</a>.
//	<p/>
	
	sb.append HtmlTemplate.tail
	
	""+sb
	}
	
	
	def removeAt(s) {
		if (s.contains("@"))
			(s =~ /@(.*)/)[0][1]
		else s
	}

	def get(ttl) {
		
	def sb = new StringBuilder()
	sb.append HtmlTemplate.head
	
	def m = new JenaUtils().loadDirModel(ttl)
//	def m = new JenaUtils().loadFileModelFilespec(ttl)
	m.write(new File("/temp/temp.json-ld").newOutputStream(),"JSON-LD")
	def map = new JsonSlurper().parse(new File("/temp/temp.json-ld"))
	buildTables(map["@context"])
	
	sb.append HtmlTemplate.tableHead("Graph")
	sb.append """
<h2>
VisualArtsDNA render...
</h2>
<br/>
<a href="${cfg.host}/model">the Visual Arts model</a>.
<br/>
"""
			
	map["@graph"].each {
		
		sb.append "<tr><td><dl>"
		it.each{k,v->
		
			if (k == "@id")
			sb.append """<dt>${removeAt(k)}&emsp;${v}</dt>
"""
			else sb.append """<li>${removeAt(k)}&emsp;${v}</li><br/>
"""
		}
		sb.append "</dl></td></tr>"
	}
	sb.append HtmlTemplate.tableTail
	sb.append "<br/><hr/><br/>"
	sb.append HtmlTemplate.tableHead("Context")
			
	map["@context"].each {k,v->
		def kn = v instanceof Map ? k : "$k:"
		sb.append "<tr><td><dl>"
		
			if (k == "@id")
			sb.append """<dt>${removeAt(k)}&emsp;${v}</dt>
"""
			else sb.append """<li>${removeAt(kn)}&emsp;${v}</li><br/>
"""
		sb.append "</dl></td></tr>"
	}
	sb.append HtmlTemplate.tableTail


	sb.append HtmlTemplate.tail
	
	""+sb
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
	}
	
	def nsLu(s) {
		def pre = (s =~ /([a-z]+):(.*)/)[0]
		"${ns[pre[1]]}${pre[2]}"
		
	}

	def get0(ttl) {
		
	def sb = new StringBuilder()
	sb.append HtmlTemplate.head
	
	def m = new JenaUtils().loadFileModelFilespec(ttl)
	m.write(new File("/temp/temp.json-ld").newOutputStream(),"JSON-LD")
	def map = new JsonSlurper().parse(new File("/temp/temp.json-ld"))
	buildTables(map["@context"])
	
	sb.append HtmlTemplate.tableHead("Graph")
	sb.append """
<h2>
VisualArtsDNA render...
</h2>
<br/>
<a href="${cfg.host}/model">the Visual Arts model</a>.
<br/>
"""
			
	map["@graph"].each {
		
		sb.append "<tr><td><dl>"
		it.each{k,v->
		
			if (k == "@id")
			sb.append """<dt>${removeAt(k)}&emsp;${v}</dt>
"""
			else sb.append """<li>${removeAt(k)}&emsp;${v}</li><br/>
"""
		}
		sb.append "</dl></td></tr>"
	}
	sb.append HtmlTemplate.tableTail

		
	sb.append HtmlTemplate.tail
	
	""+sb
	}
	

}
