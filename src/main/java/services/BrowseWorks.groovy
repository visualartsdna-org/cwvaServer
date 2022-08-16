package services

import static org.junit.Assert.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import rdf.JenaUtilities
import rdf.JenaUtils

import org.junit.Test


class BrowseWorks {
	

	@Test
	public void test() {
		def s = new BrowseWorks().browse("C:/stage/planned/node/ttl/art.ttl")
		println s
	}
	
	def browse(host,m) {

		def l = new JenaUtils().queryListMap1(m, 
			rdf.Prefixes.forQuery, """
select ?s ?label ?a ?artist {
 ?s rdfs:label ?label .
 ?s vad:hasArtistProfile/vad:artist  ?a .
 ?a foaf:name ?artist .
 ?s a vad:CreativeWork .
} order by ?artist ?lab
"""
)
	def sb = new StringBuilder()
	sb.append HtmlTemplate.head(host)
	sb.append HtmlTemplate.tableHead("Works")
	
	l.each { 
		def s = it.s.replaceAll("http://visualartsdna.org",host)
		def a = it.a.replaceAll("http://visualartsdna.org",host)
		
		sb.append """<tr><td>
			
			<a href="$s">${it.label}</a>
</td><td>
			<a href="$a">${it.artist}</a>
</td></tr>

"""
	}
	sb.append HtmlTemplate.tableTail
	sb.append HtmlTemplate.tail
	
	""+sb
	}

	@Test
	public void test0() {
		def m = new JenaUtils().loadFileModelFilespec("/temp/art.ttl")
		def l = new JenaUtils().queryListMap1(m, """
prefix vad:	<http://visualartsdna.org/2020/04/painting/>
prefix work:	<http://visualartsdna.org/work/>
prefix skos: <http://www.w3.org/2004/02/skos/core#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
""", """
select ?s ?lab {
 ?s rdfs:label ?lab ;
	.
}
"""
)
	l.each { 
		println "${it.s}, ${it.lab}"
	}
	}

}
