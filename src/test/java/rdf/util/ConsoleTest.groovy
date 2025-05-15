package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import rdf.tools.SparqlConsole

class ConsoleTest {

	def ju = new JenaUtils()
	
	@Test
	void testSPARQL() {
		def l = [
			//"C:/work/stats/artpal/artPal.ttl",
			"C:/work/stats/logMetric.ttl",
			]
		def m2 = ju.loadListFilespec(l)
		def m = ju.loadFiles("C:/temp/git/cwvaContent/ttl")
		m.add m2
		new SparqlConsole().show(m)
	}
	
	
	@Test
	void test() {
		def ju = new JenaUtils()
		def site = "http://visualartsdna.org/metrics"
		// load current metric data to model
		def stats = new URL(site).text
		def c = new JsonSlurper().parseText(stats)
		def mod = new support.StatsReport().getStats(c)
		mod.add ju.loadFiles("c:/work/stats/logMetric.ttl")
		//mod.add ju.loadFiles("c:/work/stats/artpal")
		
		new SparqlConsole().show(mod)
		
	}




}
