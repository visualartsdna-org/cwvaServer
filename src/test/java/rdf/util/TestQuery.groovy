package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.apache.jena.rdf.model.*
import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import rdf.tools.SparqlConsole
import org.apache.jena.reasoner.*

class TestQuery {

	def ju = new JenaUtilities()
	
	@Test
	void testSchema() {
		def url = new URL("http://localhost:8080/schema")
		def connection = url.openConnection() as HttpURLConnection
		
		try {
		    // Set the Accept header
			// 
		    //connection.setRequestProperty("Accept", "application/json")
			connection.setRequestProperty("Accept", "application/n-triples")
			
		    // Set request method (default is GET, but good practice to be explicit if needed)
		    connection.setRequestMethod("GET")
		
		    // Get the response code to initiate the request
		    def responseCode = connection.responseCode
		    println "Response Code: " + responseCode
		
		    // Read the response
		    if (responseCode == HttpURLConnection.HTTP_OK) {
		        def responseBody = connection.getInputStream().getText('UTF-8')
		        println "Response Body: " + responseBody
		    } else {
		        println "Request failed with response code: " + responseCode
		    }
		
		} catch (Exception e) {
		    e.printStackTrace()
		} finally {
		    // Disconnect the connection
		    if (connection != null) {
		        connection.disconnect()
		    }
		}
	}

	@Test
	void testAgentAvailable() {
		println isAgentAvailable()
	}
	def isAgentAvailable() {
		def health = new JsonSlurper().parse(new URL("http://localhost:8090/health"))
		return health.available
		//return health
	}
	@Test
	void testAgentShutdown() {
		println shutdownAgent()
	}
	def shutdownAgent() {
		def url = "http://localhost:8090/shutdown"
		try {
			def connection = new URL(url).openConnection()
			connection.requestMethod = 'POST'
			connection.doOutput = true
			connection.connect()
			def response = connection.responseCode
			return response == 200
		} catch (ConnectException ce) {
			false
		}
	}
	
	@Test
	void testSparqlEndpoint() {
		def pre="http://localhost:80/sparqlEndpoint?query="
		def q= """
PREFIX vad: <http://visualartsdna.org/model/>
PREFIX work: <http://visualartsdna.org/work/>
PREFIX the: <http://visualartsdna.org/thesaurus/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX schema: <https://schema.org/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT DISTINCT ?work ?title
WHERE {
  ?work a vad:Painting ;
        rdfs:label ?title ;
        the:tag ?tag .
  ?tag skos:related ?paint .
  ?paint rdfs:label ?paintLabel .
  FILTER(regex(?paintLabel, "cadmium yellow", 'i'))
}

"""
		def q0 = """
PREFIX vad: <http://visualartsdna.org/model/>
PREFIX work: <http://visualartsdna.org/work/>
PREFIX the: <http://visualartsdna.org/thesaurus/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX schema: <https://schema.org/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

SELECT DISTINCT ?tag ?tagLabel ?definition
WHERE {
  ?work rdfs:label ?title ;
        the:tag ?tag .
  FILTER(regex(?title, "circular courtyard", 'i'))
  { ?tag rdfs:label ?tagLabel } UNION { ?tag skos:prefLabel ?tagLabel }
  OPTIONAL { ?tag skos:definition ?definition }
}
"""
		def r = new URL("${pre}${URLEncoder.encode(q)}").getText()
		println r
	}
	
	@Test
	void testLoad() {
		def dataFile = "C:/stage/metadata/vocab/palette.ttl"
		Model data = ju.loadFiles(dataFile);
		println data.size()
	}

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
