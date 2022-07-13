package services

import services.Svg2Html
import util.Args

class OntoToDotDriver {
	
	/**
	 * Perform the ontology 
	 * rendering in svg/html via dot
	 * @param args
	 */
	public static void main(String[] args){

		def map = Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, rdf.visual.OntoToDotDriver -ttl ttl -dot dot -svg svg -html html
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
		s2h.convert(html,svg,"VisualArtsDNA Ontology")
	
	}


}
