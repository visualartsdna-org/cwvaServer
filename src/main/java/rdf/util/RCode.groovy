package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class RCode {

	def dec2hex2(dec) {

		"${Integer.toHexString(dec)}"
	}

	@Test
	void testCode() {
		def q = """# print just the tags for the work
prefix the:	<http://visualartsdna.org/thesaurus#>
prefix tko:   <http://visualartsdna.org/takeout#>
#select distinct ?title ?label ?def
construct {
[
#tko:title ?title ;
tko:label ?label ;
tko:def ?def ;

]

}
{ select distinct ?title ?label ?def {
	bind (work:d8554014-f473-40ac-9a4e-363ac733ab06 as ?s)
	?s ?p ?o .
	?s schema:identifier ?si .
	?t schema:identifier ?ti .
	?t tko:color "DEFAULT" .
	?t ?p2 ?o2 .
	filter (?si = ?ti)
	filter (!isBlank(?o))
	filter (!isBlank(?o2))
	?t tko:title ?title .
	?t tko:tags ?label .
	?tag rdfs:label ?label .
	?tag skos:definition ?def .
} 
} 
"""
		def code = dec2hex2(q.hashCode())
		println code
	}


}
