package services

import static org.junit.Assert.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import rdf.JenaUtilities
import rdf.JenaUtils
import rdf.QuerySupport
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

// refactor l into maps per artist
		def ma=[:]
		l.each{
			if (!ma.containsKey(it.artist)) {
				ma[it.artist]=[]
			}
			ma[it.artist] += [label:it.label,s:it.s]
		}


	def sb = new StringBuilder()
	sb.append HtmlTemplate.head(host)
	sb.append """
<table>
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
//	sb.append HtmlTemplate.tableHead("Works")
	sb.append """
<table>
<style>
td {
  vertical-align: top;
}
</style>
<tr>
<td vertical-align: bottom>

  <i>Artists</i><br><br>
"""
	ma.each{k,v->
		sb.append """<h4><a href="#$k">$k</a></h4>"""
	}
	sb.append """
</td><td>
<style>
.container
{
	max-height:${qm.isMobile == "true" ? "400" : "800"}px;
	overflow-y:scroll
}
</style>
<div class="container">
"""
	
	ma.each {k,v->
		sb.append """<div id="$k"></div>
		<b><i>Works by $k</i></b>
"""
		
		
		v.each { 
			def s = it.s.replaceAll("http://visualartsdna.org",host)
			//def a = it.a.replaceAll("http://visualartsdna.org",host)
			
			sb.append """<br>
				<a href="$s">${it.label}</a>
	
	
	"""
		}
		sb.append """<br><hr><br>
"""		
	}
	def uri= "http://visualartsdna.org/thesaurus".replaceAll("http://visualartsdna.org",host)
	sb.append """
</div></td>
${qm.isMobile == "true" ? "</tr><tr><td></td>" : ""}
<td>
<h5>Concept Collections</h5>
"""
	
	def lc = new QuerySupport().queryCollections()
	lc.each{
		sb.append """
<a href="${it.s.replaceAll("http://visualartsdna.org",host)}">${it.l}</a><br>
"""
	}
	sb.append """
</td></tr></table>
"""
//	sb.append HtmlTemplate.tableTail
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
