package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import groovy.json.JsonOutput

class LoadDataAsJsonTree {

	def ju = new JenaUtilities()
	def model = ju.newModel()
	//def src = "/temp/git/cwvaContent/ttl/vocab"
	def src = "/stage/server/cwvaContent/ttl/data"
	def tgt = "/stage/tmp/dataTree.json"
	
	@Test
	void test() {
		model = ju.loadFiles(src)
		process()
	}

	def process() {
		
		enhanceModelWithDates()
		
		def dl = buildDict().sort{a,b->
			a.l <=> b.l
		}
		def dictionaryTree = convertToDictionary(dl)
		def jsonOutput = JsonOutput.toJson(dictionaryTree)
		def prettyJson = JsonOutput.prettyPrint(jsonOutput)
		new File(tgt).text = prettyJson
	
		}
		
	def THE_NS = "http://visualartsdna.org/thesaurus/"
	
	def enhanceModelWithDates() {
		def m = ju.queryExecConstruct(model, rdf.Prefixes.forQuery, """
construct  {
		?sdt a the:DateObject ;
		rdfs:label ?l ;
		rdfs:comment ?d ;
		.
} where {
	?s a vad:Watercolor ;
		schema:dateCreated ?dt .
		bind("year" as ?d)
		bind(year(?dt) as ?y)
		bind(str(?y) as ?l)
		bind(iri(concat("$THE_NS",?l)) as ?sdt)
}

""")
		model.add m
	
		m = ju.queryExecConstruct(model, rdf.Prefixes.forQuery, """
construct  {
		?sdt a the:DateObject ;
		rdfs:label ?l ;
		rdfs:comment ?d ;
		the:year ?b ;
		.
} where {
	?s a vad:Watercolor ;
		schema:dateCreated ?dt .
		bind("month" as ?d)
		bind(month(?dt) as ?monthNum)
		bind(year(?dt) as ?y)
		bind(str(?y) as ?yl)
VALUES (?monthNum ?monthName) {
    (01  "Jan")
    (02  "Feb")
    (03  "Mar")
    (04  "Apr")
    (05  "May")
    (06  "Jun")
    (07  "Jul")
    (08  "Aug")
    (09  "Sep")
    (10  "Oct")
    (11  "Nov")
    (12  "Dec")
  }


		bind(concat(?yl,"-",?monthName) as ?l)
		bind(iri(concat("$THE_NS",?l)) as ?sdt)
		bind(iri(concat("$THE_NS",?yl)) as ?b)
} 
""")
		model.add m
	}
	
	/**
	 * Recursively builds a tree node for the given URI
	 * @param uri The URI of the current node
	 * @param resultsMap Map of URI to result row
	 * @param childrenMap Map of parent URI to list of child URIs
	 * @return A map representing the node in dictionary format
	 */
	def buildNode(uri, resultsMap, childrenMap) {
		def result = resultsMap[uri]
		def label = result.l
		def definition = result.d
		
		def childUris = childrenMap[uri] ?: []
		def children = childUris.collect { childUri ->
			buildNode(childUri, resultsMap, childrenMap)
		}
		
		return [(label): [
			uri: ju.getCuri(model,uri),
			definition: definition,
			children: children
		]]
	}
	
	/**
	 * Converts query results to dictionary tree format
	 * @param queryResults List of maps with s, b, l, d keys
	 * @return List of root nodes in dictionary format
	 */
	def convertToDictionary(queryResults) {
		// Create lookup maps
		def resultsMap = queryResults.collectEntries { [(it.s): it] }
		
		// Build parent-to-children mapping
		def childrenMap = [:].withDefault { [] }
		queryResults.each { row ->
			if (row.b) {
				childrenMap[row.b] << row.s
			}
		}
		
		// Find root nodes (nodes without parent 'b' property)
		def rootUris = queryResults.findAll { !it.b }.collect { it.s }
		
		// Build tree recursively from each root
		return rootUris.collect { rootUri ->
			buildNode(rootUri, resultsMap, childrenMap)
		}
	}
	
	def buildDict() {
		
		ju.queryListMap1(model, rdf.Prefixes.forQuery, """
select ?s ?b ?l ?d {

{
	?s a vad:Watercolor ;
		rdfs:label ?l ;
		schema:description ?d ;
		schema:dateCreated ?dt .
		bind(month(?dt) as ?monthNum)
VALUES (?monthNum ?monthName) {
    (01  "Jan")
    (02  "Feb")
    (03  "Mar")
    (04  "Apr")
    (05  "May")
    (06  "Jun")
    (07  "Jul")
    (08  "Aug")
    (09  "Sep")
    (10  "Oct")
    (11  "Nov")
    (12  "Dec")
  }
		bind(year(?dt) as ?y)
		bind(concat(str(?y),"-",?monthName) as ?bl)
		bind(iri(concat("http://visualartsdna.org/thesaurus/",?bl)) as ?b)
		.
		
} union {
	?s a the:Entity ;
		rdfs:label ?l ;
		skos:definition ?d ;
		the:tag ?b ;
		.

} union {
	?s a the:DateObject ;
		rdfs:comment ?d ;
		rdfs:label ?l .
		filter(?d="month")
		optional {?s the:year ?b ;}

} union {
	?s a the:DateObject ;
		rdfs:comment ?d ;
		rdfs:label ?l .
		filter(?d="year")

} 

} order by ?dt ?l

""")
	
	}
}
