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

class TestLoadConceptQueue {


	def vocab = "/stage/server/cwvaContent/ttl/vocab"
	def src = "/stage/conceptQueue"
	def tgt = "/stage/metadata/vocab"
	def vm
	def ju = new JenaUtilities()
	
	@Test
	void testGetHierarchy() {
		if (!vm) loadVocab()
		
		new SparqlConsole().show(vm  as Model)
		def lm = ju.queryListMap1(vm,rdf.Prefixes.forQuery,"""
		select * {
			?s a skos:Concept ;
				skos:broader ?b .
			?b	rdfs:label ?bl ;
				skos:conceptScheme ?cs .
			?cs rdfs:label ?csl .
		}
""")
		lm.each{
			it.each{k,v->
				print "$k=$v\t"
			}
			println ""
		}
		
	}
	
	@Test
	void testLookup() {
		def l=[
			"facture",
			"factur",
			"factures",
			"watercolor",
			"WATERCOLOR",
			"watercolor toolwork",
			
			]
		
		l.each{

			def ce = conceptExists(it)
			
			println "$it ${ce==true ? 'exists' : 'not exists'}" 
			
			if (ce instanceof List) {
				println "Warnings"
				ce.each{like->
					println "concept '$it' similar to existing '$like'"
				}
			}
	
			println ""
			
		}
	}
	
	def conceptExists(cpt) {
		
		def lm = lcm.findMatchingConcepts(cpt)
		println cpt
		println lm
		false
//		if (!vm) loadVocab()
//		def cee = conceptExistsExact(cpt)
//		if (cee) return true
//		conceptExistsRegex(cpt)
	}
	
	def conceptExistsExact(cpt) {
		def lm = ju.queryListMap1(vm,rdf.Prefixes.forQuery,"""
		select * {
			?s rdfs:label "$cpt"
		}
""")
		lm.size()
	}
	
	def conceptExistsRegex(cpt) {
		def lm = ju.queryListMap1(vm,rdf.Prefixes.forQuery,"""
		select * {
			?s rdfs:label ?o
			filter (regex(?o, "$cpt", "i") 
			|| regex("$cpt", ?o, "i") )
		}
""")
		def l = []
		lm.each{
			l += it.o
		}
		l
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
	
	def lcm
	@Test
	void test() {
		loadVocab()
		lcm = new TestLoadConceptMatch(vm)
		println lcm.formatHeader()
		load(src,tgt)
	}
	
	def load(src,tgt) {
		def ttl = """
${rdf.Prefixes.forFile}

"""
		new File(src).eachFile{file->
			//println file
			if (!file.name.endsWith(".txt")) return
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
				println lcm.formatDups(concept, lm)
				
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
	void test0() {
		new File(src).eachFile{file->
			println file
			file.text.eachLine{
				if (!it) return
				if (it =~ /^[ \t#]+/) {
					return
				}
				def f = it.split("\t")
				if (f.size() != 2) 
					throw new Exception("Unexpected data: $it")
				def concept = f[0]
				def defn = f[1]
				println "$concept, $defn\n"
			}
		}
	}
}