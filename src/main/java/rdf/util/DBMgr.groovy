package rdf.util

import org.apache.jena.rdf.model.*
import rdf.JenaUtilities

class DBMgr {

	def data
	def tags
	def vocab
	def instances
	def schema
	def rdfs
	def cfg
	
	DBMgr(Map cfg) {
		this.cfg = cfg
		load()
	}
	
	def load() {
			
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
	}
	
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
