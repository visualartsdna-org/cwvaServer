package rdf.util.parser

class Keep {
	
	/**
	 * collect concepts from TKO-GKeep
	 *
	 * @param fn, filename
	 * @return
	 */
	def parseKeepConcepts(input) {
		
		def m = [:]
		def prev
		def text=false
		def title=false
		def key
		def header=true
		def topConceptDesc = ""
		input.eachLine{
			def s = it.trim()
			if (header && s != "") {
				topConceptDesc += s
				return
			}
			if (s=="" && prev=="") {
				text=false
				title=true
				header=false
				return
			}
			if (s=="") {
				text = true
				prev = s
				header=false
				return
			}
			if (title) {
				key = s.replaceAll(/[^A-Za-z_0-9]/,"")
				m[key]=[ann:[:],text:""]
				title=false
				prev = s
				return
			}
			if (text) {
				def ma = extractAnnotations(s)
				if (!key) return
				if (ma.isEmpty()) m[key].text += "$s\n"
				m[key].ann+= ma
				prev = s
			}
		}
		// capture only complete and consistent concepts
		def ld=[]
		m.each{k,v->
			if (!v.text && v.ann.isEmpty()) ld += k
		}
		ld.each{m.remove(it)}
		m.topConcept= topConceptDesc
		m
	}
	
	// get any annotations in text
	def extractAnnotations(s) {
		def ma = [:]
		def m = (s =~ /^\[([A-Za-z0-9_.]+)[ \t]*[=:][ \t]*(.*)\]$/)
		if (m) {
			ma[m[0][1]] = m[0][2]
		}
		ma
	}

}
