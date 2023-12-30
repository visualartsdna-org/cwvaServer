package rdf.util

import org.apache.jena.rdf.model.*
import rdf.JenaUtilities
import rdf.tools.SparqlConsole

class DBMgr {

	def data
	def tags
	def vocab
	def instances
	def schema
	def rdfs
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
		def m = load()
		//		new Thread().start{
		//			new SparqlConsole().show(m)
		//		}
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

		// restore ttl from gcp
		println "gcpCpDirRecurse: ${cfg.cloud.src}, ${cfg.cloud.tgt}"
		util.Gcp.gcpCpDirRecurse(cfg.cloud.src,cfg.cloud.tgt)

		def ju = new JenaUtilities()

		instances = ju.loadFiles(cfg.data)
		tags = ju.loadFiles(cfg.tags)
		vocab = ju.loadFiles(cfg.vocab)

		// TODO: fix reload exception
		data = ju.newModel()
		data.add( instances )
		data.add( tags )
		data.add( vocab )

		schema = ju.loadFiles(cfg.model);
		rdfs = ModelFactory.createRDFSModel(schema, data);
		Policy.exec(rdfs)
	}

	// TODO: under construction
	def reload() {
		rdfs.close()
		load()
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
