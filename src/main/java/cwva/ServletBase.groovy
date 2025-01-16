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
import java.nio.charset.StandardCharsets

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
				def qm = parse(query)
				def token = qm.token
				def cmd = qm.cmd
				def parm = qm.parm
				def b = new Token().validate(token)
				if (b) {
					println "$cmd, $parm"
					switch (cmd) {
						
						case "restart":
						def server = Server.getInstance()
// this can go						if (!server) server = function.Server.getInstance()
						server.dbm = new rdf.util.DBMgr(server.cfg)
						server.dbm.print()
						util.Gcp.folderCleanup(
							"images", // gDir
							cfg.images,	// fDir
							/.*\.JPG|.*\.jpg/) // filter
				
						// pass cmd to any remoteHost
						if (cfg.twinHost && cfg.primaryHost) {
							def url = "${cfg.twinHost}$path"
							if (query) url = "$url?$query"
							def s = new URL(url).getText()
						}
		
						sendJson(response,new JsonBuilder([status:"ok"]))
						break;
						
						case "stats":
							def os = System.getProperty("os.name")
							def c = ""
							if (os.contains("Linux")) 
								c = "./status.s"
							else if (os.contains("Windows")) 
								c = "C:\\stage\\bin\\stats.bat"
							def s = new util.Exec().exec(c)
							s += cwva.Server.getInstance().dbm.print()
							sendJson(response,new JsonBuilder([stats:"$s"]))
						break;
						
					}
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
				sendJson(response,""+[status:"ok"])
				sleep(1000) // time to let the response clear
				logOut(payload)
				logOut "fini"
				System.exit(0)
				break
				
			case "/favicon.ico":
				sendIconFile(response,"$images/favicon.ico")
				break

			default:
				logOut "unrecognized command $path, ${query?:""}"
				//throw new RuntimeException("unrecognized command $path, ${query?:""}")
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
	
	static def parse(query) {
		def m=[:]
		if (!query) return m
		def al = query.split(/&/)
		al.each {
			def av = it.split(/=/)
			av[0] = av[0].replaceAll("%3A",":")
			if (av.size()==2)
				m[av[0]]=java.net.URLDecoder.decode(av[1], StandardCharsets.UTF_8.name())
			 }
		m

	}

	def logOut(s) {
		Server.getInstance().logOut(s)
	}

	def policy
	def policyAccept(name,s) {
		if (!policy)
			policy = util.Rson.load("${cwva.Server.getInstance().cfg.dir}/res/servletPolicy.rson")
		policy[name].path.any { it != "" && s =~ /$it/}
	}

}
