package cwva

import rdf.JenaUtilities
import util.Tmp

class DotDriver extends BaseDriver {

	def doGet(ttl,kind,html) {

		switch(kind) {
			case "svg":
				def svg = Tmp.getTemp(".svg")
				def dot = Tmp.getTemp(".dot")
				def args = toArgs("""
				   -ttl $ttl -dot $dot -html $html -svg $svg""")
				services.OntoToDotDriver.main(args)
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
				def args = toArgs("""
				   -ttl $ttlTmp -dot $dot -html $html -svg $svg""")
				services.VocabToDotDriver.main(args)
				Tmp.delTemp(svg)
				Tmp.delTemp(dot)
				Tmp.delTemp(ttlTmp)
				break

			case "svgImpOnto":
				def svg = Tmp.getTemp(".svg")
				def dot = Tmp.getTemp(".dot")
				def args = toArgs("""
				   -ttl $ttl -dot $dot -html $html -svg $svg""")
				services.InstEffectOntoToDotDriver.main(args)
				Tmp.delTemp(svg)
				Tmp.delTemp(dot)
				break

			default:
				break
		}
	}
	
}
