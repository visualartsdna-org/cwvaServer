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

class TakeoutTtl2Notes {
	def prefixes = """
prefix schema: <https://schema.org/> 
prefix dct:   <http://purl.org/dc/terms/> 
prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix work:  <http://visualartsdna.org/work/> 
prefix z0:    <http://visualartsdna.org/system/> 
prefix vad:   <http://visualartsdna.org/2021/07/16/model#> 
prefix skos:  <http://www.w3.org/2004/02/skos/core#> 
prefix tko:   <http://visualartsdna.org/takeout#> 
prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> 
prefix xs:    <http://www.w3.org/2001/XMLSchema#> 
prefix foaf:  <http://xmlns.com/foaf/0.1/> 
"""
	def tkoTmp = "C:/temp/Takeout/rickspatesart"
	def downloads = "C:/Users/ricks/Downloads"
	def tgt = "C:/stage/metadata"

	def jsonDir = "json"
	def ttlDir = "ttl"

	@Test
	void testDriver() {

		FileUtils.deleteDirectory(new File("$tkoTmp/Takeout"))

		def file = getLatestFileType(downloads,".zip")
		if (!file) {
			println "no zip file"
			return
		}
		unzipFile(file,tkoTmp)

		processJson("$tkoTmp/Takeout/Keep",tgt)
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
	void test() {
		def base = "C:/temp/generatedFiles/Takeout/Keep"
		def dest = "C:/temp/git/cwvaContent/ttl/tags"

		def src = "$base/$jsonDir"
		def m = processJson(src,dest)
		println m
	}

	def processJson(src,dest) {
		def ju = new JenaUtils()
		def model = ju.newModel()
		new File(src).eachFile {file->
			def m2 = [:]
			if (!file.name.endsWith(".json")) return
				println file

//			if (file.name.contains("Lily"))
//				println "here"
			def m = Rson.load("$file")
			def col = getCol(m)

			// check for publish label
			def publish = col.tags.find{
				it == "publish"
			}
			if (publish) {
				col.each{k,v->
					if (k == "tags") return
						if (k in
								[
									"title",
									"userEditedTimestampUsec",
									"createdTimestampUsec",
									"filteredText",
									"tag",
									"annotations",
									"attachments"
								])
							m2["${k}"]=v
						else if (k.contains(":"))  // a namespace
							m2["${k}"]=v
						else if (k in [
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
				if (!m2.tag) {
					println "ERROR-no tag to the work"
					return
				}

				try {
					def ts = new Date(((m2.createdTimestampUsec as long) / 1000) as long)
					def time = new SimpleDateFormat("yyyyMMdd").format(ts)
					def cpt = "${util.Text.camelCase(m2.title)}_$time"
					def created = makeDate(m2.createdTimestampUsec)
					def edited = makeDateTime(m2.userEditedTimestampUsec)
					def ttl= """
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix schema: <https://schema.org/> .
@prefix work:  <http://visualartsdna.org/work/> .
@prefix tko:   <http://visualartsdna.org/takeout/> .
@prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .
@prefix the:   <http://visualartsdna.org/thesaurus/> .
@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
			the:$cpt
				a the:KeepNote ;
				skos:inScheme      the:paintingNotes ;
				tko:created "$created"^^xsd:date ;
				tko:edited "$edited"^^xsd:dateTime ;
				rdfs:label "${m2.title}" ;
				the:tag ${m2.tag} ;
				skos:definition \"\"\"${(m2.filteredText).trim()}\"\"\" ;

"""
					m2.findAll{k,v->
						k.startsWith("skos:")// && k != "skos:related"
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
							// file copy from src to dest
						Files.copy(new File("$src/${it.filePath}").toPath(), 
							new File("$dest/${it.filePath}").toPath(), StandardCopyOption.REPLACE_EXISTING)
						}
					}

					ttl += """ .
"""
					//${m2.tag} the:tag the:$cpt .
					

					//println ttl
					model.add ju.saveStringModel(ttl, "TTL")
				} catch (Exception ex) {
					println "ERROR in data selection: $file, $ex"
				}
			}
		}
		ju.saveModelFile(model, "$dest/tags/notes.ttl", "TTL")
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
		// text
		col.filteredText = text
		if (m.annotations) {
			col.annotations = m.annotations
		}
		if (m.attachments) {
			col.attachments = m.attachments
		}

		// labels
		col.tags = []
		m.labels.each{
			col.tags += it.name
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
