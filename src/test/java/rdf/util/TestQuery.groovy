package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.*
import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import rdf.tools.SparqlConsole
import org.apache.jena.reasoner.*

class TestQuery {

	def ju = new JenaUtilities()
		
	@Test
	void test() {
		// SparqlConsole loader
		def data = [
			"/temp/git/cwvaContent/ttl/data",
			"/temp/git/cwvaContent/ttl/tags",
			"/temp/git/cwvaContent/ttl/vocab",
			]
		def schema = "/temp/git/cwvaContent/ttl/model"

		rdf.tools.Loader.loadInf(data,schema)
	}
	
	@Test
	void testOwl() {
		def data = "/temp/git/cwvaContent/ttl"
		def schema = "/temp/git/cwvaContent/ttl/model/cwva.ttl"
		loadOwl(data,schema)
	}
	
	def loadOwl(dataFile,schemaFile) {

		Model data = ju.loadFiles(dataFile);
		Model schema = ju.loadOntImports(schemaFile)
		//ju.loadFiles(schemaFile);
		def reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
		ValidityReport validity = infmodel.validate();
		if (validity.isValid()) {
			System.out.println("OK");
		} else {
			System.out.println("Conflicts");
			for (Iterator i = validity.getReports(); i.hasNext(); ) {
				ValidityReport.Report report = (ValidityReport.Report)i.next();
				System.out.println(" - " + report);
			}
		}
		//new SparqlConsole().show(infmodel)
	}


}
