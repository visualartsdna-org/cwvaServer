package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import rdf.util.BackupFiles
import rdf.util.Transaction
import org.apache.jena.rdf.model.InfModel
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory

	// TODO: <dt> tag, https://www.w3schools.com/tags/tag_dt.asp
	// for description fields
	// TODO: put checkbox label in href link to work:* in new tab
	// consider support for localhost vs visualartsdna.org
	
class TagModel {

	def ju = new JenaUtilities()
	def tagsFile
	def host
	Model instanceModel
	Model conceptModel
	Model tagsModel = ju.newModel()
	Transaction tx
	
	TagModel(instances,concepts,tagsFile,host){
		this.tagsFile = tagsFile
		conceptModel = ju.loadFiles(concepts)
		if (new File(tagsFile).exists()) {
			tagsModel = ju.loadFiles(tagsFile)
		}
		tx = new Transaction(tagsModel,tagsFile)
		def data = ju.loadFiles(instances);
		this.instanceModel = ModelFactory.createRDFSModel(data, tagsModel);
		this.host = host
	}
	
	def handleQueryParams(m) {
		def html = ""
		handleTags(m)
		process(m.conceptRoots,m.workRoots)
	}

	def handleTags(m) {

		if (m.containsKey("gotoTags")) {
			def wl = []
			def tl = []
			m.findAll { k,v->
				k =~ /^work[0-9].*/
			}.each{k,v->
				wl += v
			}
			m.findAll { k,v->
				k == "gotoTags"
			}.each{k,v->
				tl.addAll( v.trim().split(/[ ]+/))
			}

			def s = """
@prefix vad: <http://visualartsdna.org/2021/07/16/model#> .
@prefix work:	<http://visualartsdna.org/work/> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix the:	<http://visualartsdna.org/thesaurus/> .
"""
			wl.each{w->
				tl.each{t->
					s += "<$w> vad:tag $t .\n"

				}
			}

			def model = ju.saveStringModel(s,"TTL")
			tagsModel.add(model)
			tx.save()
		}
		else if (m.containsKey("removeTags")) {
			
			def wl = []
			m.findAll { k,v->
				k =~ /^work[0-9].*/
			}.each{k,v->
				wl += v
			}
			wl.each{w->
			ju.queryExecUpdate(instanceModel,"""
prefix vad: <http://visualartsdna.org/2021/07/16/model#> 
prefix work:	<http://visualartsdna.org/work/> 
prefix skos: <http://www.w3.org/2004/02/skos/core#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
prefix the:	<http://visualartsdna.org/thesaurus/> 
""","""
			delete {
			<$w> vad:tag ?o
			} where {
			<$w> vad:tag ?o
			}
"""
)
			}
			
		tx.save()
		}
	}

	def process(rootCpt,workType) {

		def data = getData(instanceModel,workType)
		def map = [:]
		map["$rootCpt"] = [:]

		def l = getNarrower(conceptModel, map, rootCpt)
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
.button {
  background-color: #f0f0f0; /* Gray */
  border:  1px solid #e4e4e4;
  color: black;
  padding: 5px 2px;
  text-align: center;
  text-decoration: none;
  display: inline-block;
  font-size: 12px;
	border-radius: 3px;
		cursor: pointer;
}
img {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 5px;
  width: 50px;
}
</style>
    </head>
	<body>

<h3>VisualArtsDNA Tagging</h3>

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
<br/>
<label for="conceptRoots">Choose a concept root</label>
<br/>
<select name="conceptRoots" id="conceptRoots" onchange="this.form.submit()">
  <option value="the:visualArtTerm" ${rootCpt=="the:visualArtTerm"?"selected":""}>the:visualArtTerm</option>
  <option value="the:digitalArtTerm" ${rootCpt=="the:digitalArtTerm"?"selected":""}>the:digitalArtTerm</option>
  <option value="the:watercolorMaterial" ${rootCpt=="the:watercolorMaterial"?"selected":""}>the:watercolorMaterial</option>
  <option value="the:watercolorTechnique" ${rootCpt=="the:watercolorTechnique"?"selected":""}>the:watercolorTechnique</option>
  <option value="the:watercolorTerm" ${rootCpt=="the:watercolorTerm"?"selected":""}>the:watercolorTerm</option>
  <option value="the:watercolorTool" ${rootCpt=="the:watercolorTool"?"selected":""}>the:watercolorTool</option>
</select>
<br/>
<br/>
<br/>
<label for="workRoots">Choose a work root</label>
<br/>

<select name="workRoots" id="workRoots" onchange="this.form.submit()">
  <option value="vad:Watercolor" ${workType=="vad:Watercolor"?"selected":""}>vad:Watercolor</option>
  <option value="vad:Drawing" ${workType=="vad:Drawing"?"selected":""}>vad:Drawing</option>
  <option value="vad:LindenMayerSystemImage" ${workType=="vad:LindenMayerSystemImage"?"selected":""}>vad:LindenMayerSystemImage</option>
</select>
<br/>
<br/>
<br/>
<textarea id="tags" name="tags" rows="9" cols="20"></textarea>
<input type="hidden" id="menuitem" name="menuitem" value=""/>
<input type="hidden" id="changeTags" name="gotoTags" value=""/>
<input type="hidden" id="removeTags" name="removeTags" value=""/>
<input type="hidden" id="changeVoc" name="chgVoc" value="" onchange="this.form.submit()"/>
<br/>
	<button onclick="changeTagGoto()">Tag</button>
	<button onclick="removeTagGoto()">Detag</button>

</td>
<td>
<input type="checkbox" onclick="toggle(this);" /><i>Check/clear all</i><br />
<br/>
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
function removeTagGoto() {
	document.getElementById("removeTags").value
		= true;
}
function changeVocSelect() {
this.form.submit()
		document.getElementById("changeVoc").value =document.getElementById("conceptRoots").value
}
function toggle(source) {
    var checkboxes = document.querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i] != source)
            checkboxes[i].checked = source.checked;
    }
}
</script>

version 1.0
</body>
</html>


"""
		""+sb
	}

	def getData(mdl,type) {
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
prefix the:   <http://visualartsdna.org/thesaurus/>
""", """
			
			select ?s ?label ?tag ?image {
					?s a ${type} .
					optional {?s vad:tag ?tag .}
					?s rdfs:label ?label .
					?s schema:image ?image .
			} order by ?label
			""")
		def wl= [:]
		l.each{
			if (!wl[it.s]) wl[it.s] = [:]
			wl[it.s].label = it.label
			wl[it.s].image = it.image
			wl[it.s].tag = wl[it.s].tag ? wl[it.s].tag : ""
			wl[it.s].tag += it.tag?it.tag.replaceAll("http://visualartsdna.org/thesaurus/","the:") + " ":""

		}
		wl
	}

	def printData(data,sb) {
		sb.append """
<table>
"""
		int i=0
		data.each { k,v->
		def img = v.image.replaceAll("http://visualartsdna.org",host)
			sb.append "<tr><td>"
			sb.append """
<a target="_blank" href="${img}">
  <img src="${img}" style="width:50px">
</a>
"""
			sb.append """<input type="checkbox" id="work${i}" name="work${i}" value="${k}">"""
			sb.append """<label for="work${i}">${v.label}</label><br>"""
			sb.append """${v.tag?:""}"""
			sb.append "</td></tr>"
			i++
		}
		sb.append """
</table>
"""
	}

	def printHtml(map,level,sb) {

		map.each { k,v->
			def s = """<li><button class="button" onclick="save('$k')">$k</button></li>"""
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
prefix the:   <http://visualartsdna.org/thesaurus/>
""", """

select ?s {
		?s a skos:Concept .
		?s skos:broader $term
} order by ?s
""")
		l.each{
			def st = it.s.replaceAll("http://visualartsdna.org/thesaurus/","the:")
			map[term]["$st"] = [:]
			//println "$st"
			getNarrower(mdl, map["$term"], st)
		}
	}

}
