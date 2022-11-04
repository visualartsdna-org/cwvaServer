package function
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import cwva.ServletBase
import groovy.json.JsonBuilder
import java.text.SimpleDateFormat
import rdf.util.ViaToTtl
import services.*
import support.*
import util.Tmp

class Servlet extends ServletBase {

	//def metrics = [:]
	def cfg = Server.getInstance().cfg
	def dbm = Server.getInstance().dbm
	def dir = cfg.dir
	def images = cfg.images
	def vocab = cfg.vocab
	def data = cfg.data
	def tags = cfg.tags
	def studies = cfg.studies
	def vm = new ConceptModel(vocab)
	def tm = new TagModel(data,vocab,tags,cfg.host)
	def sm = new StudiesModel(dbm.rdfs,studies)
	def artist = [:]

	Servlet(){
		cfg.artist.each{k,v->
			artist[k] = new ArtistSite(v)
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {

		try {
			handler(request._uri._path,request._uri._query,response,request)
		} catch (Exception e) {
			logOut e.printStackTrace()
			throw new RuntimeException("Something went wrong")
		}
	}

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
		handler(path,query,response,null)
	}
		
	def handler(path,query,response,request) {
			
		def tmp
		setState(path)
		def lowLevelRequest = 
		(request && request.getMethod() == "POST") || (path =~ /\/artist\/.*\.jpg/)
		
		if (cfg.verbose && !lowLevelRequest) 
			println "$path ${query?:""}"
		else if (cfg.verboseAll && lowLevelRequest) 
			println "$path ${query?:""}"
			
		switch (path) {

			case ~/\/study.*/:
				def s = sm.process(path,query)
				sendHtml(response, "$s")
				break

			case ~/\/html.*/:
				sendHtmlFile(response,"$dir/${path}")
				break

			case "/":
				sendHtmlFile(response,"$dir/html/function.html")
				break

			case "/studies":
				def s = sm.directory(path,query,cfg.host)
				sendHtml(response, "$s")
				break

			case "/tag":
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
				def jl2h = new HtmlForm2Ttl()
				def guid = new Guid().get()
				def s = jl2h.printHtml(guid)
				sendHtml(response,s)

				break

			case "/rdf/digital":
				def jl2h = new HtmlForm2DigitalTtl()
				def guid = new Guid().get()
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

			case "/translate":

				def base = "${Server.getInstance().cfg.data}/study"
				def html = new ViaToTtl().setup(base)
				sendHtml(response,html)
				
				break

			case ~/\/artist.*/:
				try {
					def name = (path =~ /artist\/([a-z]+)[\/]*.*/)[0][1]
					def svr = artist[name]
					svr.serve(path,query,response,request)
				} catch ( IndexOutOfBoundsException e) {
					 //println "bad path $path"
				}
				break

			default:
				serve(cfg,path,query,response)
				break
		}
		response.setStatus(HttpServletResponse.SC_OK);
		if (tmp) Tmp.delTemp(tmp)
	}
}
