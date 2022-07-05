package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtils

class ClassHierarchy {

	// TODO: add print format for hierarchy
	// tabs indicate subclass relations
	@Test
	void test() {
		
		def dir="C:/test/cwva/ttl/model"
		def ju = new JenaUtils()
		def m = ju.loadFiles(dir)
		def js = ju.saveModelString(m,"JSON-LD")
		def col = new JsonSlurper().parseText(js)
		
		def ch = [:]
		col["@graph"].each { 
			if (it["@id"].startsWith("_")) return
			if (it["@id"] == "http://visualartsdna.org/2021/07/16/model#") return
			
			def c = it["@id"]
			def p = it["subClassOf"]
//			if (c == "cwva:Art") {
//				println "here"
//			}
			if (p != null
				&& c != null
				&& !ch.containsKey(p)){
				ch[p]=[]
			} 
			if (p != null)
			ch[p].add c
			
		}
		ch.each{
			println it
		}
	}

	@Test
	void test1() {
		
		def dir="C:/temp/cwva"
		def ju = new JenaUtils()
		def m = ju.loadFiles(dir)
		def js = ju.saveModelString(m,"JSON-LD")
		def c = new JsonSlurper().parseText(js)
		
		def ch = [:]
		c["@graph"].each { 
			if (it["@id"].startsWith("_")) return
			if (it["@id"] == "http://visualartsdna.org/2021/07/16/model#") return
			//println "${it["@id"]}\t->${it["subClassOf"]}" 
			if (!ch.containsKey(it["@id"])) {
				ch[it["@id"]]=[]
			}
			ch[it["@id"]] += it["subClassOf"]
		}
		ch.sort().each{
			println it
		}
	}

	@Test
	void test0() {
		
		def dir="C:/test/cwva/ttl/model"
		def ju = new JenaUtils()
		def m = ju.loadFiles(dir)
		def js = ju.saveModelString(m,"JSON-LD")
		def c = new JsonSlurper().parseText(js)
		
		c["@graph"].each { 
			if (it["@id"].startsWith("_")) return
			println "${it["@id"]}\t->${it["subClassOf"]}" 
			}
	}

}
