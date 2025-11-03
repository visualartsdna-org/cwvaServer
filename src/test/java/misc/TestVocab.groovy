package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import groovy.json.JsonOutput

class TestVocab {

	@Test
	void test() {
	// Convert and output
	def dictionaryTree = convertToDictionary(queryResults)
	def jsonOutput = JsonOutput.toJson(dictionaryTree)
	def prettyJson = JsonOutput.prettyPrint(jsonOutput)
	
	println prettyJson
	}

	
	def queryResults = [
		[s: 'uri0', l: 'label0', d: 'defn0'],
		[s: 'uri1', b: 'uri0', l: 'label1', d: 'defn1'],
		[s: 'uri2', b: 'uri1', l: 'label2', d: 'defn2'],
		[s: 'uri3', b: 'uri1', l: 'label3', d: 'defn3'],
		[s: 'uri4', b: 'uri0', l: 'label4', d: 'defn4'],
		[s: 'uri5', b: 'uri4', l: 'label5', d: 'defn5'],
		[s: 'uri6', b: 'uri5', l: 'label6', d: 'defn6'],
		[s: 'uri7', b: 'uri3', l: 'label7', d: 'defn7']
	]
	
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
	
}
