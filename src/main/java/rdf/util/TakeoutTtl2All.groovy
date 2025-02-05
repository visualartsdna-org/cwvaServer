package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.Model
import java.nio.file.StandardCopyOption
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import util.Rson
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile
import groovy.io.FileType

// rule for matching on annotated properties
// ns must be one of skos:|the:|vad:
//
class TakeoutTtl2All {

	def account = "rickspatesart"
	// takeout zip extract to generic folder
	def tkoTmp = "C:/temp/Takeout/export"
	def downloads = "C:/Users/ricks/Downloads"
	def tgt = "C:/work/author/ttl/$account"
	def notesTgt = "C:/stage/metadata"
	
	def jsonDir = "json"
	def ttlDir = "ttl"
	
	TakeoutTtl2All(){
	}
	
	@Test
	void test() {
		driver("rickspatesart")
	}
	
	def driver(acct) {
		account = acct
		tgt = "C:/work/author/ttl/$account"
		def stat = ""
		FileUtils.deleteDirectory(new File("$tkoTmp/Takeout"))
		stat += "directory deleted $tkoTmp/Takeout\n"
		
		def file = getLatestFileType(downloads,".zip")
		if (!file) {
			println "no zip file"
			return "no zip file"
		}
		unzipFile(file,tkoTmp)
		stat += "$tkoTmp unzipped\n"
		
		def s = checkAcct()
		if (s) return s
		
		processJson("$tkoTmp/Takeout/Keep",tgt)
		stat += "$tkoTmp/Takeout/Keep processed to $tgt\n"
		stat
	}

	def checkAcct() {
		def s = new File("$tkoTmp/Takeout/archive_browser.html").text
		def a = (s =~ /Archive for ([A-Za-z\.]+)@gmail.com/)[0][1]
		if (a != account) 
			return "ERROR--Expected $account data but found $a account in takeout export"
		null
	}

	def makeDate(ts) {
		def ts2 = new Date(((ts as long) / 1000) as long)
		new SimpleDateFormat("yyyy-MM-dd").format(ts2)
	}
	def makeDateTime(ts) {
		def ts2 = new Date(((ts as long) / 1000) as long)
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(ts2)
	}

	def getLatestFileType(folder, ext) {
		def dir = new File(folder)
		def list = []
		dir.eachFileRecurse (FileType.FILES) { file ->
			if ((""+file).endsWith(ext))
				list << file
		}
		list.sort{a,b->
			b.lastModified() <=> a.lastModified()
		}
		list.first()
	}

	def unzipFile(File file,tgt) {
		//cleanupFolder()
		def zipFile = new ZipFile(file)
		zipFile.entries().each { it ->
			def path = Paths.get("$tgt/${it.name}")
			if(it.directory){
				Files.createDirectories(path)
			}
			else {
				def parentDir = path.getParent()
				if (!Files.exists(parentDir)) {
					Files.createDirectories(parentDir)
				}
				Files.copy(zipFile.getInputStream(it), path)
			}
		}
	}

	@Test
	void test0() {
		def base = "C:/temp/generatedFiles/Takeout/Keep"
		def dest = "C:/temp/git/cwvaContent/ttl/tags"

		def src = "$base/$ttlDir"
		process(src,dest)
	}

	@Test
	void test1() {
		def base = "C:/temp/generatedFiles/Takeout/Keep"
		def dest = "C:/temp/git/cwvaContent/ttl/tags"

		def src = "$base/$jsonDir"
		def m = processJson(src,dest)
		println m
	}

	def processJson(src,dest) {
		def ju = new JenaUtils()
		def model = ju.newModel()
		def schemes=[:]
		new File(src).eachFile {file->
			def m2 = [:]
			if (!file.name.endsWith(".json")) return
				println file

//			if (file.name.contains("Collection"))
//				println "here"
			def m = Rson.load("$file")
			def col = getCol(m)

			// check for publish label
			def tag = col.labels.find{ it == "tag" }
			def topic = col.labels.find{ it == "topic" }
			if (!col.isArchived 
				&& (tag || topic)) {
				col.each{k,v->
						if (k in
								[
									"userEditedTimestampUsec",
									"createdTimestampUsec",
									"annotations",
									"attachments"
								])
							m2["${k}"]=v
						else if (k in
								[
									"title",
									"filteredText"
								])
							m2["${k}"]=v.trim()
						else if (k in ["tag"]) 
							m2["the:${k}"]=v
						else if (k==~ /(skos:|the:|vad:).*/)  // namespaces
							m2["${k}"]=v
						else if (k in [
									"inScheme",
									"hasTopConcept",
									"topConceptOf",
									"altLabel",
									"hiddenLabel",
									"prefLabel",
									"notation",
									"changeNote",
									"definition",
									"editorialNote",
									"example",
									"historyNote",
									"note",
									"scopeNote",
									"broader",
									"broaderTransitive",
									"narrower",
									"narrowerTransitive",
									"related",
									"semanticRelation",
									"Collection",
									"OrderedCollection",
									"member",
									"memberList",
									"broadMatch",
									"closeMatch",
									"exactMatch",
									"mappingRelation",
									"narrowMatch",
									"relatedMatch"
								])
							m2["skos:${k}"]=v
				}
				if (topic && !m2["skos:inScheme"]) {
					println "ERROR-no inScheme for the concept ${m2.title}"
					return
				} 

				if (tag && !m2["the:tag"]) {
					println "ERROR-no tag for the concept ${m2.title}"
					return
				}

				try {
					def ts = new Date(((m2.createdTimestampUsec as long) / 1000) as long)
					def time = new SimpleDateFormat("yyyyMMdd").format(ts)
					def cpt = "${util.Text.camelCase(m2.title)}_$time"
					def created = makeDate(m2.createdTimestampUsec)
					def edited = makeDateTime(m2.userEditedTimestampUsec)
					if (topic && m2["skos:inScheme"] && m2["skos:topConceptOf"])
						schemes[m2["skos:inScheme"]] = "the:$cpt"
					def ttl= """
${rdf.Prefixes.forFile}
			the:$cpt
				a skos:Concept ;
				skos:inScheme  ${topic ? m2["skos:inScheme"] : "the:paintingNotes"} ; 
				tko:created "$created"^^xsd:date ;
				tko:edited "$edited"^^xsd:dateTime ;
				skos:prefLabel "${m2.title}${tag ? " Notes" : ""}" ;
				skos:definition \"\"\"${(m2.filteredText)}\"\"\" ;

"""
					m2.findAll{k,v->
						k==~ /(skos:|the:|vad:).*/
					}.each{k,v->
						//if (isUri(v))
						ttl += """
						$k $v ;
"""
					}

					// annotations--urls
					m2.annotations.each{
						ttl += """ tko:link [
						tko:url <${it.url}> ;
						tko:urlTitle "${it.title}" ;
"""
						if (it.description
							&& it.description.trim()!="") {
							ttl += """
						tko:urlDescription \"\"\"${it.description}\"\"\" ;
"""
						}
						ttl += "] ;"
					}

					m2.attachments.each{
						if (it.mimetype == "image/jpeg") {
							ttl += """schema:image <http://visualartsdna.org/images/${it.filePath}> ;
"""
						def destDir = topic ? dest : notesTgt
							// file copy from src to dest
						Files.copy(new File("$src/${it.filePath}").toPath(), 
							new File("$destDir/${it.filePath}").toPath(), StandardCopyOption.REPLACE_EXISTING)
						}
					}

					ttl += """ .
"""
					//${m2.tag} the:tag the:$cpt .
					

//					println ttl
					model.add ju.saveStringModel(ttl, "TTL")
				} catch (Exception ex) {
					println "ERROR in data selection: $file, $ex"
				}
				
			}
		}
		schemes.each{k,v->
			if (!v) println "ERROR-no topConcept for scheme ${k}"

			def ttl = """
${rdf.Prefixes.forFile}
$k
        a                     tko:KeepConceptScheme ;
        skos:hasTopConcept    ${v ? v : cpt} ;
        skos:prefLabel        "$k" ;
			.
"""
			model.add ju.saveStringModel(ttl, "TTL")
		}
		// extract the data inScheme the:paintingNotes
		// to seperate notes file and dir
		def m1 = ju.queryDescribe(model,rdf.Prefixes.forQuery,"""
			describe ?s {{
		?s skos:inScheme ?sch  
		filter (?sch != the:paintingNotes)
		} union {
			?s  a tko:KeepConceptScheme .
		}}
""")
		ju.saveModelFile(m1, "$dest/${account}.ttl", "TTL")
		
		// painting notes can only come from the one account
		if (account == "rickspatesart") {
			def m2 = ju.queryDescribe(model,rdf.Prefixes.forQuery,"""
				describe ?s {
			?s skos:inScheme the:paintingNotes
			}
	""")
			def dts = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
			ju.saveModelFile(m2, "$notesTgt/tags/notes${dts}.ttl", "TTL")
		}
		
	}
	
	def sanitize(s) {
		s.trim().replaceAll("’","'")
		
	}

	def getCol(m) {
		def col = [:]

		def text = ""
		m.textContent.eachLine{

			if (it.trim().startsWith("[")) {
				def ann = (it.trim() =~ /^\[(.*)\]/)[0][1]
				def fs = ann.split("=")
				if (fs.size() == 2)
					col[fs[0]] = fs[1]
			} else if (!it.startsWith("@")) {
				text += "$it\n"
			}
		}
		col.title = m.title
		col.userEditedTimestampUsec = m.userEditedTimestampUsec
		col.createdTimestampUsec = m.createdTimestampUsec
		col.isArchived = m.isArchived
		// text
		col.filteredText = sanitize(text)
		if (m.annotations) {
			col.annotations = m.annotations
		}
		if (m.attachments) {
			col.attachments = m.attachments
		}

		// labels
		col.labels = []
		m.labels.each{
			col.labels += it.name
		}
		col
	}

	@Test
	void testIsUri() {
		println "${isUri("the:WebAfterRain")}"
		println "${isUri("work:da79b4be-3442-4b6b-bdb4-107b2682c560")}"
		println "${isUri("http://visualartsdna.org/work/da79b4be-3442-4b6b-bdb4-107b2682c560")}"
		println "${isUri("hi there")}"
		println "${isUri("molly")}"
	}

	def isUri(s) {
		if (s.contains(" ")) return false
		if (!s.contains(":")) return false
		true
	}
}
