package support

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test

class AuthoringTest {
	
	
	@Test
	void test() {
		def a = new Authoring(null)
		a.loadSession("rickspatesart")
		def tl = a.qTopicLinks("the:CatalogOfWorks_20250205")
		tl.each{
			println it
		}
//		def g = a.qSubTopics([])
//		g.each {
//			println it
//		}
	}
	
	@Test
	void test8() {
		def a = new Authoring(null)
		a.loadSession("rickspatesart")
		def g = a.getGraph(a.tbox)
		g.each {
			println it
		}
	}
	

	// from the hierarchy of topics
	// find all "head" elements in memberlists
	@Test
	void test7() {
		def a = new Authoring(null)
		a.loadSession("rickspatesart")
		def g = a.getGraph(a.abox)
		def set = getHeadsInHierarchy(g)
		set.each{k->
			println k
		}
	}
	
	def getHeadsInHierarchy(g) {
		def set=[]
		g.each{
			def l = new JsonSlurper().parseText(it["memberList"])
			l.each{t->
				def h = g.find{
					it["@id"] == t
				}
				if (h)
					set += h.head
			}
		}
		set
	}
	
	// topics in hierarchy more than once
	@Test
	void test6() {
		def a = new Authoring(null)
		a.loadSession("rickspatesart")
		def g = a.getGraph(a.abox)
		def set=[:]
		g.each{
			println it
			def l = new JsonSlurper().parseText(it["memberList"])
			l.each{t->
				def h = g.find{
					it["@id"] == t
				}
				println "$t, ${h.head}"
				if (!set.containsKey(t))
					set[t] = null
				else throw new Exception("$t exists in multiple places")
			}
		}
	}
	
	// difference last versioned topics.ttl
	@Test
	void test5() {
		def a = new Authoring()
		println a.difference()
	}
	
	@Test
	void test4() {
		def a = new Authoring()
		//def topic = "tko:Holocene"
		def topic = "tko:ExtinctionStatement"
		//def js = a.qTest(topic)
		def js =  a.qTopicLinks(topic)
		println js
	}
	
	@Test
	void test3() {
		def a = new Authoring()
		def js =  a.getGraph(a.model)

	}
	
	@Test
	void test2() {
		def a = new Authoring()
		println a.printTopics()
	}
	
	@Test
	void test1() {
		def a = new Authoring()
		println a.printTopics()
		def links = ["tko:Holocene",
			"tko:Anthropocene",
			"tko:EndangeredSpecies",
			"tko:IUCNRedList",
			"tko:EndangeredDesignation",
			]
		def topic = "tko:ExtinctionStatement"
		a.execTopicLinks(topic, links)
		println a.saveSession()
		println a.printTopics()
	}

	@Test
	void test0() {
		def a = new Authoring()
		println a.saveSession()
	}

	@Test
	void test100() {
		def l = [1,2,3]
		def l2 = [4,5]
		println l += l2
	}

}
