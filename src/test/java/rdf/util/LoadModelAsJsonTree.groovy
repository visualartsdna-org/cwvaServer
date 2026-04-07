package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import groovy.json.JsonOutput

class LoadModelAsJsonTree {

	def ju = new JenaUtilities()
	def model = ju.newModel()
	//def src = "/temp/git/cwvaContent/ttl/vocab"
	def src = "/stage/server/cwvaContent/ttl/model/cwva.ttl"
	def tgt = "/stage/tmp/cwvaTree.json"
	
	@Test
	void test() {
		model = ju.loadFiles(src)
		process()
	}

	def process() {
		
		def dl = buildDict().sort{a,b->
			a.l <=> b.l
		}
		def dictionaryTree = convertToDictionary(dl)
		def jsonOutput = JsonOutput.toJson(dictionaryTree)
		def prettyJson = JsonOutput.prettyPrint(jsonOutput)
		new File(tgt).text = prettyJson
	
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
	?s a owl:Class ;
		rdfs:label ?l ;
		rdfs:comment ?d ;
		.
		optional {?s rdfs:subClassOf ?b ;}
		} order by ?l

""")
	
	}
}
