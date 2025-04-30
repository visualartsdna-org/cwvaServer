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
		
	def grafTblArrangement = "</tr><tr></tr><tr>" // stacked
	//def grafTblArrangement = "" // side-by-side
	
	def sb = new StringBuilder()
	
	def graphTables = """
<table><tr><td>
<table cellspacing="3" cellpadding="3" style="white-space: nowrap">
<tr><th><a href="https://d3js.org/">D3 graphics</a></th></tr>
<tr><td><a href="${cfg.host}/d3.wcDrawBasic">drawings and watercolors basic data</a></td></tr>
<tr><td><a href="${cfg.host}/d3.wcBasic">watercolors basic data</a></td></tr>
<tr><td><a href="${cfg.host}/d3.wcPhysical">watercolors physical data</a></td></tr>
<tr><td><a href="${cfg.host}/d3.wcNFT">watercolors NFT data</a></td></tr>
<tr><td><a href="${cfg.host}/d3.drawBasic">drawings basic data</a></td></tr>
<tr><td><a href="${cfg.host}/d3.drawPhysical">drawings physical data</a></td></tr>
<tr><td><a href="${cfg.host}/d3.drawNFT">drawings NFT data</a></td></tr>
</table>
</td>$grafTblArrangement<td>
<table cellspacing="3" cellpadding="3" style="white-space: nowrap">
<tr><th><a href="https://github.com/rspates/lsys">Lsys graphics</a></th></tr>
<tr><td><a href="${cfg.host}/lsys.wcDrawBasic">drawings and watercolors basic data</a></td></tr>
<tr><td><a href="${cfg.host}/lsys.wcBasic">watercolors basic data</a></td></tr>
<tr><td><a href="${cfg.host}/lsys.wcPhysical">watercolors physical data</a></td></tr>
<tr><td><a href="${cfg.host}/lsys.wcNFT">watercolors NFT data</a></td></tr>
<tr><td><a href="${cfg.host}/lsys.drawBasic">drawings basic data</a></td></tr>
<tr><td><a href="${cfg.host}/lsys.drawPhysical">drawings physical data</a></td></tr>
<tr><td><a href="${cfg.host}/lsys.drawNFT">drawings NFT data</a></td></tr>
</table>
</td></tr></table>
"""
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
<a href="${cfg.host}/model">RDF file (TTL/text)</a>.
<p>
<p/>
See this ontology titled "Information Model for the Visual Arts" on  
<a href="https://archivo.dbpedia.org/info?o=http://visualartsdna.org/2025/04/26/model/">Archivo</a>.
This ontology is not definitive or complete.
<p>
<p/>
A vocabulary (thesaurus) of visual arts terms is available 
in
<a href="${cfg.host}/vocab.graph">graphical form&#42;</a>
and in an 
<a href="${cfg.host}/vocab">RDF file (TTL/text)</a>.
<!--(See  
<a href="${cfg.host}/html/graphInstructions.html">pan and zoom SVG</a> for more information.)
-->
<p>
<p/>
The current instance data can be browsed by selecting "Browse" at the top of the page.  
${cwva.Server.getInstance().cfg.sparql ? "Query the model, vocabulary and instance data by selecting \"SPARQL\" at the top of the page." : ""}
<!--The current instance data is also available in an 
<a href="${cfg.host}/data">RDF file (TTL/text)</a>.  -->
<!--A list of <a href="${cfg.host}/html/vocab.html">vocabulary concepts</a> is also available.
-->
<p>
<p><i>
The thesaurus and the ontology use different knowledge representation formalisms (SKOS and OWL, respectively).  The concepts in the thesaurus serve as a vocabulary that can be used to populate or describe instances within the classes defined by the ontology.  The hierarchical relationships defined using skos:broader in the thesaurus align with the class hierarchies defined using rdfs:subClassOf in the ontology.  The organization of the thesaurus using skos:inScheme reflects different subject areas or modules within the ontology. The thesaurus and the ontology are intended to be semantically aligned and coherent, with the thesaurus providing the vocabulary for the domain modeled by the ontology. They work together to provide a structured representation of knowledge in the visual arts.
</i> <br>[This analysis is from a <a href="https://gemini.google.com">google gemini</a> review of the thesaurus and ontology.]

""" + // comment out the next section to remove D3 and lsys graphics from index page
"""
<p/>
<p/>
The data in the instance model can be viewed with:
<p>
$graphTables
<p/>
""" +
"""Development of the VisualArtsDNA ontology is motivated by
a need to organize the <a href="http://rickspates.art">author's art information</a>,
other artists may benefit from using the model for their own art information, and
collaborating artists may need a common information model over the visual arts creative-process domain.
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
