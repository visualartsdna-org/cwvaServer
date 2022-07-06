package cwva
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import groovy.json.JsonBuilder
import java.text.SimpleDateFormat
import rdf.JenaUtils
import rdf.QuerySupport
import services.*
import util.Tmp

class Servlet extends HttpServlet {

	def tmp = new Tmp()
	def metrics = [:]
	def cfg = Server.getInstance().cfg
	def dir = cfg.dir
	def model = "$dir/${cfg.model}"
	def data = "$dir/${cfg.data}"
	def domain = cfg.domain
	def ns = cfg.ns
	def ju = new JenaUtils()
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {

		try {
			handler(request._uri._path,request._uri._query,response)
		} catch (Exception e) {
			logOut e.printStackTrace()
			throw new RuntimeException("Something went wrong")
		}
	}

	def handler(path,query,response) {
		
		def tmpFile
		setState(path)
		if (cfg.verbose) println "$path ${query?:""}"
		switch (path) {

			case "/model":
				sendTextFile(response,"$model/cwva.ttl")
				break

			case "/2021/07/16/model":
				sendTextFile(response,"$model/cwva.ttl")
				break

			case "/data":
				sendModelFile(response, data)
//				sendTextFile(response,"$data/art.ttl")
				break

			case ~/\/d3\..*/:
				def n = path.lastIndexOf(".")
				def kind="all"
				if (n>=0) kind = path.substring(n+1)
				tmpFile = tmp.getTemp("d3f",".html")
				new D3Driver().doGet(data,kind,tmpFile)
				sendHtmlFile(response,tmpFile)
				break

			case "/model.graph":
				tmpFile = tmp.getTemp("dot",".html")
				new DotDriver().doGet(model,"svg",tmpFile)
				sendHtmlFile(response,tmpFile)
				break

			case "/dataModel.graph":
				tmpFile = tmp.getTemp("dot",".html")
				new DotDriver().doGet(data,"svgImpOnto",tmpFile)
				sendHtmlFile(response,tmpFile)
				break

			case ~/\/lsys\..*/:
				def n = path.lastIndexOf(".")
				def kind="all"
				if (n>=0) kind = path.substring(n+1)
				tmpFile = tmp.getTemp("lsys",".jpg")
				new LsysDriver().doGet(data,kind,tmpFile)
				sendJpegFile(response,tmpFile)
				break

			case ~/\/images.*/:
				sendJpegFile(response,"$dir${path.replaceAll("%20"," ")}")
				break

			case ~/\/dist.*/:
				sendJSFile(response,"$dir${path}")
				break

			case "/graphInstructions.html":
				sendHtmlFile(response,"$dir${path}")
				break

			case ~/\/work.*/:
				def jl2h = new JsonLd2Html()
				def relPath = jl2h.parsePath(path)
				def guid = jl2h.parseGuid(path)
				
				if (query) {
					def fmt = (query =~ /^format=([a-zA-Z-\/]+)[&]?.*$/)[0][1]
					def qs = new QuerySupport(data)
					def m = qs.getOneInstanceModel("work",guid)
					
//					def m = ju.loadFiles(data)
//					def m2 = jl2h.getOneInstanceModel(m,"work",guid)
					sendModel(response, m, fmt.toLowerCase())
				} else {
					def s = jl2h.process(data,model,domain,relPath,ns,guid,cfg.host)
					sendHtml(response,s)
				}
				break

			case "/browse":
				def s = new BrowseWorks().browse(cfg.host,data)
				sendHtml(response,s)
				break

			case "/browse2":
				sendHtmlFile(response,"$dir/browse.html")
				break

			//			case "/json*":
			//				sendJsonFile(response,"$dirname/$path")
			//				break

			case "/":
				def s = new IndexHtml(cfg).get()
				sendHtml(response,s)
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
				sendIconFile(response,"$dir/images/favicon.ico")
				break

			default:
				logOut "unrecognized command $path, $query"
				break
		}
		response.setStatus(HttpServletResponse.SC_OK);
		tmp.rmTemps()
		
	}

	def sendHtml(response, so) {
		response.setContentType("text/html");
		response.getWriter().println(so)

	}
	def sendHtmlFile(response, file) {
		response.setContentType("text/html");
		response.getWriter().println(new File(file).text)

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
		def payload = new File(file).readBytes()
		def os = response.getOutputStream()
		os.write(payload)
	}
	def sendJpegFile(response, file) {
		response.setContentType("image/jpeg");
		sendImageFile(response, file)
	}
	def sendIconFile(response, file) {
		response.setContentType("image/x-icon");
		sendImageFile(response, file)
	}
	def sendModelFile(response, fileSpec) {
		//response.setContentType("text/plain")
		def m = ju.loadFiles(fileSpec)
		sendModel(response, m, "TTL")
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

	def logOut(s) {
		Server.getInstance().logOut(s)
	}

}
