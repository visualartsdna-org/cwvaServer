package support

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import rdf.JenaUtils
import rdf.tools.SparqlConsole
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat

import static java.nio.file.StandardCopyOption.*

class LoadConceptQueue {


	def vocab = "/stage/server/cwvaContent/ttl/vocab"
	def src = "/stage/conceptQueue"
	def tgt = "/stage/metadata/vocab"
	def vm
	def lcm
	def ju = new JenaUtilities()
	
	LoadConceptQueue(vm, lcm){
		this.vm = vm
		this.lcm = lcm
	}
	
	
	def loadVocab() {
		vm = ju.loadFiles(vocab)
	}
	
	def getScheme(broader) {
		if (!vm) loadVocab()
		def lm = ju.queryListMap1(vm,rdf.Prefixes.forQuery,"""
		select ?sch {
			bind($broader as ?bc)
			?bc skos:inScheme ?sch
		}
""")
			if (lm.size())
				return "<${lm[0].sch}>"
		return "<http://visualartsdna.org/thesaurus/visualArtTerms>"
	}
	
	def loadTtl(src,tgt) {
		def sb = new StringBuilder()
		def ttl = """
${rdf.Prefixes.forFile}

"""
		def dir = new File(src)
		
		// check for available files first
		def flist = []
		dir.eachFile{file->
			if (!file.name.endsWith(".ttl")) return
			flist += file.name
		}
		
		if (flist.isEmpty())
			return """No concept import TTL files in conceptQueue

"""
			
		dir.eachFile{file->
			//println file
			if (!file.name.endsWith(".ttl")) return
			sb.append """
File: $file
"""
			def m = ju.loadFiles("$file")
			def clm = ju.queryListMap1(m,rdf.Prefixes.forQuery,"""
	select ?l {
		{
		?s rdfs:label ?l
		} union {
		?s skos:prefLabel ?l
		}
	}
""")

			clm.each {concept->
				// check dups
				def lm = lcm.findMatchingConcepts(concept.l)
				sb.append lcm.formatDups(concept.l, lm)
			}
		println "${file.name} model size = ${m.size()}"
		
		// move from queue to distribute
		def srcPath = Paths.get("${file.path}")
		def tgtPath = Paths.get("$tgt/${file.name}")
		def chkPath = Paths.get("/stage/metadata/vocab/${file.name}")
		if (Files.exists(chkPath)) sb.append """Target File ${file.name} ALREADY EXISTS 
"""
		Files.move(srcPath, tgtPath, REPLACE_EXISTING)
		}

		""+sb
	}

	
	def load(src,tgt) {
		def sb = new StringBuilder()
		def ttl = """
${rdf.Prefixes.forFile}

"""
		def dir = new File(src)
		
		// check for available files first
		def flist = []
		dir.eachFile{file->
			if (!file.name.endsWith(".txt")) return
			flist += file.name
		}
		
		if (flist.isEmpty()) 
			return """No concept import Text files in conceptQueue

"""
			
		dir.eachFile{file->
			//println file
			if (!file.name.endsWith(".txt")) return
			sb.append """
File: $file
"""
			file.text.eachLine{
				if (!it) return
				if (it =~ /^[ \t#]+/) {
					return
				}
				def f = it.split("\t")

				def concept = f[0]
				def broader = f[1]
				def scheme = getScheme(broader)
				
				// check dups
				def lm = lcm.findMatchingConcepts(concept)
				sb.append lcm.formatDups(concept, lm)
				
				def defn = f[2]
				def optionalProps = [:]
				for (int i=3; i<f.size();i++) {
					def s = f[i]
					def matcher = (s =~ /\[(.*)\][ ]*(.*)/)[0]
					optionalProps[matcher[1]] = matcher[2]
				}
				//println "$concept, $defn\n"

				ttl +=  """ 
the:${util.Text.camelCase(concept)}
		a              skos:Concept ;
        rdfs:label         "$concept" ;
        skos:broader       $broader ;
        skos:definition    \"\"\"$defn\"\"\" ;
        skos:inScheme      $scheme ;
		dct:created		"${getDateTime()}^^xsd:dateTime" ;
"""
				optionalProps.each{k,v->
					def val = v
					if (isUri(v)) val = "<$v>"
					else if (!isCuri(v)) val = "\"\"\"$v\"\"\""
					
					ttl += """
					$k $val ;
"""
				}
ttl += """
.
	"""
			}
			def srcPath = Paths.get("${file.path}")
			def tgtPath = Paths.get("$src/old/${file.name}")
			Files.move(srcPath, tgtPath, REPLACE_EXISTING)
			
		}
		def m = ju.saveStringModel(ttl,"ttl")
		println "model size = ${m.size()}"
		ju.saveModelFile(m,"$tgt/concepts_${getDateStamp()}.ttl","ttl")
		""+sb
	}
	
	def isCuri(s) {
		s =~ /^[a-z0-9]+\:[A-Za-z0-9_-]+$/
	}

	def isUri(s) {
		s =~ /^(http|https):\/\/[a-zA-Z0-9-\.]+\.[a-zA-Z]{2,}(\/\S*)?$/
	}


	def getDateStamp() {
		new SimpleDateFormat("yyyyMMdd").format(new Date())
	}
	
	def getTimeStamp() {
		new SimpleDateFormat("yyyyMMddHHmm").format(new Date())
	}
	
	def getDateTime() {
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
	}
	
	@Test
	void test() {
	}
	
}