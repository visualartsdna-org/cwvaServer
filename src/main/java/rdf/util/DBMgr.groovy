package rdf.util

import org.apache.jena.rdf.model.*
import org.apache.jena.shacl.*
import org.apache.jena.reasoner.*
import org.junit.jupiter.api.Test
import groovy.io.FileType
import rdf.JenaUtilities
import rdf.tools.SparqlConsole
import org.apache.jena.util.*
import org.apache.jena.vocabulary.ReasonerVocabulary
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory
import org.apache.jena.shacl.Shapes
import org.apache.jena.shacl.ValidationReport
import org.apache.jena.shacl.validation.*
import org.apache.jena.shacl.lib.ShLib

class DBMgr {

	def data
	def tags
	def vocab
	def instances
	def schema
	def rdfs // TODO: change rdfs to infModel globally
	def cfg
	def ju = new JenaUtilities()


	// needs work and direction!
	DBMgr(List loads) {
		this.cfg = cfg
		def m = load(loads)
		//		new Thread().start{
		//			new SparqlConsole().show(m)
		//		}
	}

	DBMgr(Map cfg) {
		this.cfg = cfg
		cleanUp()
		def m = load()
//				new Thread().start{
//					new SparqlConsole().show(m as Model)
//				}
	}

	def load(model,l) {


		def data = ju.newModel()
		l.each{
			data.add ju.loadFiles(it)
		}

		// TODO: fix reload exception

		schema = ju.loadFiles(model);
		rdfs = ModelFactory.createRDFSModel(schema, data);
	}

	def load() {

		def clobber = System.getProperty("clobber")?:false
		// restore ttl from gcp
		if (cfg.cloud) {
			println "gcpCpDirRecurse: ${cfg.cloud.src}, ${cfg.cloud.tgt}"
			if (clobber) {
				util.Gcp.gcpCpDirRecurseClobber(cfg.cloud.src,cfg.cloud.tgt)
			}
			else {
				util.Gcp.gcpCpDirRecurse(cfg.cloud.src,cfg.cloud.tgt)
			}
		}
		

		instances = ju.loadFiles(cfg.data)
		tags = ju.loadFiles(cfg.tags)
		vocab = ju.loadFiles(cfg.vocab)

		// TODO: fix reload exception
		data = ju.newModel()
		data.add( instances )
		data.add( tags )
		data.add( vocab )

		schema = ju.loadFiles(cfg.model)
		//rdfs = ModelFactory.createRDFSModel(schema, data)
		rdfs = getReasoner("owlmicro", schema, data)
		rdfs = skosInfer(rdfs,"${cwva.Server.getInstance().cfg.dir}/res/skos.rules")
		//rdfs = ModelFactory.createRDFSModel(schema, data)
		Policy.exec(rdfs)
		validate(rdfs)
	}
	
	def validate(inf) {
		ValidityReport validity = (inf as InfModel).validate();
		if (!validity.isValid()) {
			println "Model validity conflicts"
			for (Iterator i = validity.getReports(); i.hasNext(); ) {
				ValidityReport.Report report = (ValidityReport.Report)i.next();
				println " - " + report
			}
		} else println "Inference model valid"
		
		// shacl validation
		def dir = new File("${cwva.Server.getInstance().cfg.dir}/res")
		dir.eachFileRecurse (FileType.FILES) { file ->
			if(file.name.endsWith('.shacl')) {
				println "$file"
				shacl(inf, "$file")
			}
		}
		
		inf
	}
	
	def getReasoner(name, schema, data) {
		def inf
		def ctms = System.currentTimeMillis()
		switch (name){
			
			case "owl":
			def reasoner = ReasonerRegistry.getOWLReasoner()
			reasoner = reasoner.bindSchema(schema)
			inf = ModelFactory.createInfModel(reasoner, data)
			break
			
			case "owlmini":
			def reasoner = ReasonerRegistry.getOWLMiniReasoner()
			reasoner = reasoner.bindSchema(schema)
			inf = ModelFactory.createInfModel(reasoner, data)
			break
			
			case "owlmicro":
			def reasoner = ReasonerRegistry.getOWLMicroReasoner()
			reasoner = reasoner.bindSchema(schema)
			inf = ModelFactory.createInfModel(reasoner, data)
			break
			
			case "rdfs":
			default:
			inf = ModelFactory.createRDFSModel(schema, data);
			break
		}
		println "reasoner: $name, size=${inf.size()}, elapsed=${System.currentTimeMillis()-ctms}"
		inf
	}

	// TODO: under construction
	def reload() {
		inf.close()
		load()
	}
	
	def cleanUp() {
		[
			"ttl/data":cfg.data,
			"ttl/model":cfg.model,
			"ttl/tags":cfg.tags,
			"ttl/vocab":cfg.vocab,
			].each{k,v->
		util.Gcp.folderCleanup(
			k, // gDir
			v,	// fDir
			/.*\.ttl/) // filter
		}

	}

	def getSizes() {
		[
			data : data.size(),
			instances :  instances.size(),
			vocab :  vocab.size(),
			tags :  tags.size(),
			schema :  schema.size(),
			rdfs :  rdfs.size()
		]
	}

	def print() {
		getSizes().each{k,v->
			println ("$k = $v")
		}
	}
	
	def skosInfer(df,rules) {
		
		Model data = ju.loadFiles(df)
		skosInfer( data, rules)
		
	}
	
	def skosInfer(Model data,rules) {
		//def ju = new JenaUtilities()
		
		String demoURI = "http://www.w3.org/2004/02/skos/core#";
		PrintUtil.registerPrefix("skos", demoURI);
		
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
//			if (it.contains("skos:broader"))
//			println it
//		}
	}

	def shacl(Model dataGraph, shacl) {
		
		Model shapesGraph = ju.loadFiles(shacl)
		
		Shapes shapes = Shapes.parse(shapesGraph);
	
		ValidationReport report = ShaclValidator.get().validate(shapes.getGraph(), dataGraph.getGraph());

		ShLib.printReport(report)
	}

}
