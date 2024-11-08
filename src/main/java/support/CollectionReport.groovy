package support

import static org.junit.Assert.*

import cwva.Server
import org.apache.jena.rdf.model.Model
import java.nio.charset.StandardCharsets

import groovy.json.JsonSlurper
import rdf.JenaUtils
import rdf.QuerySupport
import org.junit.Test

	// register of all watercolors
class CollectionReport {

	def ns = [:]
	def defs = [:]
	def target = "/temp/html/register.html"
	
	@Test
	public void testWork() {
	}
	
	// TODO: is there a more generic way to do this
	def handleUpload(m) {
		collect()
		"file://$target"
	}



	def collect() {
		def rdfs = cwva.Server.getInstance().dbm.rdfs
		def s = new CollectionReport().process(rdfs)
		new File(target).text = s
	}


	def process(rdfs) {
		def qs = new QuerySupport(rdfs)
		def data = qs.queryRegistry()
		def s = printHtml(data)
	}

	def printHtml(data) {
		def sb = new StringBuilder()
		//sb.append CertAuthTemplate.head(host)
		sb.append """
<h4 id="title">
<center>Register of Watercolors</center><br>
<center>by Rick Spates</center>
</h4>
<br>
<table>
"""
		def ht = 60
			int i=0
			data.each{map->
				sb.append """
<tr height="${ht}"><td>${map.label}</td><td><a href="${map.image}"><img src="${map.image}" width="50"></a></td><td><a href="${map.qrcode}"><img src="${map.qrcode}" width="50"></a></td><td>${map.year}</td><td>${map.desc}</td></tr>
"""
			}
			sb.append """
</table>
"""
		
		sb.append CertAuthTemplate.tail
		
		return ""+sb
	}
}
