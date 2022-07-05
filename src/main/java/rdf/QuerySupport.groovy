package rdf

import org.apache.jena.rdf.model.InfModel
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory

class QuerySupport {

	def prefixes = ""
	def ju = new JenaUtilities()
	def mdl = ju.newModel()

	QuerySupport(file){

		mdl = ju.loadFiles(file)
		//println "triples: ${mdl.size()}"
		assert mdl.size() > 0, "model size is 0"
		init()
	}
	
	def init() {

		// get prefixes from the query collection
		def p =  getSelect("","""
prefix vad:   <http://visualartsdna.org/2021/07/16/model#> 
	select ?prefixes {
		?s a vad:QueryCollection .
		?s vad:prefixes ?prefixes .
}

""")
		//println p.prefixes
		assert p.prefixes != null, "prefixes are null"
		this.prefixes = p.prefixes
	}
	
	QuerySupport(dataFile,schemaFile){
		Model data = ju.loadFiles(dataFile);
		Model schema = ju.loadFiles(schemaFile);
		mdl = ModelFactory.createRDFSModel(schema, data);
		init()
	}
	
	def getPrefixNsMap() {
		mdl.getNsPrefixMap()
	}

	// get the queries
	// for the type and
	// membership in the query collection
	def queriesByType(type,ns,guid) {
		def q = getSelectList("","","""
prefix skos:  <http://www.w3.org/2004/02/skos/core#> 
prefix tko:   <http://visualartsdna.org/takeout#> 
prefix vad:   <http://visualartsdna.org/2021/07/16/model#> 
prefix z0:    <http://visualartsdna.org/system#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
prefix schema: <https://schema.org/> 

	select ?query ?desc {
		?s schema:identifier ?query .
		?c skos:member ?s .
		?c a vad:QueryCollection .
		?s z0:targetType ?t .
		filter (?t in (${listToStr(type) }))  #  type is a list
		?s z0:priority ?priority .
		?s schema:description ?desc .
		} order by ?priority

""")
		//println q.query

		def resList=[]
		def descList=[]
		// 5. retrieve the query from the collection
		q.query.each {qry->
			def r2 =  query(qry,ns,guid)
			resList += r2[0]
			descList += r2[1] ?: ""
			println r2[1] ? "${r2[1]} (${qry})": "no id/desc"
		}
		[resList,descList]
	}
	
	def listToStr(l) {
		def s=""
		int i=0
		l.each {
			if (i++) s+="," 
			s+= "<$it>"
			}
			s
	}

	def query(qid) {
		query(qid,"","")
	}

	def query(qid,ns,guid) {
		query("$ns:$qid","$ns:$guid")
	}

	// get query from query collection
	// run the query
	def query(qid,work) {
		def r =  getSelect(qid,"""
	select ?query ?desc {
		bind (%work as ?s)
		?s z0:query ?query .
		?s schema:description ?desc
}

""")
		//println r.query

		if (r.query.contains("construct "))
			[getConstruct(work,r.query),r.desc]
		else if (r.query.contains("describe "))
			[getDescribe(work,r.query),r.desc]
		else if (r.query.contains("select "))
			[getSelect(work,r.query),r.desc]
	}

	// query support methods
	def getOneInstanceModel(ns,guid) {
		getOneInstanceModel("$ns:$guid")
	}
	def getOneInstanceModel(work) {

		ju.queryDescribe(mdl, prefixes, """
describe ${work}
"""
				)
	}

	def getDescribe(ns,guid,query) {
		getDescribe("$ns:$guid",query)
	}
	def getDescribe(work,query) {

		query = query.replaceAll("%work",work)
		ju.queryDescribe(mdl,prefixes, query)
	}

	def getConstruct(ns,guid,query) {
		getConstruct("$ns:$guid",query)
	}
	def getConstruct(work,query) {

		query = query.replaceAll("%work",work)
		ju.queryExecConstruct(mdl,prefixes, query)
	}

	def getSelect(ns,guid,query) {
		getSelect("$ns:$guid",query)
	}
	def getSelect(work,query) {
		query = query.replaceAll("%work",work)
		ju.queryListMap2(mdl,prefixes,query)
	}

	def getSelectList(query) {
		getSelectList("","",query)
	}
	def getSelectList(ns,guid,query) {
		getSelectList("$ns:$guid",query)
	}
	def getSelectList(work,query) {
		query = query.replaceAll("%work",work)
		ju.queryListMap(mdl,prefixes,query)
	}

	def getType(ns,guid) {


		def q = getSelectList("","","""
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix work:	<http://visualartsdna.org/work/>

	select ?type {
		bind ($ns:$guid as ?s)
#		?s rdf:type/rdfs:subClassOf ?type .
		?s rdf:type ?type .
		}

""")
		q.type

	}

}
