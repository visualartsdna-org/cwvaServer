package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import support.util.LoadConceptMatch

class PreserveCmtTtlLoad {

	def ju = new JenaUtilities()
	
	// This is a pattern
	// that reads a src ttl file
	// saves its comments
	// loads the ttl into a model
	// does a callback with the model
	// and comment string
	// returns a new comment string
	// saves the model to a tgt file
	// appends the original ttl file
	// comments to the end of the tgt
	// then appends any new comments 
	// from the callback 
	// to the end of the tgt
	// Useful when the model is processed
	// resulting in added commentary
	@Test
	void test0() {
		load("C:/stage/conceptQueue/ttl/Abstraction Metrics.ttl",
			"c:/stage/tmp/Abstraction Metrics.ttl",
			this.&callme)
	}
	

	def callme(m,cmts) {
		println "model size = ${m.size()}"
		println "cmts size = ${cmts.size()}"
		"all done\n"
	}

	def load(src,tgt, Closure callback) {
		def f = new File(src)
		if (f.isDirectory()) {
			f.eachFile{
				loadFile(f.absolutePath,"$tgt/${f.name}", callback)
			}
		} else {
			loadFile(src,tgt, callback)
		}
	}
		
	def loadFile(src,tgt, Closure callback) {
		def file = new File(src)
		if (!file.isDirectory()) {
			
			def cmts = saveComments(file)
			def m = ju.loadFiles(src) 
			def cmts2 = callback(m,cmts)
				
			ju.saveModelFile(m,tgt,"ttl")
			
			def tf = new File(tgt)
			tf << "\n"
			tf << cmts
			if (cmts2) {
				tf << "\n# comment addendum ${new Date()}\n\n"
				tf << cmts2
			}
		}
	}
	
	def saveComments(File file) {
		def s=""
		file.eachLine{
			if (it.startsWith("#")) 
				s+= "$it\n"
		}
		s
	}
	
}
