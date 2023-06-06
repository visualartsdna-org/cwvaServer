package cwva

import java.text.SimpleDateFormat
import org.eclipse.jetty.servlet.ServletHandler;
import rdf.util.DBMgr

// singleton
class Server {

	static Server instance
	static version = ""

	def cfg
	def dbm

	Server(cfg){

		this.cfg = cfg
		instance = this
		cfg.each { println it }
		dbm = new DBMgr(cfg)
		dbm.print()
		util.Gcp.folderCleanup(
			"images", // gDir
			cfg.images,	// fDir
			/.*\.JPG|.*\.jpg/) // filter
		version = util.BuildProperties.getProperties()
	}
	
	static def getInstance() {
		instance
	}
	
	def startJetty() {

		startJetty(cfg.port,Servlet.class)
	}

	def startJetty(theClass) {

		startJetty(cfg.port,theClass)
	}

	def startJetty(port,theClass) {

		def server = new org.eclipse.jetty.server.Server(port)

		def handler = new ServletHandler();
		server.setHandler(handler);

		handler.addServletWithMapping(theClass, "/*");
		server.start();
		server.join();
	}

	def logOut(s) {
		def time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())
		println "$time\t$s"
	}

}
