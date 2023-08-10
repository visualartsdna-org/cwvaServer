package rdf.util.parser

import util.Rson

class TkoExtract {
	
	def process(src) {

		def map = [:]
		def f = new File(src)
		
		if (f.isDirectory())
			f.eachFile {file->
				
				if (!(file.name.endsWith(".json"))) return
	
				println "$file"
				
				def c = Rson.load(file.absolutePath)
				if (!hasLabel(c,"publish")) return
				map[c.title]=c.textContent
			}
		else {
			def c = Rson.load(f.absolutePath)
			map[c.title]=c.textContent
		}
		map
	}


	def hasLabel(c,label) {
		def l = c.labels
		l.find{
			it.name == label
		}
	}
}
