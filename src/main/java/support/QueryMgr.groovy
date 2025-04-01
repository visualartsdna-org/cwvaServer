package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes
import org.apache.jena.query.*
import org.apache.jena.sparql.core.Prologue
import rdf.parser.*

class QueryMgr {
	def MAXRESULTSIZE = 1000000
	def prefixes = Prefixes.forQuery
	def dir
	def ju = new JenaUtilities()
	def qm
	def updateEnabled = false
	
	QueryMgr(dir){
		this.dir = dir
		qm = loadQueries()
	}
	
	Model getModel() {
		cwva.Server.getInstance().dbm.rdfs
	}

	/*
	 * Load Clear Drop Add Move Copy Create Delete Modify Insert
	 */
	def isUpdate(qs) {
		qs =~ /(?i)delete[ \t\n]*\{|delete[ \t\n]+data[ \t\n]*\{|insert[ \t\n]*\{|insert[ \t\n]+data[ \t\n]*\{/
	}
	
	
	def handleQueryParams(m) {
		process(m)
	}

	def process() {
		process([])
	}
		
	def process(m) {
		//m.format = "HTML" // test
		def format = m.format ?: "HTML"
		def q = m.query ? m.query.trim() : ""
		println "$q" // clear query for log file
		def resultMap = query(q.trim(),format,m.isMobile=="true") ?: ""
		// for mobile format
		def htmlMobile = """
<html>
<head>
<head/>
<body>
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<br>
<!-header->
<style>
body {background-color: WhiteSmoke;}
h1   {color: blue;}
#queries{
 width:400px;   
}
</style>
<h3>SPARQL</h3>
<form id="myForm" action="/sparql" method="get">
<table>
<tr><td>
<table>
<tr><td>
<table>
   <col width="290px" />
<tr><td style="border:1px solid black;">
<!--  Format: -->
  <input type="radio" id="format" name="format" value="HTML" ${format=="HTML" ? "checked" : ""}>
  <label for="type1">html</label>
  <input type="radio" id="format" name="format" value="CSV" ${format=="CSV" ? "checked" : ""}>
  <label for="type1">csv</label>
<!--
  <input type="radio" id="format" name="format" value="Text" ${format=="Text" ? "checked" : ""}>
  <label for="type2">text</label>
-->
  <input type="radio" id="format" name="format" value="TSV" ${format=="TSV" ? "checked" : ""}>
  <label for="type2">tsv</label>
  <input type="radio" id="format" name="format" value="JSON" ${format=="JSON" ? "checked" : ""}>
  <label for="type2">json</label>
  <input type="radio" id="format" name="format" value="XML" ${format=="XML" ? "checked" : ""}>
  <label for="type2">xml</label>
</td></tr><tr><td>
  <label for="sparqlUpdate">Update</label>
<input type="checkbox" id="sparqlUpdate" name="sparqlUpdate" value="Update" disabled>
</td><td>
<input type = "submit" name = "submit" value = "Execute" />
</td></tr>
</table>
</td></tr>
<tr><td>
<textarea id="query" name="query" rows="10" cols="40" spellcheck="false">
$q
</textarea>
</td></tr>
<tr><td>
<h4>
Query Set
</h4>
<select name="queries" id="queries" size="${qm.size()}">
"""
					qm.each{k,v->
						htmlMobile += """
			<option value="$v">$v
			   </option>
"""
						   
					}
				htmlMobile +=
			"""
</select>

</td></tr>
<tr><td>
<!-- <h4>Results</h4> -->
${resultMap.result}
</td></tr>
<tr><td>
${resultMap.time} ms | ${resultMap.resultSetSize} | ${resultMap.status}
</td></tr>
</table>

</td></tr>
</table>
</form>
<script>
var mytextbox = document.getElementById('query');
var mydropdown = document.getElementById('queries');

mydropdown.onchange = function() {
  var mydropdownValue = mydropdown.options[mydropdown.selectedIndex].value;
  mytextbox.value =  mydropdownValue;
}
</script>
<hr>
<table>
<tr><td>
<h4>
Prefixes
</h4>
dct:  &lt;http://purl.org/dc/terms/&gt;  <br>
foaf:  &lt;http://xmlns.com/foaf/0.1/&gt;  <br>
owl:  &lt;http://www.w3.org/2002/07/owl#&gt; <br>
rdf:  &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;  <br>
rdfs:  &lt;http://www.w3.org/2000/01/rdf-schema#&gt;  <br>
schema:  &lt;https://schema.org/&gt;  <br>
skos:  &lt;http://www.w3.org/2004/02/skos/core#&gt;  <br>
the:  &lt;http://visualartsdna.org/thesaurus/&gt;  <br>
tko:  &lt;http://visualartsdna.org/takeout/&gt; <br>
vad:  &lt;http://visualartsdna.org/2021/07/16/model#&gt; <br>
work:  &lt;http://visualartsdna.org/work/&gt;  <br>
xs:  &lt;http://www.w3.org/2001/XMLSchema#&gt;  <br>
xsd:  &lt;http://www.w3.org/2001/XMLSchema#&gt;  <br>
</td><td>
</td></tr><tr><td>
<!--</td><td valign="top">-->
<br>
<h4>
Notes
</h4>
Format (csv, ...) applies only to Select statement results.<br>
Construct and Describe statements result in Turtle (ttl) RDF.<br>
Enable Update for SPARQL Update statement processing.<br>
<a href="https://www.w3.org/TR/sparql11-query/">SPARQL 1.1 Query Language</a><br>

</td></tr>
</table>
<script>
function myFunction() {
  document.getElementById("sparqlUpdate").disabled = true;
}
</script>
</body>
</html>
"""
			
		def html = """
<html>
<head>
<head/>
<body>
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<br>
<!-header->
<style>
body {background-color: WhiteSmoke;}
h1   {color: blue;}
#queries{
 width:400px;   
}
</style>
<h3>SPARQL</h3>
<form id="myForm" action="/sparql" method="get">
<table>
<tr><td>
<table>
<tr><td>
<table>
   <col width="290px" />
<tr><td style="border:1px solid black;">
<!--  Format: -->
  <input type="radio" id="format" name="format" value="HTML" ${format=="HTML" ? "checked" : ""}>
  <label for="type1">html</label>
  <input type="radio" id="format" name="format" value="CSV" ${format=="CSV" ? "checked" : ""}>
  <label for="type1">csv</label>
<!--
  <input type="radio" id="format" name="format" value="Text" ${format=="Text" ? "checked" : ""}>
  <label for="type2">text</label>
-->
  <input type="radio" id="format" name="format" value="TSV" ${format=="TSV" ? "checked" : ""}>
  <label for="type2">tsv</label>
  <input type="radio" id="format" name="format" value="JSON" ${format=="JSON" ? "checked" : ""}>
  <label for="type2">json</label>
  <input type="radio" id="format" name="format" value="XML" ${format=="XML" ? "checked" : ""}>
  <label for="type2">xml</label>
</td><td>
  <label for="sparqlUpdate">Update</label>
<input type="checkbox" id="sparqlUpdate" name="sparqlUpdate" value="Update" disabled>
</td><td>
<input type = "submit" name = "submit" value = "Execute" />
</td></tr>
</table>
</td></tr>
<tr><td>
<textarea id="query" name="query" rows="10" cols="60" spellcheck="false">
$q
</textarea>
</td></tr>
<tr><td>
${resultMap.result}
</td></tr>
<tr><td>
${resultMap.time} ms | ${resultMap.resultSetSize} | ${resultMap.status}
</td></tr>
</table>
</td><td>

<h4>
Query Set
</h4>
<select name="queries" id="queries" size="${qm.size()}">
"""
		qm.each{k,v->
			html += """
			<option value="$v">$v
			   </option>
"""
			   
		}
	html +=
"""
</select>
</td></tr>
</table>
</form>
<script>
var mytextbox = document.getElementById('query');
var mydropdown = document.getElementById('queries');

mydropdown.onchange = function() {
  var mydropdownValue = mydropdown.options[mydropdown.selectedIndex].value;
  mytextbox.value =  mydropdownValue;
}
</script>
<hr>
<table>
<tr><td>
<h4>
Prefixes
</h4>
dct:  &lt;http://purl.org/dc/terms/&gt;  <br>
foaf:  &lt;http://xmlns.com/foaf/0.1/&gt;  <br>
owl:  &lt;http://www.w3.org/2002/07/owl#&gt; <br>
rdf:  &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;  <br>
rdfs:  &lt;http://www.w3.org/2000/01/rdf-schema#&gt;  <br>
schema:  &lt;https://schema.org/&gt;  <br>
skos:  &lt;http://www.w3.org/2004/02/skos/core#&gt;  <br>
the:  &lt;http://visualartsdna.org/thesaurus/&gt;  <br>
tko:  &lt;http://visualartsdna.org/takeout/&gt; <br>
vad:  &lt;http://visualartsdna.org/2021/07/16/model#&gt; <br>
work:  &lt;http://visualartsdna.org/work/&gt;  <br>
xs:  &lt;http://www.w3.org/2001/XMLSchema#&gt;  <br>
xsd:  &lt;http://www.w3.org/2001/XMLSchema#&gt;  <br>
</td><td>
</td><td valign="top">
<h4>
Notes
</h4>
Format (csv, ...) applies only to Select statement results.<br>
Construct and Describe statements result in Turtle (ttl) RDF.<br>
Enable Update for SPARQL Update statement processing.<br>
<a href="https://www.w3.org/TR/sparql11-query/">SPARQL 1.1 Query Language</a><br>

</td></tr>
</table>
<script>
function myFunction() {
  document.getElementById("sparqlUpdate").disabled = true;
}
</script>
</body>
</html>
"""
		if (m.isMobile == "true")
			return htmlMobile
		else return html
	}
	
	def query(sparql,format,isMobile) {
		def result = [time:0,status:"ok",resultSetSize:0,result:""]
		
		if (sparql.trim())
		if (isUpdate(sparql)) {
			if (!updateEnabled) {
				result.status = "update disabled"
			} else 
			try {
				ju.queryExecUpdate(getModel(),prefixes,sparql)
				result.status = "completed"
			} catch (QueryParseException qpex) {
				result.status = "Parse exception encountered"
				result.result ="""${numLines(prefixes + sparql)}
---
${(""+qpex).substring((""+qpex).indexOf(":")+2)}"""
			} catch (Exception ex) {
				result.status = "Exception encountered"
				result.result = """${numLines(prefixes + sparql)}
---
$ex"""
			}
		}
		else {
			def ctms = System.currentTimeMillis()
			try {
				def mdl = ju.newModel()
				Query query = QueryFactory.create(prefixes + sparql) ;
				int rowcnt = 0
				
				switch (query.queryType) {
					case Query.QueryTypeSelect:
						ResultSet resultSet = ju.queryResultSet(getModel(),prefixes,sparql)
						ByteArrayOutputStream baos = new ByteArrayOutputStream()
						switch (format) {
							case "Text":
							ResultSetFormatter.out(baos, resultSet, new Prologue(getModel())) // same as text
							break
							
							case "CSV":
							ResultSetFormatter.outputAsCSV(baos,resultSet) // nice format! real CR NL
							break
							
							case "TSV":
							ResultSetFormatter.outputAsTSV(baos,resultSet) // ok
							break
							
							case "JSON":
							ResultSetFormatter.outputAsJSON(baos,resultSet) // ok
							break
							
							case "XML":
							ResultSetFormatter.outputAsXML(baos,resultSet) // ok
							break

							case "HTML":
							def m = ju.queryListMap1(getModel(),prefixes,sparql)
							rowcnt = m.size();
							htmlFormat(baos, m)
							break
						}

						def res = new String( baos.toByteArray())
						if (format != "HTML") {
							result.result = fmtTextArea(res,isMobile)
							result.resultSetSize = resultSet.getRowNumber()
						}
						else {
							result.result = res
							result.resultSetSize = rowcnt
						}
						break;

					case Query.QueryTypeDescribe:
						mdl=ju.queryDescribe(getModel(),prefixes,sparql)
						result.result = fmtTextArea(ju.saveModelString(mdl,"ttl"),isMobile)
						result.resultSetSize = mdl.size()
						break;

					case Query.QueryTypeConstruct:
						mdl=ju.queryExecConstruct(getModel(),prefixes,sparql)
						result.result = fmtTextArea(ju.saveModelString(mdl,"ttl"),isMobile)
						result.resultSetSize = mdl.size()
						break;
						
					default: 
						result.status="Command not supported"
				}
			} catch (QueryParseException qpex) {
				result.status = "Parse exception encountered"
				result.result = fmtTextArea("""${numLines(prefixes + sparql)}
---
${(""+qpex).substring((""+qpex).indexOf(":")+2)}""",isMobile)
			} catch (Exception ex) {
				result.status = "Exception encountered"
				result.result = fmtTextArea("""${numLines(prefixes + sparql)}
---
$ex""" ,isMobile)
			} catch (OutOfMemoryError me) {
				result.status="Out of Memory Error"
				println "Out of Memory Error: $me"
				println "$sparql\n$format"
			}
			result.time = System.currentTimeMillis() - ctms
			println "query: ${result.time} ms"
			result.size = result.size()
		}
		if (result.size() > MAXRESULTSIZE) {
			result.result = fmtTextArea("Result size (${result.size()}) > max size ($MAXRESULTSIZE)",isMobile)
			result.status = "result string size > max"
		}
		result
	}
	
	def fmtTextArea(s,isMobile) {
		"""<textarea readonly rows="20" cols="${isMobile?"40":"60"}" spellcheck="false">$s</textarea>"""
		
	}
	
	def numLines(s) {
		def rs = ""
		int i=1
		s.eachLine{
			rs += "${i++}\t$it\n"
		}
		rs
	}

	def loadQueries() {
		def qm = [:]
		def ql = new File("$dir/res/cached.sparql").text
		int i=0
		def s=""
		ql.eachLine{
			if (it.startsWith("##")) return
			if (it.trim() == "" && s != "") {
				qm["s${i++}"] = s
				s = ""
			}
			else {
				s += it + "\n"
			}
		}
		if (s != "")
			qm["s${i++}"] = s
		
		qm
	}

	// [{s=<http://visualartsdna.org/work/95eb37e3-5a82-4fa0-82f3-87001c9872d2>}, {s=<http
	def htmlFormat(baos, map) {
		int i=0
		def sb = new StringBuilder()
		sb.append """
<style>
.table-wrapper1
{
    border: 1px solid black;
    width: 500px;
    height: 500px;
    overflow: auto;
    resize: both;
	margin-top: 0px;
    margin-bottom: 0px;
    margin-right: 0px;
    margin-left: 0px;
}}
.data-table
{
background-color:#FFFFFF;
tr:nth-child(even) {background-color: #f8f8f8;}
}
</style>
<div class="table-wrapper1">
<table class="data-table">
"""
		map.each{
			sb.append "<tr>"
			
			if (!i++) { // column headers
				it.each{k,v->
					sb.append "<th>$k</th>"
				}
				sb.append "</tr><tr>"
			}
			it.each{k,v->
				if (v.startsWith("http://") || v.startsWith("https://")) {
					def pm = ju.getPrefix(getModel(),v)
					def url = pm[0] != "null:" ? "${pm[0]}${pm[1]}" : "$v"
					sb.append """<td>
<a href=${v.replaceAll("http://visualartsdna.org",cwva.Server.getInstance().cfg.twinHost)} target="_blank" rel="noopener noreferrer">$url</a>
</td>"""
				} else sb.append "<td>$v</td>"
			}
		sb.append "</tr>"
		}
		sb.append """
</table>
</div>"""
		baos.write(sb.toString().getBytes())
	}
	
	def extractUrl(s) {
		s.substring(1,s.length()-1)
	}
}
