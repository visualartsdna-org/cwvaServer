package services

import static org.junit.Assert.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import rdf.JenaUtils

import org.junit.Test


class BrowseWorks {
	

	@Test
	public void test() {
		def s = new BrowseWorks().browse("C:/stage/planned/node/ttl/art.ttl")
		println s
	}
	
	def browse(host,ttl) {
		def m = new JenaUtils().loadDirModel(ttl)
//		def m = new JenaUtils().loadFileModelFilespec(ttl)
		def l = new JenaUtils().queryListMap1(m, """
prefix vad:	<http://visualartsdna.org/2020/04/painting/>
prefix work:	<http://visualartsdna.org/work/>
prefix skos: <http://www.w3.org/2004/02/skos/core#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
""", """
select ?s ?lab ?image {
 ?s rdfs:label ?lab .
optional {?s	vad:image ?image }
 filter(!isblank(?s))
} order by ?lab
"""
)
	def sb = new StringBuilder()
	sb.append HtmlTemplate.head(host)
	sb.append HtmlTemplate.tableHead("Works")
	
	l.each { 
		def s = it.s.replaceAll("http://visualartsdna.org",host)
		if (!it.containsKey("image"))
			sb.append """<tr><td><a href="$s">${it.lab}</a></td></tr>\n"""
		else sb.append """<tr height="40"><td><a href="$s">
			${it.lab} <img src="${it.image}" width="50" height="40">
			</a></td></tr>
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
