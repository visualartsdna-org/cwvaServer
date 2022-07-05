package function

import java.text.SimpleDateFormat
import org.eclipse.jetty.servlet.ServletHandler;

// singleton
class Server {
	
	static Server instance
	
	def cfg
	Server(){
		this([ // default test cfg
			port:8082,
			dir:"/test/function",
//			data: "ttl/art.ttl",
//			model: "ttl/cwva.ttl",
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
