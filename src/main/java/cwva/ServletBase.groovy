package cwva

import jakarta.servlet.ServletException;
import util.Token
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import rdf.JenaUtilities
import groovy.json.JsonBuilder
import util.FileUtil
import util.Tmp

class ServletBase extends HttpServlet {

	def tmp = new Tmp()
	def cfg = Server.getInstance().cfg
	def dir = cfg.dir
	def model = cfg.model
	def vocab = cfg.vocab
	def data = cfg.data
	def tags = cfg.tags
	def domain = cfg.domain
	def ns = cfg.ns
	def metrics = [:]
	def ju = new JenaUtilities()
	
	def dbm() {
		Server.getInstance().dbm
	}

	def serve(cfg,path,query,response) {
		def dir = cfg.dir
		def model = cfg.model
		def vocab = cfg.vocab
		def data = cfg.data
		def tags = cfg.tags
		def domain = cfg.domain
		def images = cfg.images
	
		switch(path) {
			case ~/\/images.*/:
				def f = FileUtil.loadImage(images,path)
				sendJpegFile(response,f)
				break

			case "/cmd":
				def qm = queryToMap(query)
				def token = qm.token
				def cmd = qm.cmd
				def parm = qm.parm
				def b = new Token().validate(token)
				if (b) {
					println "$cmd, $parm"
					switch (cmd) {
						
						case "restart":
						def server = Server.getInstance()
						if (!server) server = function.Server.getInstance()
						server.dbm = new rdf.util.DBMgr(server.cfg)
						server.dbm.print()
						util.Gcp.folderCleanup(
							"images", // gDir
							cfg.images,	// fDir
							/.*\.JPG|.*\.jpg/) // filter
				
						break;
						
					}
					sendJson(response,new JsonBuilder([status:"ok"]))
				}
				break

			case "/status":
				sendJson(response,""+[status:"ok"])
				break

			case "/metrics":
				def payload = new JsonBuilder(metrics).toPrettyString()
				logOut(payload)
				sendJson(response,payload)
				break

			case "/cestfini":
				def payload = new JsonBuilder(metrics).toPrettyString()
				logOut(payload)
				logOut "fini"
				System.exit(0)
				break
				
			case "/favicon.ico":
				sendIconFile(response,"$images/favicon.ico")
				break

			default:
				logOut "unrecognized command $path, ${query?:""}"
				break
		}
	}
	
	def sendHtml(response, so) {
		response.setContentType("text/html");
		response.getWriter().println(so)

	}
	def sendHtmlFile(response, file) {
		response.setContentType("text/html");
		response.getWriter().println(new File(file).text)

	}
	def sendText(response, text) {
		response.setContentType("text/plain");
		response.getWriter().println(text)
	
	}
	def sendTextFile(response, file) {
		response.setContentType("text/plain");
		response.getWriter().println(new File(file).text)

	}
	def sendJSFile(response, file) {
		response.setContentType("text/javascript");
		response.getWriter().println(new File(file).text)

	}
	def sendCSSFile(response, file) {
		response.setContentType("text/css");
		response.getWriter().println(new File(file).text)

	}
	def sendJsonFile(response, file) {
		response.setContentType("application/json");
		response.getWriter().println(new File(file).text)

	}
	def sendJson(response, s) {
		response.setContentType("application/json");
		response.getWriter().println("$s")

	}
	def sendImageFile(response, file) {
		def payload = file.readBytes()
		def os = response.getOutputStream()
		os.write(payload)
	}
	def sendJpegFile(response, file) {
		response.setContentType("image/jpeg");
		sendImageFile(response, file)
	}
	def sendGifFile(response, file) {
		response.setContentType("image/gif");
		sendImageFile(response, file)
	}
	def sendIconFile(response, String file) {
		sendIconFile(response, new File(file))
	}
	def sendIconFile(response, File file) {
		response.setContentType("image/x-icon");
		sendImageFile(response, file)
	}
	def sendModelFile(response, fileSpec) {
		def m = ju.loadFiles(fileSpec)
		sendModel(response, m, "TTL")
	}
	def sendModel(response, model) {
		sendModel(response, model, "TTL")
	}
	// supported types: ttl rdf/xml jsonld json-ld nt nq trig trix rt trdf
	// TODO: add: rdfa microdata
	def sendModel(response, m, type) {
		response.setContentType("text/plain")
		def s = ju.saveModelString(m, type)
		response.getWriter().println("$s")
	}

	def setState(k) {
		if (!metrics[k]) {
			metrics[k]=0
		}
		metrics[k]++
	}
	
	// token=123&cmd=abc
	def queryToMap(query) {
		def m=[:]
		def l = query.split("&")
		l.each{
			def l2 = it.split("=")
			m[l2[0]]=l2[1]
		}
		m
	}

}
