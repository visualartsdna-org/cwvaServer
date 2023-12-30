package rdf

import org.apache.jena.rdf.model.InfModel
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory

class QuerySupport {

	def prefixes = Prefixes.forQuery
	def ju = new JenaUtilities()
	def mdl = ju.newModel()

	QuerySupport(mdl){
		this.mdl = mdl
	}
	
	def query(ns,guid) {
		def mm = [:]
		
		mm ["$ns:$guid"] = ju.queryDescribe(mdl, prefixes, """
describe ${ns}:${guid}
""")
		if (mm["$ns:$guid"].size()==0) return mm
		mm ["label"] = ju.queryListMap1(mdl, prefixes, """
select ?label { ${ns}:${guid} rdfs:label ?label }
""")[0]["label"]
		mm ["Tags"] =
		ju.queryExecConstruct(mdl, prefixes, """
# print the concepts, labels and definitions for the work or collection
# containing the work
construct {
[
 rdfs:label ?l ;
 skos:description ?d
]
} {
	select distinct ?c ?l ?d
	{
		bind (${ns}:${guid} as ?s)
		{
		?s the:tag ?c .
		?c a skos:Concept ;
        		rdfs:label ?l ;
			skos:definition ?d
		} union {
		?col skos:member ?s .
		?col the:tag ?c .
		?c a skos:Concept ;
        		rdfs:label ?l ;
			skos:definition ?d
		}
	} order by ?l
}
""")
		mm
	}
	
	def queryCertAuth(ns,guid) {
		def mm = [:]
		
		mm ["$ns:$guid"] = ju.queryDescribe(mdl, prefixes, """
describe ${ns}:${guid}
""")
		mm ["label"] = ju.queryListMap1(mdl, prefixes, """
select ?label { ${ns}:${guid} rdfs:label ?label }
""")[0]["label"]
		mm ["Profile"] =
		ju.queryDescribe(mdl, prefixes, """
# print the profile
describe ?c {
		bind (${ns}:${guid} as ?s)
		?s vad:hasArtistProfile ?c .
}
""")
		mm ["Artist"] =
		ju.queryDescribe(mdl, prefixes, """
# print the profile
describe ?a {
		bind (${ns}:${guid} as ?s)
		?s vad:hasArtistProfile ?c .
		?c vad:artist ?a .
}
""")
		mm
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


}
