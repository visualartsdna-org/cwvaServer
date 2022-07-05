package rdf

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import rdf.tools.*

import org.junit.jupiter.api.Test
import rdf.JenaUtils
import org.apache.jena.rdf.model.ModelFactory

class RdfTest {

	@Test
	void testTypeJsonld() {
		
		def ns = "work"
		def guid = "d8554014-f473-40ac-9a4e-363ac733ab06"
		def qs = new QuerySupport("C:/test/cwva/ttl/data")
		def lmdl = qs.queriesByType("vad:CreativeWork",ns,guid)
		
		def ljld = []
		lmdl.each { m->
			def baos = new ByteArrayOutputStream()
			m.write(baos,"JSON-LD")
			ljld += new JsonSlurper().parseText(""+baos)
			
		}
		ljld.each{m->
			println m
		}


	}
	
	// get superclass for instance type
	@Test
	void testGetSuperClass() {

		def ns = "work"
		def guid = "d8554014-f473-40ac-9a4e-363ac733ab06"
		def qs = new QuerySupport("C:/test/cwva/ttl/data")
		
		def q = qs.getType(ns,guid)
		println q
		
	}
	
	// get a query by code
	// run it for the specified work
	@Test
	void testType() {

		// 1. get query support
		// with prefixes from query collection
		def ns = "work"
		def guid = "d8554014-f473-40ac-9a4e-363ac733ab06"
		def qs = new QuerySupport("C:/test/cwva/ttl/data")
		
		// 2. run query for given work
		def lm = qs.queriesByType("vad:CreativeWork",ns,guid)
		lm.each{m->
			println new JenaUtils().saveModelString(m)
			println "-----------------------"
		}
	}
	
	@Test
	void testSelect() {

		// 1. get query support
		// with prefixes from query collection
		def ns = "work"
		def guid = "d8554014-f473-40ac-9a4e-363ac733ab06"
		def qs = new QuerySupport("C:/test/cwva/ttl/data")

		// 2. run query for given work
		def m = qs.query("c1ef23fc",ns,guid)
		println new JenaUtils().saveModelString(m)
	}
	
	@Test
	void test0() {

		//def file = "C:/test/cwva/ttl/data"
		def file = "C:/test/cwva/ttl/queries.ttl"
		def mdl = new JenaUtils().loadFiles(file)
		println "triples: ${mdl.size()}"
	}

}
