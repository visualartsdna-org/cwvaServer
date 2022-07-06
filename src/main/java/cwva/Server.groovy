package cwva

import java.text.SimpleDateFormat
import org.eclipse.jetty.servlet.ServletHandler;

// singleton
class Server {
	
	static Server instance
	
	def cfg
	Server(){
		this([ // default test cfg
			port:8080,
			dir:"../cwva",
			data: "ttl/data",
			model: "ttl/model",
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
