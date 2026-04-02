package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.apache.jena.rdf.model.Model
import org.junit.jupiter.api.Test
import rdf.JenaUtilities


/*
testNew(), from an update file (nav.upd) collect 
concepts and broader relations, create ttl for 
missing concepts or broaders
test(), using an update file (nav.upd) collect 
concepts and broader relations, delete the concepts 
current broader and replace with the one from the 
update file
test0(), load ttls from src and inject a property 
to each instance (concept, scheme, collection) 
for its origin file. Save by selecting for 
list of filenames in query, then describe each of 
those instances to ttl, then delete the filename 
from the instance and save to tgt by that fname.
Differences between src and tgt files, m0-m1 and m1-m0. 
*/
class LoadTtlKeepFile {
	
	def ju = new JenaUtilities()
	def src = "/temp/git/cwvaContent/ttl/vocab"
	def tgt = "C:/stage/tmp/test"
	def script = "C:/stage/tmp/nav.upd"

	@Test
	void testNew() {
		def s = checkNewConcept()
		println s
	}
	
	def checkNewConcept() {
		def m=[:]
		new File(script).text.eachLine{
			
			if (it.startsWith("#")) return
			if (it.trim() == "") return
			def fs =  it.split("\t")
			if (fs.size() != 2) return
			m[fs[0]]=""
			m[fs[1]]=""
		}
		def model = ju.loadFiles(src)
		def s = rdf.Prefixes.forFile
		m.each{k,v->
			def lm = ju.queryListMap1(model,
				rdf.Prefixes.forQuery,"""
select * {
	$k ?p ?o 
}
""")
			
			if (!lm.size()) {
				println "new $k"
				s += """
$k a skos:Concept ;
	    rdfs:label         "$k" ;
        skos:definition    "" ;
        skos:inScheme      the:visualArtTerms ;
 .

"""
			}
		}
		s
	}
	
	
	// run update
	@Test
	void test() {
		
		def m = load(src)
		println "size = ${m.size()}"
		
		// update
		update(m,script)
		
		
		save(m,tgt)
		diff(src,tgt)
	}
	
	def loadScript(script) {
			def l=[]
			def s=""
		
		new File(script).text.eachLine{
	
			if (it.startsWith("#")) return
			if (it.trim() == "") return
			def fs =  it.split("\t")
			if (fs.size() != 2) return
			l += """
delete {${fs[1]} skos:broader ?b} where { ${fs[1]} skos:broader ?b }
"""
			l += """
insert data {${fs[1]} skos:broader ${fs[0]}}
"""
		}
		l
	}

	def update(model,script) {
		def n = 0
		try {
			def l = loadScript(script)
			l.each{
				try {
					ju.queryExecUpdate( model,rdf.Prefixes.forQuery, it)
					n++
				} catch (Exception ex) {
					println "ERROR in policy update:\n$it"
					println "$ex"
				}
			}
		} catch (Exception ex) {
			println "ERROR loading policy update file"
			println "$ex"
		}
		println "Policy update complete.  $n updates executed."
		model
	}

	
	@Test
	void test0() {
		
		def m = load(src)
		println "size = ${m.size()}"
		save(m,tgt)
		diff(src,tgt)
	}
	
	def diff(src,tgt) {
		new File(src).eachFile{file->
			println file.name
			def m0 = ju.loadFiles(file.absolutePath)
			def m1 = ju.loadFiles("$tgt/${file.name}")
			def m01 = m0.difference(m1)
			def m10 = m1.difference(m0)
			println "diff m0-m1 = ${m01.size()}"
			println "diff m1-m0 = ${m10.size()}"
			if (m01.size()) {
				println "m0-m1"
				println ju.saveModelString(m01,"ttl")
			}
			if (m10.size()) {
				println "m1-m0"
				println ju.saveModelString(m10,"ttl")
			}
		}
	}
	
	def load(src) {
		
		def model = ju.newModel()
		
		new File(src).eachFile{file->
			if (!file.name.endsWith(".ttl")) return
			def m = ju.loadFiles(file.absolutePath)
			inject(m,file.name)
			model.add m
		}
		model
	}
	
	def save(model, tgt) {
		
		def lm = ju.queryListMap1(model,rdf.Prefixes.forQuery,"""
		select distinct ?fn {
			?s vad:srcFile ?fn 
		}
""")

		lm.each{
			def fmodel = ju.newModel()
			
			fmodel.add ju.queryDescribe(model,rdf.Prefixes.forQuery,"""
		describe ?s {
			?s vad:srcFile "${it.fn}"
		}
""")
			delFname(fmodel, it.fn)
			
			ju.saveModelFile(fmodel,"$tgt/${it.fn}","ttl")

		}
	}
	
	def inject(m,fname) {
		ju.queryExecUpdate(m,rdf.Prefixes.forQuery,"""
			insert {?s vad:srcFile "$fname" }
			where {
				?s a ?t .
				filter (?t in (skos:Concept, 
				skos:Collection, 
				skos:ConceptScheme, 
				rdfs:Resource,
				rdf:Property))
			}
""")
	}
	
	def delFname(m, fname) {
		ju.queryExecUpdate(m,rdf.Prefixes.forQuery,"""
			delete {?s vad:srcFile "$fname" }
			where {
				?s a ?t .
				filter (?t in (skos:Concept, 
				skos:Collection, 
				skos:ConceptScheme, 
				rdfs:Resource,
				rdf:Property))
			}
""")
	}
	

}
