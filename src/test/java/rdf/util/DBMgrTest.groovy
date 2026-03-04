package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.tools.SparqlConsole

class DBMgrTest {

	def content = "C:/temp/git/cwvaContent"
//	def content = "C:/stage/cwvaContent"
	
	// std rdfs db load
	// to sparql console
	@Test
	void test() {
		def map = [
			data: "$content/ttl/data",
			vocab: "$content/ttl/vocab",
			tags: "$content/ttl/tags",
			model: "$content/ttl/model",
			dir: "/temp/git/cwva"
			]
		
		def dbm = new DBMgr()
		dbm.cfg = map
		dbm.loadDb()
		dbm.print()
		new SparqlConsole().show(dbm.rdfs)
	}
	
	@Test
	void test1() {
		def map = [
			data: "$content/ttl/data",
			vocab: "$content/ttl/vocab",
			tags: "$content/ttl/tags",
			model: "$content/ttl/model"
			]
		def dbm = new DBMgr(map)
		dbm.print()
		
		def ctms = System.currentTimeMillis()
		
		dbm.reload()
		dbm.print()
		
		println "${System.currentTimeMillis() - ctms} ms"

	}
	

}
