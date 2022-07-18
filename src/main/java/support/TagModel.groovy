package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities

class TagModel {

// TODO: <dt> tag, https://www.w3schools.com/tags/tag_dt.asp
	def ju = new JenaUtilities()
	def instances
	def concepts
	TagModel(instances,concepts){
		this.instances = instances
		this.concepts = concepts
	}
	
	def handleQueryParams(m) {
		def html = ""

		process(m.conceptRoots)
	}
	
	def process(rootCpt) {
		
		def data = getData(instances)
		def m = ju.loadFiles(concepts)
		def map = [:] //["$rootCpt":[:]]
		map["$rootCpt"] = [:]
		
		def l = getNarrower(m, map, rootCpt)
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
<br/>
<div id="menu">
    <ul id="nav">
"""
		printHtml(map,0,sb)	
			
		sb.append """
    </ul>
</div>

</td>
<td>
<form id="myForm" action="http://localhost:8082/tag.entry" method="get">

<table>
<tr>
<td>
<label for="conceptRoots">Choose a concept root</label>
<br/>
<select name="conceptRoots" id="conceptRoots" onchange="this.form.submit()">
  <option value="voc:visualArtTerm" ${rootCpt=="voc:visualArtTerm"?"selected":""}>voc:visualArtTerm</option>
  <option value="voc:watercolorMaterial" ${rootCpt=="voc:watercolorMaterial"?"selected":""}>voc:watercolorMaterial</option>
  <option value="voc:watercolorTechnique" ${rootCpt=="voc:watercolorTechnique"?"selected":""}>voc:watercolorTechnique</option>
  <option value="voc:watercolorTerm" ${rootCpt=="voc:watercolorTerm"?"selected":""}>voc:watercolorTerm</option>
  <option value="voc:watercolorTool" ${rootCpt=="voc:watercolorTool"?"selected":""}>voc:watercolorTool</option>
</select>
<br/>
<br/>
<textarea id="tags" name="tags" rows="9" cols="20"></textarea>
<input type="hidden" id="menuitem" name="menuitem" value=""/>
<input type="hidden" id="changeTags" name="gotoTags" value=""/>
<input type="hidden" id="changeVoc" name="chgVoc" value="" onchange="this.form.submit()"/>
<br/>
	<button onclick="changeTagGoto()">Tag</button>

</td>
<td>
<label for="workRoots">Choose a work root</label>
<br/>

<select name="workRoots" id="workRoots" onchange="this.form.submit()">
  <option value="vad:Watercolor">vad:Watercolor</option>
  <option value="vad:Drawing">vad:Drawing</option>
  <option value="vad:ComputerArt">vad:ComputerArt</option>
</select>

"""
		printData(data,sb)
		sb.append """
</td>
</tr>
</table>
</form>
<br/>
</td>
</tr>
</table>
<script>
function save(x) {
  document.getElementById("menuitem").value=x;
  //document.getElementById("demo").innerHTML = "Menu= " + document.getElementById("menuitem").value ;
  document.getElementById("tags").value =
		document.getElementById("tags").value
		+ " "
		+ document.getElementById("menuitem").value ;
}
function changeTagGoto() {
	var textarea = document.getElementById("tags");  
	document.getElementById("changeTags").value
		= textarea.value;
	document.getElementById("tags").value = ""
}
function changeVocSelect() {
this.form.submit()
		document.getElementById("changeVoc").value =document.getElementById("conceptRoots").value
}

</script>

version 0.2
</body>
</html>


"""
		""+sb
	}
	
	def getData(ttl) {
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
		int i=0
		data.each { 
			sb.append "<tr><td>"
			sb.append """<input type="checkbox" id="work${i}" name="work${i}" value="${it.s}">"""
			sb.append """<label for="work${i}">${it.label}</label><br>"""
			sb.append """${it.tag?it.tag.replaceAll("http://visualartsdna.org/voc#","voc:"):""}"""
			sb.append "</td></tr>"
			i++
		}
		sb.append """
</table>
"""
	}
	
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
			map[term]["$st"] = [:]
			//println "$st"
			getNarrower(mdl, map["$term"], st)
		}
	}
	
}
