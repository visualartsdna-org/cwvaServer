package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class AuthoringTest {
	
	

	@Test
	void test() {
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

}
