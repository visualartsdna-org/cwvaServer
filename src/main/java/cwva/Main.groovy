package cwva
import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import util.*
	import org.eclipse.jetty.util.log.Logger;

class Main {

	@Test
	void test() {
	def content = "/temp/git/cwvaContent"
	def port = 80

		new Server([ // default test cfg
			port: port,
			dir:"/temp/git/cwva",
			cloud:[src:"ttl",tgt:content],
			data: "$content/ttl/data",
			vocab: "$content/ttl/vocab",
			tags: "$content/ttl/tags",
			model: "$content/ttl/model",
			images: "$content/../../images",
			domain: "http://visualartsdna.org" ,
			ns: "work",
			host: "http://192.168.1.71:$port",
			functionHost: "http://192.168.1.71:8082",
			verbose: true,
			sparql: true
		]).startJetty()
		
	}
	
	@Test
	void testStudy() {
		def content0 = "/temp/git/cwvaContent"
		def content = "/temp/git/cwvaStudyContent"
		
		def cfg = [ // default test cfg
			port:8080,
			dir:"$content",
			data: "$content/ttl/data",
			vocab: "$content0/ttl/vocab/vocabulary.ttl",
			tags: "$content/ttl/tags/tags.ttl",
			model: "$content0/ttl/model",
			images: "$content/images",
//			domain: "http://visualartsdna.org" ,
//			ns: "work",
			host: "http://localhost:8080",
			verbose: true
			]
		new Server(cfg).startJetty()
	}


	/**
	 * Run a cwva server
	 * @param args
	 */
	public static void main(String[] args){

		def map = Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, 
cwva.Server -cfg cwvaServer.rson
"""
			return
		}
		def cfg = map["cfg"]
		//assert cfg , "no cfg"
		if (cfg==null) {
			new Server().startJetty()
		}
		else {
			def cfgMap = Rson.load(cfg)
			new Server(cfgMap).startJetty()
		}
		
//		def cfgMap = Rson.load(cfg)
//		new Server(cfgMap).startJetty()
	
	}
}
