package support.script

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import org.apache.jena.rdf.model.*

class CvtTkoInst {

	@Test
	void test() {
		def ju = new JenaUtilities()
		def infile = "C:/temp/git/cwva/gkeep/tko" ///tratsi"
		def outfile = "C:/temp/git/cwva/ttl/data/tags"
//		def outmodel = ju.newModel()
		def data = ju.loadFiles(infile)
//		def model = ModelFactory.createRDFSModel(data,
//			outmodel)
		def model = ju.queryExecConstruct(data, """
prefix schema: <https://schema.org/> 
prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix work:  <http://visualartsdna.org/work/> 
prefix z0:    <http://visualartsdna.org/system/> 
prefix vad:   <http://visualartsdna.org/2021/07/16/model#> 
prefix tko:   <http://visualartsdna.org/takeout#> 
prefix skos:  <http://www.w3.org/2004/02/skos/core#> 
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 
prefix xs:    <http://www.w3.org/2001/XMLSchema#> 
prefix foaf:  <http://xmlns.com/foaf/0.1/> 
prefix dc:    <http://purl.org/dc/elements/1.1/> 
prefix the:   <http://visualartsdna.org/thesaurus#>
""", """
construct {
		?s vad:tag ?tag .
}{
		?bn tko:tags ?t .
		?bn schema:identifier ?id .
		bind(iri(concat("http://visualartsdna.org/work/",?id)) as ?s)
		bind(iri(concat("http://visualartsdna.org/thesaurus#",?t)) as ?tag)
		
}
""")
		println ju.saveModelString(model)
		
	}

}
