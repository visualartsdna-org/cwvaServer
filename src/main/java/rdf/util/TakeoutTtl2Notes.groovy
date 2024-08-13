package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.Model

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
	def tgt = "C:/stage/tags"

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
		
		
		// json to anon ttl
		def t2t = new Takeout2Ttl()
		def base = "$tkoTmp/Takeout/Keep"
		t2t.setup(base,jsonDir,ttlDir)
		t2t.process(base,jsonDir)
		
		def src = "$base/$jsonDir"
		def ttlTmp = "$base/$ttlDir"
		def type = "vad:NotaBene" 	// Google Keep notes
		def prefix = "tko"			// via Takeout
		t2t.process(src,ttlTmp,type,prefix)

		
		// anon ttl to notes ttl
		process(ttlTmp,tgt)
	}

	def process(src,dest) {

		def ju = new JenaUtils()
		def model = ju.newModel()
		new File(src).eachFile {file->
			
			if (!(file.name.endsWith(".ttl"))) return
			println "$file"
			def m = ju.loadFileModelFilespec("$file")
			
			println "${m.size()}"
			
			def c = ju.queryListMap2(m,prefixes, """
select ?id ?created ?edited ?text ?title {
			?s schema:identifier ?id .
			?s tko:createdTimestampUsec ?created .
			?s tko:userEditedTimestampUsec ?edited .
			?s tko:filteredText ?text .
			?s tko:title ?title 
			
}
""")
			if (c.id==null) return
			//println c
			try {
				def ts = new Date(((c.created as long) / 1000) as long)
				def time = new SimpleDateFormat("yyyyMMdd").format(ts)
				def cpt = "${util.Text.camelCase(c.title)}_$time"
				def created = makeDate(c.created)
				def edited = makeDateTime(c.edited)
				def ttl= """
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix schema: <https://schema.org/> .
@prefix work:  <http://visualartsdna.org/work/> .
@prefix tko:   <http://visualartsdna.org/takeout#> .
@prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .
@prefix the:   <http://visualartsdna.org/thesaurus/> .
@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
			the:$cpt
				a tko:KeepNote ;
				tko:created "$created"^^xsd:date ;
				tko:edited "$edited"^^xsd:dateTime ;
				rdfs:label "${c.title}" ;
				skos:inScheme      the:paintingTerms ;
				skos:definition \"\"\"${(c.text).trim()}\"\"\" ;
				the:id work:${c.id} ;
				.
			work:${c.id} the:tag the:$cpt .
"""
				println ttl
				def m2 = ju.saveStringModel(ttl, "TTL")
				model.add m2
			} catch (Exception ex) {
				println "ERROR in data selection: $file, $ex"
			}

		}
		ju.saveModelFile(model, "$dest/notes.ttl", "TTL")
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
	void test() {
		def base = "C:/temp/generatedFiles/Takeout/Keep"
		def dest = "C:/temp/git/cwvaContent/ttl/tags"
		
		def src = "$base/$ttlDir"
		process(src,dest)
	}


}
