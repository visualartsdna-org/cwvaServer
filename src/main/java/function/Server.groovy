package function

import java.text.SimpleDateFormat
import org.eclipse.jetty.servlet.ServletHandler;

// singleton
class Server {
	
	static Server instance
	static def content = "/temp/git/cwvaContent"
	
	def cfg
	Server(){
		this([ // default test cfg
			port:8082,
			dir:"/test/function",
			data: "$content/ttl/data",
			vocab: "$content/ttl/vocab/vocabulary.ttl",
			tags: "$content/ttl/tags/tags.ttl",
			model: "$content/ttl/model",
			images: "$content/images",
//			domain: "http://visualartsdna.org" ,
//			ns: "work",
			host: "http://localhost:8082",
			verbose: true
			])
	}
	
	Server(cfg){
		this.cfg = cfg
		instance = this
		cfg.each { println it }

	}
	
	static def getInstance() {
		instance
	}

	def startJetty() {

		startJetty(cfg.port)
	}

	def startJetty(port) {

		def server = new org.eclipse.jetty.server.Server(port)

		def handler = new ServletHandler();
		server.setHandler(handler);

		handler.addServletWithMapping(Servlet.class, "/*");
		server.start();
		server.join();
	}

	def logOut(s) {
		def time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
		println "$time\t$s"
	}

}
