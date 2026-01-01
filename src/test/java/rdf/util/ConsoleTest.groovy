package rdf.util

import static org.junit.jupiter.api.Assertions.*
import org.apache.jena.reasoner.*
import org.apache.jena.rdf.model.*
import org.apache.jena.vocabulary.ReasonerVocabulary
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory
import org.apache.jena.util.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtilities
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

	@Test
	void testLoadAllSansRdfs() {
		def ju = new JenaUtils()

		def mod = new JenaUtilities().loadFiles("/stage/server/cwvaContent/ttl")
		
		def rdfs = skosInfer(mod,"/stage/tmp/rdfs.rules")
		
		ju.saveModelFile(rdfs,"/stage/tmp/test","ttl")
		new SparqlConsole().show(rdfs)
		
	}

	
//	def skosInfer(df,rules) {
//		
//		Model data = ju.loadFiles(df)
//		skosInfer( data, rules)
//		
//	}
	
	def skosInfer(Model data,rules) {
		//def ju = new JenaUtilities()
		
		String demoURI = "<http://www.w3.org/2000/01/rdf-schema#";
		PrintUtil.registerPrefix("rdfs", demoURI);
		
//		println "${data.size()}"
		Model m = ju.newModel()
		
		Resource configuration =  m.createResource();
		configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
		configuration.addProperty(ReasonerVocabulary.PROPruleSet,  rules);
		
		// Create an instance of such a reasoner
		Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
		
		// Load data
		InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
		
//		def ms = ju.saveModelString(infmodel)
//		println ms
//		ms.eachLine {
//			if (it.contains("rdf:type"))
//			println it
//		}
	}



}
