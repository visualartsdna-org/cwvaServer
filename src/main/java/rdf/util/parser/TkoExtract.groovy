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
				if (c.isTrashed || c.isArchived) return
//				if (file.name =="Drawings collection.json") {
//					println "here"
//				}
				if (map.containsKey(c.title))
					throw new RuntimeException("title already exists")
				map[c.title]=c
			}
		else {
			def c = Rson.load(f.absolutePath)
			map[c.title]=c
		}
		
		map
	}
	
	def processLabels(base) {
		def labelsMap = [:]
		new File("$base/Labels.txt").text.eachLine{
			labelsMap[it] = []
		}
		labelsMap
	}


	def hasLabel(c,label) {
		def l = c.labels
		l.find{
			it.name == label
		}
	}
}
