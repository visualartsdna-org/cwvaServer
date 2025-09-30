package services

import org.junit.Test
import util.Gcp
import groovy.json.JsonSlurper
import rdf.JenaUtils

class OtherStuff {
	
	@Test
	public void test() {
		//def ttl = "test.ttl"
		//def ttl = "/temp/art.ttl"
		def ttl = "C:/stage/planned/node/ttl/art.ttl"
		def s = new OtherStuff().get()
		println s
		new File("C:/test/vadna/junk.html").text = s
	}
	
	def cfg=[:]
	
	OtherStuff(){
		this.cfg = cwva.Server.getInstance().cfg
	}
	
//	OtherStuff(cfg){
//		this()
//	}
	
	def grafTblArrangement = "</tr><tr></tr><tr>" // stacked
	//def grafTblArrangement = "" // side-by-side
	def graphTables = """
"""

	def get() {
		
	def grafTblArrangement = "</tr><tr></tr><tr>" // stacked
	//def grafTblArrangement = "" // side-by-side
	
	def sb = new StringBuilder()
	
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
More
</h2></center>
<p/>
<a href="https://w3id.org/lode/owlapi/http://visualartsdna.org/model/">Ontology Documentation (via LODE server)</a><br/>
<a href="${cfg.host}/html/AICWVAReview.html">Ontology and Thesaurus AI Reviews by OpenAI, MetaAI, Gemini and Perplexity</a><br/>
<!--
See the Ontology, "Information Model for the Visual Arts," on <a href="https://archivo.dbpedia.org/info?o=http://visualartsdna.org/model/">Archivo</a><br/>
-->
<a href="${cfg.host}/sparql">SPARQL Browser</a><br/>
<a href="${cfg.host}/metricTables">Metrics</a><br/>
<a href="${cfg.host}/html/references.html">References</a><br/>
<p/>
<p/>
<p/>
The data in the instance model can be viewed with:
<p>
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

<p/>
""" 
	sb.append HtmlTemplate.tail
	
	""+sb
	}
	
	def getMetrics() {
		
		def src = "stats"
		def f = "chart.html"
		try {
			def url = Gcp.gcpLs(src,f)
			//if (url) println "found url: ${url}" // debug
			if (url) Gcp.gcpCp(url,"${cfg.dir}/$f")
		} catch (RuntimeException re) {
			System.err.println ("$f not found, $re")
			throw new FileNotFoundException("$f not found")
		}
		def f2 = new File("${cfg.dir}/$f")
		
		if (!f2.exists()) {
			throw new FileNotFoundException("$f not found")
		}
		f2.text
	}
			
		
}
