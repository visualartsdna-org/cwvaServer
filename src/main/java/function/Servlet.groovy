package function
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import groovy.json.JsonBuilder
import java.text.SimpleDateFormat
import services.*
import support.Guid
import support.ParseDigitalQuery
import support.ParseQuery
import support.TagModel
import support.ConceptModel
import util.Tmp

class Servlet extends HttpServlet {

	def metrics = [:]
	def cfg = Server.getInstance().cfg
	def dbm = Server.getInstance().dbm
	def dir = cfg.dir
	def vocab = cfg.vocab
	def data = cfg.data
	def tags = cfg.tags
	def vm = new ConceptModel(vocab)
	def tm = new TagModel(data,vocab,tags,cfg.host)
	
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
		
		def tmp
		setState(path)
		if (cfg.verbose) println "$path ${query?:""}"
		switch (path) {

			case "/tag":
				//sendHtmlFile(response, "/temp/junk/vocab.html")
				def s = tm.process("the:visualArtTerm","vad:Watercolor")
				sendHtml(response, "$s")
			break
			
			case "/tag.entry":
				def m = vm.parse(query)
				m.each { println it }
				def s = tm.handleQueryParams(m)
				sendHtml(response, "$s")
				break
	
			case "/vocab":
				//sendHtmlFile(response, "/temp/junk/vocab.html")
				def s = vm.initial("the:visualArtTerm")
				sendHtml(response, "$s")
			break
			
			case "/vocab.entry":
				def m = vm.parse(query)
				def s = vm.handleQueryParams(m)
				sendHtml(response, "$s")
				break
	
			case "/vocab.entry.text":
				def m = vm.parse(query)
				def inst = URLDecoder.decode(m.instance, "UTF-8")
				m.instance = inst
				def s = vm.handleQueryParamsText(m)
				sendHtml(response, "$s")
				break
	
			case "/fxai/state":
				sendTextFile(response,"./fxaiState.txt")
				break

			case "/fxai/stop":
				new File("./fxaiState.txt").text = "0"
				sendTextFile(response,"./fxaiState.txt")
				break

			case "/fxai/start":
				new File("./fxaiState.txt").text = "1"
				sendTextFile(response,"./fxaiState.txt")
				break
				
			case "/rdf/entry":
				//sendHtmlFile(response, "./rdfForm.html")
			def jl2h = new HtmlForm2Ttl()
			def guid = new Guid().get()
			//def guid = "abc"
			def s = jl2h.printHtml(guid)
			sendHtml(response,s)

				break
				
			case "/rdf/digital":
				//sendHtmlFile(response, "./rdfForm.html")
			def jl2h = new HtmlForm2DigitalTtl()
			def guid = new Guid().get()
			//def guid = "abc"
			def s = jl2h.printHtml(guid)
			sendHtml(response,s)

				break
				
			case "/rdf_page.entry":
			
				def tpls = new ParseQuery().parse(query)
				sendText(response,"$tpls")
				
				break
	
			case "/rdf_digital.entry":
			
				def tpls = new ParseDigitalQuery().parse(query)
				sendText(response,"$tpls")
				
				break
	
			case ~/\/images.*/:
				sendJpegFile(response,"$dir${path.replaceAll("%20"," ")}")
				break

			case "/":
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
				
			default:
				logOut "unrecognized command $path, $query"
				break
		}
		response.setStatus(HttpServletResponse.SC_OK);
		if (tmp) Tmp.delTemp(tmp)
		
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
		response.setContentType("image/jpeg");
		def payload = new File(file).readBytes()
		def os = response.getOutputStream()
		os.write(payload)
	}
	def sendJpegFile(response, file) {
		response.setContentType("image/jpeg");
		sendImageFile(response, file)
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
