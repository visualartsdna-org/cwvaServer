package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import services.HtmlTemplate

class VocabQuery {

	// TODO: @Release This is a release task
	@Test
	void test() {
		def server = new cwva.Server()
		def vocab = server.cfg.vocab
		def target = "${server.cfg.dir}/html/vocab.html"
		def ju = new JenaUtilities()
		def mdl = ju.loadFiles(vocab)
		def l = ju.queryListMap1(mdl,"""
prefix xs: <http://www.w3.org/2001/XMLSchema#> 
prefix skos: <http://www.w3.org/2004/02/skos/core#> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
prefix the:	<http://visualartsdna.org/thesaurus#>
""","""
select distinct ?tc ?t ?s ?l ?d {
?s a skos:Concept .
?s skos:inScheme ?cs .
?cs skos:hasTopConcept ?tc .
?s skos:broader ?t .
?s skos:broader* ?tc .
?s rdfs:label ?l .
?s skos:definition ?d .
} order by ?tc ?t ?s
""")
		
		def sb = new StringBuilder()
		sb.append("""
<html>
<head/>
<body>
<H3>Visual Arts Vocabulary</H3>
<p/>
This vocabulary is a work in progress.
<p/>
<p/>
<table>
<tr><th>Top Concept</th><th>Parent</th><th>Concept</th><th>Label</th><th>Description</th></tr>
""")
		l.each{ m->
			sb.append "<tr><td>${fix(m.tc)}</td><td>${fix(m.t)}</td><td>${fix(m.s)}</td><td>${m.l}</td><td>${m.d}</td></tr>\n"
		}
		sb.append("""
</table>
<p/>
<p/>
<p/>
<p/>
${HtmlTemplate.tail}
</html>
""")
		new File(target).text = "$sb"
	}
	@Test
	void test2() {
		def vocab = "../cwva/ttl/data/vocab"
		def target = "../cwva/html/vocab.html"
		def ju = new JenaUtilities()
		def mdl = ju.loadFiles(vocab)
		def l = ju.queryListMap1(mdl,"""
prefix xs: <http://www.w3.org/2001/XMLSchema#> 
prefix skos: <http://www.w3.org/2004/02/skos/core#> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
prefix the:	<http://visualartsdna.org/thesaurus#>
""","""
select distinct ?tc ?t ?s ?l ?d {
?s a skos:Concept .
?s skos:inScheme ?cs .
?cs skos:hasTopConcept ?tc .
?s skos:broader ?t .
?s skos:broader* ?tc .
?s rdfs:label ?l .
?s skos:definition ?d .
} order by ?tc ?t ?s
""")
		def l2 = ju.queryListMap1(mdl,"""
prefix xs: <http://www.w3.org/2001/XMLSchema#>
prefix skos: <http://www.w3.org/2004/02/skos/core#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
prefix the:	<http://visualartsdna.org/thesaurus#>
""","""
select distinct ?l ?url ?d ?sl {
?col a skos:Collection .
?col rdfs:label ?l .
optional {?col skos:definition ?d . }
?col rdfs:seeAlso ?url .
?col skos:member ?s .
?s rdfs:label ?sl .
#?s skos:inScheme ?cs .
} order by ?l
""")

		def map = [:]
		l2.each{
			if (!map[it.l]) map[it.l] = [:]
			map[it.l].url = it.url
			if (!map[it.l].term) map[it.l].term = []
			map[it.l].term += it.sl
		}
		
		def sb = new StringBuilder()
		sb.append("""
<html>
<head/>
<body>
<H3>Visual Arts Vocabulary</H3>
<p/>
The intent of this vocabulary is not to constrain creative works, 
but to find new concepts and relationships within what is already known.
This vocabulary is a work in progress.
<p/>
<p/>
<table>
<tr><th>Top Concept</th><th>Parent</th><th>Concept</th><th>Label</th><th>Description</th></tr>
""")
		l.each{ m->
			sb.append "<tr><td>${fix(m.tc)}</td><td>${fix(m.t)}</td><td>${fix(m.s)}</td><td>${m.l}</td><td>${m.d}</td></tr>\n"
		}
		sb.append("""
</table>
<p/>
<p/>
<p/>
<h3>References</h3>
<p/>
<p/>
<table>
<tr><th>Collection</th><th>URL</th><th>Concepts</th></tr>
""")
		map.each{ k,v->
			sb.append """<tr><td>${fix(k)}</td><td><a href="${fix(v.url)}">${fix(v.url)}</a></td><td>${v.term}</td></tr>\n"""
		}
		sb.append"""
</table>
<p/>
<p/>
${HtmlTemplate.tail}
</html>
"""
		new File(target).text = "$sb"
	}

	def fix(s) {
		if (!s) return s
		s.replaceAll("http://visualartsdna.org/thesaurus#","the:")
	}
	
	@Test
	void test1() {
		def vocab = "../cwva/ttl/data/vocab"
		def ju = new JenaUtilities()
		def mdl = ju.loadFiles(vocab)
		def l = ju.queryListMap1(mdl,"""
prefix xs: <http://www.w3.org/2001/XMLSchema#> 
prefix skos: <http://www.w3.org/2004/02/skos/core#> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
prefix the:	<http://visualartsdna.org/thesaurus#>
""","""
select distinct ?t ?s ?l ?d {
?s a skos:Concept .
?s skos:broader* ?t .
?s rdfs:label ?l .
?s skos:definition ?d .
} order by ?t ?s
""")
		def sb = new StringBuilder()
		l.each{ m->
			sb.append "${m.t}\t${m.s}\t${m.l}\t${m.d}\n"
		}
		new File("../cwva/vocab.txt").text = "$sb"
	}
	
	@Test
	void test0() {
		def vocab = "../cwva/ttl/data/vocab"
		def ju = new JenaUtilities()
		def mdl = ju.loadFiles(vocab)
		def l = ju.queryListMap1(mdl,"""
prefix xs: <http://www.w3.org/2001/XMLSchema#> 
prefix skos: <http://www.w3.org/2004/02/skos/core#> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
prefix the:	<http://visualartsdna.org/thesaurus#>
""","""
select distinct ?t ?s ?l ?d {
?s a skos:Concept .
?s skos:broader* ?t .
?s rdfs:label ?l .
?s skos:definition ?d .
} order by ?t ?s
""")

		l.each{ m->
			println "${m.t}\t${m.s}\t${m.l}\t${m.d}"
		}
	}
	

}
