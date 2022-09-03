package cwva

import java.text.SimpleDateFormat
import org.eclipse.jetty.servlet.ServletHandler;
import rdf.util.DBMgr

// singleton
class Server {
	
	static Server instance
	static def content = "/temp/git/cwvaContent"
	
	def cfg
	def dbm
	
	Server(){
		this([ // default test cfg
			port:8080,
			dir:"/temp/git/cwva",
			data: "$content/ttl/data",
			vocab: "$content/ttl/vocab",
			tags: "$content/ttl/tags",
			model: "$content/ttl/model",
			images: "$content/../..",
			domain: "http://visualartsdna.org" ,
			ns: "work",
			host: "http://localhost:8080",
			verbose: true
			])
	}
	
	Server(cfg){
		this.cfg = cfg
		instance = this
		cfg.each { println it }
		dbm = new DBMgr(cfg)
		dbm.print()
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
