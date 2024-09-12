package rdf.util

import org.apache.jena.rdf.model.*
import org.apache.jena.reasoner.*
import rdf.JenaUtilities
import rdf.tools.SparqlConsole

class DBMgr {

	def data
	def tags
	def vocab
	def instances
	def schema
	def rdfs // TODO: change rdfs to infModel globally
	def cfg

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

		def ju = new JenaUtilities()

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
		println "gcpCpDirRecurse: ${cfg.cloud.src}, ${cfg.cloud.tgt}"
		if (clobber) {
			util.Gcp.gcpCpDirRecurseClobber(cfg.cloud.src,cfg.cloud.tgt)
		}
		else {
			util.Gcp.gcpCpDirRecurse(cfg.cloud.src,cfg.cloud.tgt)
		}
		
		def ju = new JenaUtilities()

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
		} else println "Model valid"
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

}
