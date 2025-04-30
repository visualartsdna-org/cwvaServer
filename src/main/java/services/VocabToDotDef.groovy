package services
import org.apache.commons.text.WordUtils

import rdf.JenaUtils

class VocabToDotDef {

	JenaUtils ju = new JenaUtils()
	def model
	// https://imagecolorpicker.com/color-code
	def propColor = "#44b5dc"
	def literalColor = "#E3B077"
	def classColor = "#5eeed2"
	def labelIds = [:]
	def wrapWidth = 20


	VocabToDotDef(ttl){
		model = ju.loadFiles(ttl)
	}

	static def driver(ttl,dot) {

		def otd = new VocabToDotDef(ttl)
		def s = "${otd.getProlog()}"
		def l = otd.getNodes()
		l.each{
			s += "$it\n"
		}
//		def lp = otd.getPropNodes()
//		lp.each{
//			s += "$it\n"
//		}
		def list = otd.getLabels()
		list[0].each{
			s += "$it\n"
		}
		list[1].each{
			s += "$it\n"
		}
//		def listProp = otd.getPropLabels()
//		listProp[0].each{
//			s += "$it\n"
//		}
//		listProp[1].each{
//			s += "$it\n"
//		}
		s += "${otd.getEnd()}"
		new File(dot).text = s
	}

	def getProlog() {
		"""digraph G {
  rankdir=BT
  node[style="filled" height=.3] ranksep=5
"owl:DatatypeProperty" [fillcolor="$classColor" color="$classColor" label="owl:DatatypeProperty"]
"owl:ObjectProperty" [fillcolor="$classColor" color="$classColor" label="owl:ObjectProperty"]
"owl:Thing" [fillcolor="$literalColor" color="$classColor" label="owl:Thing"]
"skos:Concept" [fillcolor="$classColor" color="$classColor" label="skos:Concept"]


"""
	}

	def getEnd() {
		"}"
	}

	def getPrefixedLabel(s) {
		if (s==null) {
			//println "here"
			return s
		}
		def n = s.lastIndexOf("#")
		if (n<0) n = s.lastIndexOf("/")
		def label = (s).substring(n+1)
		def ns = (s).substring(0,n+1)
		def pre = model.getNsURIPrefix(ns)
		"$pre:$label"
	}

	def getPropNodes() {

		def list = ju.queryListMap1(model,"","""
${rdf.Prefixes.forQuery}
SELECT ?s ?l ?d ?r {
  { ?s a ?property } UNION { ?s owl:subPropertyOf+ ?o . ?o a ?property }
  FILTER ( ?property IN ( owl:DatatypeProperty, owl:ObjectProperty ) )
 #optional { ?s rdfs:label ?l }
 optional { ?s rdfs:domain ?d }
 optional { ?s rdfs:range ?r }

} order by ?s
""")

		def l = []
		list.each{
			def label = getPrefixedLabel(it.s)
			def color = propColor
			def s= """"${label}" [fillcolor="$color" color="$color" label="${label}"]"""
			l.add s
		}
		l
	}

	def getNodes() {

		def list0 = ju.queryListMap1(model,"","""
${rdf.Prefixes.forQuery}
SELECT ?s ?sc ?l ?c {
  { ?s a skos:Concept } 
#  { ?s a skos:Concept } UNION
#  { ?s skos:broader+ ?o . ?o a skos:Concept . }
 optional { ?s skos:broader ?sc }
 #optional { ?s rdfs:label ?l }
 optional { ?s skos:definition ?c }
 #optional { ?s rdfs:label ?pl }
		} order by ?s
""")

		def list=[]
		list0.each{
				def m=[:]
				m.s = getPrefixedLabel(it.s)
				m.sc = getPrefixedLabel(it.sc)
				m.l = it.l
				m.c = it.c
				list += m
		}
		def l = []
		list.each{
			def label = it.s
			def color = classColor
			def notLeaf = list.find{c->
				c.sc == it.s
			}
			if (notLeaf) color = classColor
			def s= """"${it.s}" [fillcolor="$color" color="$color" label="${label}"]"""
			l.add s
		}
		l
	}

	def getPropLabels() {

		def list0 = ju.queryListMap1(model,"","""
${rdf.Prefixes.forQuery}
SELECT ?s ?l ?d ?r ?t {
  { ?s a ?property } UNION { ?s owl:subPropertyOf+ ?o . ?o a ?property }
  FILTER ( ?property IN ( owl:DatatypeProperty, owl:ObjectProperty ) )
 #optional { ?s rdfs:label ?l }
 optional { ?s rdfs:domain ?d }
 optional { ?s rdfs:range ?r }
 ?s a ?t
} order by ?s
""")

		def list = []
		list0.each{
			def m=[:]
			m.s = getPrefixedLabel(it.s)
			m.d = getPrefixedLabel(it.d)
			m.r = getPrefixedLabel(it.r)
			m.l = it.l
			list += m
		}


		def l = []
		def g = []
		list.each{
			if (it.l) {
				def s = it.l
				def id = s.hashCode()
				l.add """"$id" [fillcolor="$literalColor" color="$literalColor" label="${fixLabel(s)}" shape="rect"]"""
				g.add """"${it.s}" -> "$id" [label="rdfs:label"]"""
			}
			if (it.d) {
				def s = it.d
				l.add """"$s" [fillcolor="$classColor" color="$classColor" label="${fixLabel(s)}"]"""
				g.add """"${it.s}" -> "$s" [label="rdfs:domain"]"""
			}
			if (it.r) {
				def s = it.r
				l.add """"$s" [fillcolor="$classColor" color="$classColor" label="${fixLabel(s)}"]"""
				g.add """"${it.s}" -> "$s" [label="rdfs:range"]"""
			}
			if (it.t) {
				def s = it.t
				l.add """"$s" [fillcolor="$classColor" color="$classColor" label="${fixLabel(s)}"]"""
				g.add """"${it.s}" -> "$s" [label="a"]"""
			}
		}
		[l, g]
	}

	def getLabels() {

		def list0 = ju.queryListMap1(model,"","""
${rdf.Prefixes.forQuery}
SELECT ?s ?sc ?l ?c ?pl {
  { ?s a skos:Concept } 
#  { ?s a skos:Concept } UNION
#  { ?s skos:broader+ ?o . ?o a skos:Concept . }
 optional { ?s skos:broader ?sc }
 #optional { ?s rdfs:label ?l }
 optional { ?s skos:definition ?c }
 #optional { ?s rdfs:label ?pl }
		} order by ?s

""")

		def list = []
		list0.each{
			def m=[:]
			m.s = getPrefixedLabel(it.s)
			m.sc = getPrefixedLabel(it.sc)
			m.c = it.c
			m.l = it.l
			m.pl = it.pl
			list += m
		}

		def l = []
		def g = []
		list.each{
			if (it.l) {
				def s = it.l
				def id = s.hashCode()
				l.add """"$id" [fillcolor="$literalColor" color="$literalColor" label="${fixLabel(s)}" shape="rect"]"""
				g.add """"${it.s}" -> "$id" [label="rdfs:label"]"""
			}
			if (it.c) {
				def s = it.c
				def id = s.hashCode()
				l.add """"$id" [fillcolor="$literalColor" color="$literalColor" label="${fixLabel(s)}" shape="rect"]"""
				g.add """"${it.s}" -> "$id" [label="skos:definition"]"""
			}
			if (it.pl) {
				def s = it.pl
				def id = s.hashCode()
				l.add """"$id" [fillcolor="$literalColor" color="$literalColor" label="${fixLabel(s)}" shape="rect"]"""
				g.add """"${it.s}" -> "$id" [label="rdfs:label"]"""
			}
			if (it.sc) {
				def s = it.sc
				l.add """"$s" [fillcolor="$classColor" color="$classColor" label="${fixLabel(s)}"]"""
				g.add """"${it.s}" -> "$s" [label="skos:broader"]"""
			}
		}
		[l, g]
	}

	def fixLabel(s) {
		def s0 = fixQuotes(s)
		//s0.replaceAll(/[ ]+/,"\\\\n")
		WordUtils.wrap(s0,WrapWidth)
	}

	def fixQuotes(s) {
		if (!s.contains('"')) return s
		def s1 = s.replaceAll(/"/,"\\\\\"")
		s1
	}
}
