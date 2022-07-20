package function
import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import util.*

class Main {

	@Test
	void test() {
		new Server().startJetty()
		
	}
	
	@Test
	void testStudy() {
		def content0 = "/temp/git/cwvaContent"
		def content = "/temp/git/cwvaStudyContent"
		
		def cfg = [ // default test cfg
			port:8082,
			dir:"$content",
			data: "$content/ttl/data",
			vocab: "$content0/ttl/vocab/vocabulary.ttl",
			tags: "$content/ttl/tags/tags.ttl",
			model: "$content0/ttl/model",
			images: "$content/images",
//			domain: "http://visualartsdna.org" ,
//			ns: "work",
			host: "http://localhost:8082",
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
function.Server -cfg fcnServer.rson
"""
			return
		}
		def cfg = map["cfg"]
		assert cfg , "no cfg"
		
		def cfgMap = Rson.load(cfg)
		new Server(cfgMap).startJetty()
	
	}
}
