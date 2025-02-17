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
	
	def browse(host,m,qm) {
		def order = ""
		if (qm.order == "Title") {
			order = "order by ?artist ?label"
		} else if (qm.order == "Date") {
			order = "order by ?artist desc(?date) ?label"
		}

		def archiveThreshold = """"2010-01-01"^^xs:date"""
		def filter = ""
		if (qm.archived) {
			filter = """filter(year(?dc) < year($archiveThreshold))"""
		} else {
			filter = """filter(year(?dc) > year($archiveThreshold))"""
		}
		
		def l = new JenaUtils().queryListMap1(m, 
			rdf.Prefixes.forQuery, """
select ?s ?label ?a ?artist {
 ?s rdfs:label ?label .
 ?s schema:dateCreated ?date .
 ?s vad:hasArtistProfile/vad:artist  ?a .
 ?a foaf:name ?artist .
 ?s a vad:CreativeWork .
 ?s schema:dateCreated ?dc .
 $filter
} $order
"""
)
	def sb = new StringBuilder()
	sb.append HtmlTemplate.head(host)
	sb.append """
<script>
function myFunction() {
  document.getElementById("myForm").submit();
}
</script>
<h6>
<form id="myForm" action="/browseSort" method="get">
Order:
<input type="radio" id="title" name="order" onclick="myFunction()" value="Title" ${qm.order=="Title" ? "checked" : ""}>
<label for="title">Title</label>
<input type="radio" id="date" name="order" onclick="myFunction()" value="Date" ${qm.order=="Date" ? "checked" : ""}>
<label for="date">Date</label>
<!--
<br><label for="archived" align="right">Archived</label>
<input type="checkbox" id="archived" name="archived" onclick="myFunction()" value="Archived" ${qm.archived ? "checked" : ""}>
-->
</form>
</h6>
"""
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
