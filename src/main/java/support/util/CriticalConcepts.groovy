package support.util

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import java.nio.file.Paths
import java.nio.file.Files

class CriticalConcepts {
	
	def ju = new JenaUtilities()
	def cptQ = "\\stage\\conceptQueue"
	def cptQueue = "/stage/conceptQueue"
	def tgt = "/stage/conceptQueue/processed"
	def data = "/stage/server/cwvaContent/ttl/data"
	def model = ju.loadFiles(data)
	def cptLimit = 10 // change as needed (5-10)
	
	@Test
	void testDerive() {
		println derive()
	}

	def derive() {
		process("Derived")
	}
	
	@Test
	void testTag() {
		println tag()
	}

	def tag() {
		process("Tagging")
	}
	
	def process(type) {
		def s = "$type concepts\n"
		def m = getIdMapFromDoc()
		m.each{id,map->
			s += "$id, ${map.file}, ${map.name}, ${map.topics}\n"
			def derFile = "$cptQ\\${map.file.replaceAll(/(\.md|\.txt)/,".ttl")}"
			def schemes = getSchemesForTopics(map.topics)
			switch (type) {
				case "Derived":
					callCA("${map.name}", "$cptQ\\${map.file}", derFile, id, "curate", "--derive-ttl", schemes)
					break
				
				case "Tagging":
					callCA("${map.name}", "$cptQ\\${map.file}", derFile, id,  "tag", "--tag-ttl", schemes)
				break
			}
			moveFile("$cptQueue/${map.file}","$tgt/${map.file}") // processed
		}
		s
	}
	
	def callCA(title, file, derived, uri, mode, ttlSpec, schemes) {
		String apiKey = System.getProperty("anthropKey")

		def cmd = """\\work\\python\\execCA.bat $apiKey "$title" "$file" $mode $uri $cptLimit "$ttlSpec" "$derived" "$schemes" """
		println cmd
		new util.Exec().execVerbose(cmd)
		println ""
	}

	
	
	def getSchemesForTopics(topics) {
		def lm = ju.queryListMap1(model, rdf.Prefixes.forQuery,"""
select ?tag {
	?s a the:Collection ;
		the:topic $topics ;
		the:tag ?tag ;
}
""")
		def s = ""
		int i=0
		lm.each{
			if (i++) s+= ","
			s += ju.getCuri(model,it.tag)
		}
		s
	}

	def moveFile(file,tgt) {
		def source = Paths.get(""+file)
		def target = Paths.get("$tgt")

		// Moves the file, replacing it if it already exists
		Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
	}

	@Test
	void testGetUri() {
		def m = getIdMapFromDoc()
		m.each{k,v->
			println "$k, $v"
		}
	}

	def getIdMapFromDoc() {
		def map = [:]
		new File("/stage/conceptQueue").eachFile{file->
			if (!(file.name.endsWith(".md"))
					&& !(file.name.endsWith(".txt"))) return
				def text = file.text
			def id = (text =~ /\[id=[\[]?(work\:[A-Za-z0-9\-]+)\]/)[0][1]

			// get instance attributes
			def lm = ju.queryListMap1(model, rdf.Prefixes.forQuery,"""
select ?topic ?name {
	$id a the:AI ;
		the:topic ?topic ;
		rdfs:label ?name ;
} order by ?topic
""")
			def topics = ""
			int i=0
			lm.findAll{
				it.topic
			}.each{
				if (i++) topics += ","
				topics += ju.getCuri(model,it.topic)
			}
			map[id] = [file:file.name, name:lm[0].name, topics:topics]
		}
		map
	}

}
