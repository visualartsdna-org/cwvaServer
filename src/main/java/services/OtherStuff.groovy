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
Tools
</h2></center>
<p/>
<a href="${cfg.host}/sparql">SPARQL Browser</a></br>
<a href="${cfg.host}/metricTables">Metrics</a></br>
<a href="${cfg.host}/html/references.html">References</a></br>
<p/>
<p/>
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
