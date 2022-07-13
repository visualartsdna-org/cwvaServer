package services

import util.Args

class DriveSvg2Html {

	public static void main(String[] args){

		def map = Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, services.Driver ...
"""
			return
		}

		if (map.containsValue("print")){
			map.each{k,v->
				println "\t$k = $v"
			}
			return
		}

		def html = map["html"]
		def svg = map["svg"]

		assert html , "no html"
		assert svg , "no svg"
		
		new Svg2Html().convert(html,svg,"VisualArtsDNA Ontology")
		
	}
}
