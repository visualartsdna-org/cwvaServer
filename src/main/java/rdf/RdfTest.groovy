package rdf

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import rdf.tools.*
import rdf.util.DBMgr
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import org.apache.jena.rdf.model.ModelFactory

class RdfTest {
	
	def content = "/temp/git/cwvaContent"
	
	def dbm = new DBMgr([
			data: "$content/ttl/data",
			vocab: "$content/ttl/vocab",
			tags: "$content/ttl/tags",
			model: "$content/ttl/model"
			])


}
