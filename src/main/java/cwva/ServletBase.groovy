package cwva

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import groovy.json.JsonBuilder
import groovy.io.FileType

class ServletBase extends HttpServlet {

	def metrics = [:]
	
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
				def f = loadImage(images,path)
				sendJpegFile(response,f)
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

	/**
	 * load request path images 
	 * The policy is to search for the image
	 * from the parent path of the request path 
	 * below the images root dir
	 * breadth-first recursive through subdirs
	 * returning the first one found.
	 * This allows for folder organization of 
	 * image files. 
	 * E.g., request for images/myfile.jpg
	 * may be found at /content/images/stuff/myfile.jpg
	 * @param dir the base content folder containing images
	 * @param fn the request path for the image
	 * @return
	 */
	def loadImage(dir,fn) {
		def fl=[]
		def file = fn.replaceAll("%20"," ")
		def p = new File(file).getParent()
		def f = new File(file).getName()
		def path = dir + p
		if (new File(path).isDirectory()) {
			new File(path).eachFileRecurse(FileType.FILES) {
				if (it.name == f)
					fl += it
			}
		}
		assert !fl.isEmpty(), "$f not found"
		fl.first()
	}


}
