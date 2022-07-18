package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities

class TagTest0 {

	def ju = new JenaUtilities()
	
	def getData() {
		def ttl = "/temp/git/cwva/ttl/data"
		def mdl = ju.loadFiles(ttl)
		def l = ju.queryListMap1(mdl, """
prefix schema: <https://schema.org/> 
prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix work:  <http://visualartsdna.org/work/> 
prefix z0:    <http://visualartsdna.org/system/> 
prefix vad:   <http://visualartsdna.org/2021/07/16/model#> 
prefix tko:   <http://visualartsdna.org/takeout#> 
prefix skos:  <http://www.w3.org/2004/02/skos/core#> 
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 
prefix xs:    <http://www.w3.org/2001/XMLSchema#> 
prefix foaf:  <http://xmlns.com/foaf/0.1/> 
prefix dc:    <http://purl.org/dc/elements/1.1/> 
prefix voc:   <http://visualartsdna.org/voc#>
""", """
			
			select ?s ?label ?tag {
					?s a ?type .
					filter(?type in (vad:Watercolor))
					optional {?s vad:tag ?tag .}
					?s skos:label ?label
			} order by ?label
			""")
		//l.each{
		l
	}
	
	def printData(data,sb) {
		sb.append """
<table>
"""
		data.each { 
			sb.append "<tr><td>"
			sb.append """<input type="checkbox" id="voc1" name="voc1" value="${it.s}">"""
			sb.append """<label for="voc1">${it.label}</label><br>"""
			sb.append """${it.tag?it.tag.replaceAll("http://visualartsdna.org/voc#","voc:"):""}"""
			sb.append "</td></tr>"
		}
		sb.append """
</table>
"""
	}
	
	@Test
	void test() {
		
		def data = getData()
		def html = "/temp/junk/tag.html"
		def ttl = "/temp/git/cwva/ttl/data/vocab"
		def m = ju.loadFiles(ttl)
		def map = ["voc:visualArtTerm":[:]]
		
		def l = getNarrower(m, map, "voc:visualArtTerm")
		def sb = new StringBuilder()
		sb.append """
<!Doctype html>
<html>
<head>
<style>
table, th, td {
  #border: 1px solid black;
  border-collapse: collapse;
}
th, td {
  padding-top: 5px;
  padding-bottom: 5px;
  padding-left: 5px;
  padding-right: 5px;
}
</style>
    </head>
	<body>

<table>
<tr>
<td>
<div id="menu">
    <ul id="nav">
"""
		printHtml(map,0,sb)	
			
		sb.append """
    </ul>
</div>

</td>
<td>
<table>
<tr>
<td>
<form id="myForm" action="http://localhost:8082/tag.entry" method="get">

<p id="demo"></p>
<input type="hidden" id="menuitem" name="menuitem" value="">
	 <input type="button" name="tag" value="Tag">

</form>
</td>
<td>
"""
		printData(data,sb)
		sb.append """
</td>
</tr>
</table>
<br/>
</td>
</tr>
</table>
<script>
function save(x) {
  document.getElementById("menuitem").value=x;
  document.getElementById("demo").innerHTML = "Menu= " + document.getElementById("menuitem").value ;
}

</script>
</body>
</html>


"""
		new File(html).text = ""+sb
	}
	// TODO: <dt> tag, https://www.w3schools.com/tags/tag_dt.asp
	
	def printHtml(map,level,sb) {
		
		map.each { k,v->
			def s = """<li><button onclick="save('$k')">$k</button></li>"""
			sb.append "${tabs(level)}${s}"
			if (v) {
				sb.append "${tabs(level)}<ul>"
				printHtml(v,level+1,sb)
				sb.append "${tabs(level)}</ul>"
			}
		}
	}
	
	def print(map,level) {
		
		map.each { k,v->
			println "${tabs(level)}$k"
			if (v) print(v,level+1)
		}
	}
	
	def tabs(n) {
		def s = ""
		for (int i=0;i<n;i++) {
			s += "\t"
		}
		s
	}
	
	def getNarrower(mdl, map, term) {

		def l = ju.queryListMap1(mdl, """
prefix schema: <https://schema.org/> 
prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix work:  <http://visualartsdna.org/work/> 
prefix z0:    <http://visualartsdna.org/system/> 
prefix vad:   <http://visualartsdna.org/2021/07/16/model#> 
prefix tko:   <http://visualartsdna.org/takeout#> 
prefix skos:  <http://www.w3.org/2004/02/skos/core#> 
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 
prefix xs:    <http://www.w3.org/2001/XMLSchema#> 
prefix foaf:  <http://xmlns.com/foaf/0.1/> 
prefix dc:    <http://purl.org/dc/elements/1.1/> 
prefix voc:   <http://visualartsdna.org/voc#>
""", """

select ?s {
		?s a skos:Concept .
		?s skos:broader $term
} order by ?s
""")
		l.each{
			def st = it.s.replaceAll("http://visualartsdna.org/voc#","voc:")
			map["$term"]["$st"] = [:]
			//println "$st"
			getNarrower(mdl, map["$term"], st)
		}
	}
	
	
	

}
