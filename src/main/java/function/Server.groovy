package function

import java.text.SimpleDateFormat
import org.eclipse.jetty.servlet.ServletHandler;
import rdf.util.DBMgr

// singleton
class Server {
	
	static Server instance
	static def content = "/temp/git/cwvaContent"
	static def port = 8082
	static version = ""
	
	def cfg
	def dbm
	Server(){
		this([ // default test cfg
			port:port,
			dir:"/temp/git/cwva",
			data: "$content/ttl/data",
			vocab: "$content/ttl/vocab/vocabulary.ttl",
			tags: "$content/ttl/tags/tags.ttl",
			studies: "$content/ttl/studies",
			model: "$content/ttl/model",
			images: "$content/../../images",
//			domain: "http://visualartsdna.org" ,
//			ns: "work",
			host: "http://192.168.1.71:$port",
			verbose: true,
			verboseAll: false,
			artist: [
				rspates: [
					dir: "C:/temp/git/rspates",
					imageDir: "C:/temp/git/rspates/images",
					path: "/artist/rspates",
					cache: "C:/temp/git/rspates/cache"
					]
				]
			])
	}
	
	Server(cfg){
		this.cfg = cfg
		instance = this
		cfg.each { println it }
		dbm = new DBMgr(cfg)
		dbm.print()
		version = util.BuildProperties.getProperties()
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
