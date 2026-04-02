package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes
import util.Guid

class EntityEntry {
	
	static def dir="/stage/metadata/tags"
	def prefixes = Prefixes.forQuery
	def model
	def ju = new JenaUtilities()
	
	EntityEntry(){
		model = cwva.Server.getInstance().dbm.rdfs
	}
	
	def validate(m) {
			assert m.type				, "no m.type"
			assert m.label				, "no m.label"
			assert m.media				, "no m.media"
			assert m.definition			, "no m.definition"
			assert m.recordedDateTime	, "no m.recordedDateTime"
			
	}
	
	def handleQueryParams(m) {
		if (m.isEmpty()) {
			m.guid = new Guid().get()
			def date = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
			m.recordedDateTime = date
			printHtml m
		}
		else {
			
			
		// collect media
			//m.media
			int i=0
			for (int j=1;j<=30;j++) { // limit leaves room for growth
				def k = "media$j"
				if (m[k]) {
					if (!m.media) m.media = ""
					if (i++) m.media += ","
					m.media += "${m[k]}"
				}
			}
			
		// collect notes
			def notesList = []
			for (int j=1;j<=20;j++) {
				def noteKey = "note$j"
				def noteTypeKey = "noteType$j"
				if (m[noteKey]) {
					notesList << [type: m[noteTypeKey] ?: "note", text: m[noteKey]]
				}
			}
			
			validate(m)
		//  support format <http://visualartsdna.org/images/NearElSoplador.jpg>
			def il = m.image ? m.image.split(",") : []
			def is = ""
			def j=0
			il.each{
				if (j++>0) is += ","
				is += "<http://visualartsdna.org/images/${it.trim()}>"
			}
			
		// build notes TTL
			def notesTtl = ""
			notesList.each { note ->
				notesTtl += "\t\tskos:${note.type}\t\"\"\"${note.text}\"\"\" ;\n"
			}
			
		def ttl = """
${rdf.Prefixes.forFile}
work:${m.guid}
		a the:${m.type} ;
		rdfs:label "${m.label}" ;
		the:topic	${m.media} ;
		skos:definition  \"\"\"${m.definition}\"\"\" ;
${m.document?"":"#"}		the:document <${m.document}> ;
${m.primarySite?"":"#"}		schema:sameAs <${m.primarySite}> ;
${m.wikipedia?"":"#"}		the:wikipedia	<${m.wikipedia}> ;
${m.dbpedia?"":"#"}		the:dbpedia	<${m.dbpedia}> ;
${m.image?"":"#"}		schema:image	$is ;
${m.tag?"":"#"}		the:tag	${m.tag} ;
${notesTtl}		schema:datePublished "${m.recordedDateTime}"^^xs:date ;
		skos:inScheme	the:entities ;
		.
"""
		new File("${m.dir}/${m.guid}.ttl").text = ttl
		ttl
		}
	}
		
	def printHtml(m) {
		
		def lm = ju.queryListMap1(model,
			rdf.Prefixes.forQuery,"""
select ?s ?l {
?s a skos:Concept ;
	rdfs:label ?l ;
	skos:inScheme the:TopicScheme ;
.
} order by ?l
""")
				
		 def html = """
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body style="margin:100;padding:0">
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<h2>
Art Entity work:${m.guid}</h2>
<form id="myForm" action="/entity_page.entry" method="get">
 <input type="hidden" id="guid" name="guid" value="${m.guid}">
<table><tbody><tr><td>
  <label for="label">Label:</label>
  <input type="text" id="label" name="label" size="40" value=""> 
</td></tr><tr><td><br>
Type:
</td></tr><tr><td>
  <input type="radio" id="type2" name="type" value="AI">
  <label for="type2">AI</label>
  <input type="radio" id="type1" name="type" value="Article">
  <label for="type1">Article</label>
  <input type="radio" id="type1" name="type" value="Artist">
  <label for="type1">Artist</label>
  <input type="radio" id="type2" name="type" value="Collection">
  <label for="type2">Collection</label>
  <input type="radio" id="type2" name="type" value="Concept">
  <label for="type2">Concept</label>
  <input type="radio" id="type2" name="type" value="Entity">
  <label for="type2">Entity</label>
  <input type="radio" id="type2" name="type" value="Image">
  <label for="type2">Image</label>
</td></tr><tr><td>
  <input type="radio" id="type2" name="type" value="Operation">
  <label for="type2">Operation</label>
  <input type="radio" id="type2" name="type" value="Organization">
  <label for="type2">Organization</label>
  <input type="radio" id="type2" name="type" value="Posting">
  <label for="type2">Posting</label>
  <input type="radio" id="type2" name="type" value="Series">
  <label for="type2">Series</label>
  <input type="radio" id="type2" name="type" value="Study">
  <label for="type2">Study</label>
  <input type="radio" id="type2" name="type" value="Work">
  <label for="type2">Work</label>
</td></tr><tr><td><br>
 Topics:
<table><tr>
"""
		int i=0
		lm.each{
			html += """
				${i!=0 && !(i%5) ? "</tr><tr>":""}
				<td>
			<input type="checkbox" id="media$i" name="media$i" value=${ju.getCuri(model,it.s)}>
			<label for="media$i">${it.l}</label>
			</td>

"""
		  i++
		}
		html += "</tr></table>"

		html += """
</td></tr><tr><td><br>
         Definition : <br>
         <textarea rows="5" cols="70" id="definition" name="definition"></textarea>
</td></tr><tr><td>
<table cellspacing="1" cellpadding="5"><tr><td align="right">
<!-- tags -->
  <label for="tag">Tags:</label><br>prefix URIs<br>w/commas
</td><td>
 	<textarea rows="3" cols="60" id="tag" name="tag"></textarea>
<!-- notes container -->
</td></tr><tr><td colspan="2">
  <div id="notesContainer">
    <div class="note-row">
      <select name="noteType1" size="1">
        <option>note</option>
        <option>changeNote</option>
        <option>editorialNote</option>
        <option>example</option>
        <option>historyNote</option>
        <option>scopeNote</option>
      </select>
      <textarea rows="3" cols="60" name="note1"></textarea>
    </div>
  </div>
  <button type="button" onclick="addNote()">+ Add Note</button>
</td></tr><tr><td align="right">
</td></tr><tr><td align="right">
  <label for="document">Document:<br>single</label></td><td>
  <input type="text" id="document" name="document" size="60" value=""> 
</td></tr><tr><td align="right">
  <label for="primarySite">URL:<br>single</label></td><td>
  <input type="text" id="primarySite" name="primarySite" size="60" value=""> 
</td></tr><tr><td align="right">
  <label for="wikipedia">Wikipedia:<br>single</label></td><td>
  <input type="text" id="wikipedia" name="wikipedia" size="60" value=""> 
</td></tr><tr><td align="right">
  <label for="image">Image Files:</label><br>filenames<br>w/commas
</td><td>
  <!--<input type="text" id="image" name="image" size="60" value="">-->
 	<textarea rows="3" cols="60" id="image" name="image"></textarea>
</td></tr><tr><td align="right">
<!-- recordedDateTime -->
</td></tr><tr><td align="right">
  <label for="recordedDateTime">Recorded:</label></td><td>
  <input type="text" id="recordedDateTime" name="recordedDateTime" size="20" value="${m.recordedDateTime}">
</td></tr></table>
</td></tr></tbody></table>
<br>
	 <input type="hidden" name="pagename" value="10">
	 <input type="submit" name="submit" value="Submit"></td><td>
	<input type="button" onclick="myFunction()" value="Reset form">
<br><br>
  Configuration:<br>
  <br><label for="dir">TTL &amp; QRC temp dir:</label>
  <input type="text" id="dir" name="dir" size="40" value="/stage/data"> 
<!--
<table><tr><td>
	 <input type = "hidden" name = "pagename" value = "10" />
	 <input type = "submit" name = "submit" value = "Submit" />
	<input type="button" onclick="myFunction()" value="Reset form">
</td><td style="text-align:right">Configuration parameters</td><td>
  <br><label for="dir">TTL & QRC temp dir:</label><br>
  <input type="text" id="dir" name="dir" size="40" value="/stage/data"> 
</td></tr></table>-->
</form>

<script>
var noteCount = 1;

function addNote() {
  noteCount++;
  var container = document.getElementById('notesContainer');
  var div = document.createElement('div');
  div.className = 'note-row';
  div.innerHTML = '<select name="noteType' + noteCount + '" size="1">' +
    '<option>note</option>' +
    '<option>changeNote</option>' +
    '<option>editorialNote</option>' +
    '<option>example</option>' +
    '<option>historyNote</option>' +
    '<option>scopeNote</option>' +
    '</select> ' +
    '<textarea rows="3" cols="60" name="note' + noteCount + '"></textarea>' +
    ' <button type="button" onclick="this.parentNode.remove()">x</button>';
  container.appendChild(div);
}

function myFunction() {
  document.getElementById("myForm").reset();
  var container = document.getElementById('notesContainer');
  while (container.children.length > 1) {
    container.removeChild(container.lastChild);
  }
  noteCount = 1;
}
</script>

</body></html>
"""
		html
	}
	
	
}
