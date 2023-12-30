package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.tools.SparqlConsole

import org.apache.jena.rdf.model.*

class PolicyTest {

	def ju = new JenaUtilities()
	@Test
	void testRecurseDirRDFS() {
		long ctms = System.currentTimeMillis()
		def data = ju.loadDirRecurseModel("C:/temp/git/cwvaContent/ttl/data")
		println "data ${data.size()} in ${System.currentTimeMillis() - ctms} ms"
		ctms = System.currentTimeMillis()
		def schema = ju.loadDirRecurseModel("C:/temp/git/cwvaContent/ttl/model")
		println "schema ${schema.size()} in ${System.currentTimeMillis() - ctms} ms"
		
		// provenance data optional
//		def prov = ju.loadDirRecurseModel("C:/temp/git/cwvaContent/ttl/provenance")
//		println "prov ${prov.size()} in ${System.currentTimeMillis() - ctms} ms"
//		data.add(prov)

		def rdfs = ModelFactory.createRDFSModel(schema, data);
		
		Policy.exec(rdfs)
		new SparqlConsole().show(rdfs)
	}

	@Test
	void test() {
		def l=[]
		def s=""
		new File("../cwva/Policy.upd").text.eachLine{
			if (it.startsWith("# update delimiter")) {
				if (s != "") l += s
				s=""
			}
			if (it.startsWith("#")) return
			s += "$it\n"
		}
		if (s != "") l += s
		
		l.each { println it}
	}
	
}
