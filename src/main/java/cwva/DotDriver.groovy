package cwva

import rdf.JenaUtilities
import rdf.tools.SparqlConsole
import util.Tmp
import services.Svg2Html

class DotDriver extends BaseDriver {
	
	def doGet(ttl,kind,html) {
		doGet(ttl,kind,html,[:])
	}
	def ju = new JenaUtilities()
	
	def doGet(ttl,kind,html,mq) {

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

			case "svgVocab2":
				def svg = Tmp.getTemp(".svg")
				def dot = Tmp.getTemp(".dot")
				def ttlTmp = Tmp.getTemp(".ttl")
				def m = ju.loadFiles(ttl)
				ju.saveModelFile(m, ttlTmp, "TTL")
				def schemes = makeSchemeList(m,mq)
				def includeDefs = mq.defn
				services.VocabToDotDef.driver(ttl,dot,schemes,includeDefs)
				drive(ttl, dot, html, svg,"Schemes: $schemes")
				Tmp.delTemp(svg)
				Tmp.delTemp(dot)
				Tmp.delTemp(ttlTmp)
				break

			case "svgModel2":
				def svg = Tmp.getTemp(".svg")
				def dot = Tmp.getTemp(".dot")
				def ttlTmp = Tmp.getTemp(".ttl")
				def m = ju.loadFiles(ttl)
				ju.saveModelFile(m, ttlTmp, "TTL")
				def schemes = makeSchemeList(m,mq)
				def includeDefs = mq.defn
				services.OntoToDotDef.driver(ttl,dot,schemes,includeDefs)
				drive(ttl, dot, html, svg,"Schemes: $schemes")
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
	
	def makeCuri(m,s) {
		def list = ju.getPrefix(m,s)
		def prefix = list[0]
		if (prefix == "cwva:") prefix = "vad:"
		"$prefix${list[1]}"
	}
	
	def makeSchemeList(m,mq) {
		
		def lvg = mq.vocabGraph
		def schemeList = ""
		if (lvg instanceof List) {
			int i=0
			lvg.each{
				if (i++) schemeList += ", "
				schemeList += "${makeCuri(m,it)}"
			}
		} else {
			schemeList = makeCuri(m,lvg)
		}
		schemeList
	}	
}
