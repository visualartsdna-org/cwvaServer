package rdf.util

import rdf.JenaUtilities

class Policy {
	static def ju = new JenaUtilities()

	static def load() {
		def home = "../cwva"
			try {
				home = cwva.Server.instance.cfg.dir
			} catch (Exception ex) {
				// defaulted
			}
		def l=[]
		def s=""
		new File("$home/res/Policy.upd").text.eachLine{
			if (it.startsWith("# update delimiter")) {
				if (s != "") l += s
				s=""
			}
			if (it.startsWith("#")) return
				s += "$it\n"
		}
		if (s != "") l += s  // last one
		l
	}

	static def exec(model) {
		def l = []
		def n = 0
		try {
			l = load()
			l.each{
				try {
					ju.queryExecUpdate( model,"", it)
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
}
