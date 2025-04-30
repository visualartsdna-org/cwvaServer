package cwva

import rdf.JenaUtilities
import util.Tmp
import services.Svg2Html

class DotDriver extends BaseDriver {

	def doGet(ttl,kind,html) {

		switch(kind) {
			case "svg":
				def svg = Tmp.getTemp(".svg")
				def dot = Tmp.getTemp(".dot")
				services.OntoToDotDef.driver(ttl,dot)
				drive(ttl, dot, html, svg,"VisualArtsDNA Ontology")
				Tmp.delTemp(svg)
				Tmp.delTemp(dot)
				break

			case "svgVocab":
				def ju = new JenaUtilities()
				def svg = Tmp.getTemp(".svg")
				def dot = Tmp.getTemp(".dot")
				def ttlTmp = Tmp.getTemp(".ttl")
				def m = ju.loadFiles(ttl)
				ju.saveModelFile(m, ttlTmp, "TTL")
				services.VocabToDotDef.driver(ttl,dot)
				drive(ttl, dot, html, svg,"VisualArtsDNA Thesaurus")
				Tmp.delTemp(svg)
				Tmp.delTemp(dot)
				Tmp.delTemp(ttlTmp)
				break

			case "svgImpOnto":
				throw new RuntimeException("svgImpOnto not implemented")
				
				def svg = Tmp.getTemp(".svg")
				def dot = Tmp.getTemp(".dot")
				services.InstEffectOntoToDotDriver.drive(ttl, dot, html, svg)
				Tmp.delTemp(svg)
				Tmp.delTemp(dot)
				break

			default:
				break
		}
	}
	
	def drive(ttl, dot, html, svg, title) {
		
		// dot -Tsvg -o test100.svg test100.dot
		Process process = "dot -Tsvg -o $svg $dot".execute()
		def out = new StringBuffer()
		def err = new StringBuffer()
		process.consumeProcessOutput( out, err )
		process.waitFor()
		if( err.size() > 0 ) throw new Exception( ""+err)
		if( out.size() > 0 ) println "$out"
		
		def s2h = new Svg2Html()
		s2h.convert(html,svg,title)

	}
	
}
