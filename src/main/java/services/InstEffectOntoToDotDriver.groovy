package services
import services.Svg2Html
import rdf.JenaUtils
import util.Args

class InstEffectOntoToDotDriver {
	
	/**
	 * Perform the ontology 
	 * rendering in svg/html via dot
	 * @param args
	 */
	public static void main(String[] args){

		def map = Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, services.OntoToDotDriver -ttl ttl -dot dot -svg svg -html html
Perform the ontology rendering in svg/html via dot
"""
			return
		}
		def ttl = map["ttl"]
		assert ttl , "no ttl"
		def dot = map["dot"]
		assert dot , "no dot"
		def svg = map["svg"]
		assert svg , "no svg"
		def html = map["html"]
		assert html , "no html"
		
		def effectiveOnto = "${System.getProperty("java.io.tmpdir")}/effectiveOnto.ttl"

		InstEffectOntoToDotDriver ieotdd = new InstEffectOntoToDotDriver()
		
		ieotdd.getEffectiveOnto(ttl,effectiveOnto)
		ieotdd.graph(effectiveOnto,dot,svg,html)
		
	}
	
	def graph(ttl,dot,svg,html) {
		
		OntoToDot.driver(ttl,dot)
		
		// dot -Tsvg -o test100.svg test100.dot
		Process process = "dot -Tsvg -o $svg $dot".execute()
		def out = new StringBuffer()
		def err = new StringBuffer()
		process.consumeProcessOutput( out, err )
		process.waitFor()
		if( err.size() > 0 ) throw new Exception( ""+err)
		if( out.size() > 0 ) println "$out"
		
		def s2h = new Svg2Html()
		s2h.convert(html,svg,"Effective Ontology")
		
	
	}
	

	def getEffectiveOnto(ttl,tempOnto) {
		
//		def ttl2 = "C:/stage/planned/node/ttl/cwva.ttl"
		JenaUtils ju = new JenaUtils()
		def model = new JenaUtils().loadDirModel(ttl)
		//def model = ju.loadFileModelFilespec(ttl)
//		def onto = ju.loadFileModelFilespec(ttl2)
//		model.add(onto)
		def mobj = ju.queryExecConstruct(model,rdf.Prefixes.forQuery, """
# object prop generator
construct{
	?p
	a owl:ObjectProperty ;
	rdfs:domain ?t ;
	rdfs:range ?to ;
.
} {
		?s ?p ?o .
		?s a ?t .
		filter (! isLiteral(?o))
		#filter (isIRI(?o))
		optional {?o a ?to .}
		} order by ?p

""")


def mlit = ju.queryExecConstruct(model,rdf.Prefixes.forQuery, """
# literal property generator
construct{
	?p
	a owl:DatatypeProperty ;
	rdfs:domain ?t ;
	rdfs:range ?to ;
.
} {
		?s ?p ?o .
		?s a ?t .
		filter (isLiteral(?o))
		bind(datatype(?o) as ?to)
		} order by ?p

""")


def mcls = ju.queryExecConstruct(model,rdf.Prefixes.forQuery, """
# class generator
			
construct{
	?t
  rdf:type owl:Class ;
  rdfs:subClassOf ?sc ;
.
} {
		?s ?p ?o .
		?s a ?t .
		optional {?t rdfs:subClassOf ?sco }
		bind(if(!bound(?sco),owl:Thing,?sco) as ?sc)
		} order by ?t

""")

	def mout = mcls
	mout.add(mlit)
	mout.add(mobj)
	ju.saveModelFile(mout, tempOnto, "ttl")
	}

}
