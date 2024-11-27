package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes
import org.apache.jena.query.*
import org.apache.jena.sparql.core.Prologue

class QueryMgr {
	def MAXRESULTSIZE = 1000000
	def prefixes = Prefixes.forQuery
	def dir
	def ju = new JenaUtilities()
	Model model = cwva.Server.getInstance().dbm.rdfs
	def qm
	
	QueryMgr(dir){
		this.dir = dir
		qm = loadQueries()
	}

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
		def format = m.format ?: "CSV"
		def q = m.query ?: ""
		def r = query(q,format) ?: ""
		def html = """
<html>
<head/>
<body>
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<br>

<br/>
<h2>SPARQL</h2>
<br/>
<form id="myForm" action="/sparql" method="get">
<table>
<tr><td>
<table>
<tr><td>
<table>
   <col width="400px" />
<tr><td>
  Format:
  <input type="radio" id="format" name="format" value="CSV" ${format=="CSV" ? "checked" : ""}>
  <label for="type1">CSV</label>
  <input type="radio" id="format" name="format" value="Text" ${format=="Text" ? "checked" : ""}>
  <label for="type2">Text</label>
  <input type="radio" id="format" name="format" value="TSV" ${format=="TSV" ? "checked" : ""}>
  <label for="type2">TSV</label>
  <input type="radio" id="format" name="format" value="JSON" ${format=="JSON" ? "checked" : ""}>
  <label for="type2">JSON</label>
  <input type="radio" id="format" name="format" value="XML" ${format=="XML" ? "checked" : ""}>
  <label for="type2">XML</label>
</td><td>
<input type = "submit" name = "submit" value = "Run" />
</td></tr>
</table>
</td></tr>
<tr><td>
<textarea id="query" name="query" rows="10" cols="60" spellcheck="false">
$q
</textarea>
</td></tr>
<tr><td>
<textarea rows="20" cols="60" spellcheck="false">
$r
</textarea>
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
Format applies only to Select statements.<br>
Construct and Describe statements result in Turtle (ttl) RDF.<br>
<a href="https://www.w3.org/TR/sparql11-query/">SPARQL 1.1 Query Language</a><br>

</td></tr>
</table>
</body>
</html>
"""
		html 
	}
	
	def query(sparql,format) {
		def result = ""
		
		if (sparql.trim())
		if (isUpdate(sparql)) {
			try {
				ju.queryExecUpdate(model,prefixes,sparql)
				result = "completed"
			} catch (Exception ex) {
				result="""${numLines(prefixes + sparql)}
---
$ex"""
			}
		}
		else {
			def ctms = System.currentTimeMillis()
			try {
				def mdl = ju.newModel()
				Query query = QueryFactory.create(prefixes + sparql) ;
				switch (query.queryType) {
					
					case Query.QueryTypeSelect:
						ResultSet resultSet = ju.queryResultSet(model,prefixes,sparql)
						
						ByteArrayOutputStream baos = new ByteArrayOutputStream()
						switch (format) {
							case "Text":
							ResultSetFormatter.out(baos, resultSet, new Prologue(model)) // same as text
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
						}
						result = "" + baos
						
//						result = ResultSetFormatter.asText(resultSet, new Prologue(model)) // ok
//						result = ResultSetFormatter.asText(resultSet) // same as text
						
//						ByteArrayOutputStream baos = new ByteArrayOutputStream()
//						ResultSetFormatter.outputAsSSE(baos,resultSet) // not implemented
//						ResultSetFormatter.outputAsXML(baos,resultSet) // ok
//						ResultSetFormatter.outputAsTSV(baos,resultSet) // ok
//						ResultSetFormatter.outputAsCSV(baos,resultSet) // nice format! real CR NL
//						result = "" + baos
						
//						result = ResultSetFormatter.out(baos, resultSet) // same as text
						
//						ResultSetFormatter.outputAsJSON(baos,resultSet) // ok
						result = "" + baos
						
						break;

					case Query.QueryTypeDescribe:
						mdl=ju.queryDescribe(model,prefixes,sparql)
						result = ju.saveModelString(mdl,"ttl")
						break;

					case Query.QueryTypeConstruct:
						mdl=ju.queryExecConstruct(model,prefixes,sparql)
						result = ju.saveModelString(mdl,"ttl")
						break;
				}
			} catch (Exception ex) {
				result="""${numLines(prefixes + sparql)}
---
$ex"""
			}
			//println "TIME ${System.currentTimeMillis() - ctms} ms"
		}
		if (result.size() > MAXRESULTSIZE)
			result = "Result set (${result.size()}) > max size ($MAXRESULTSIZE)"
//		else
//			println "RESULT SIZE ${result.size()}"
		result
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
		def ql = new File("$dir/res/queries.txt").text
		int i=0
		def s=""
		ql.eachLine{
			if (it.startsWith("#")) return
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

		
}
