package function
import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import util.*
import cwva.Server

class Main {

	@Test
	void test() {
	def content = "/temp/git/cwvaContent"
	def port = 8082
		new Server([ // default test cfg
			port:port,
			dir:"/temp/git/cwva",
			data: "$content/ttl/data",
			vocab: "$content/ttl/vocab",
			tags: "$content/ttl/tags", // was ../tags.ttl
			studies: "$content/ttl/studies",
			model: "$content/ttl/model",
			images: "$content/../../images",
			cloud:[src:"ttl",tgt:content],
//			domain: "http://visualartsdna.org" ,
//			ns: "work",
			host: "http://192.168.1.71:$port",
			twinHost: "http://192.168.1.71:80",
			verbose: true,
			verboseAll: false,
			clobber: false,
			multithreaded: false,
			primaryHost: false,
			artist: [
				rspates: [
					dir: "C:/temp/git/rspates",
					imageDir: "C:/temp/git/rspates/images",
					path: "/artist/rspates",
					cache: "C:/temp/git/rspates/cache"
					]
				]
			]).startJetty(Servlet.class)
		
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
		new Server(cfg).startJetty(Servlet.class)
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
		new Server(cfgMap).startJetty(Servlet.class)
	
	}
}
