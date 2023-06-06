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

class Servlet extends ServletBase {

	def tmp = new Tmp()
	def cfg = Server.getInstance().cfg
	def dbm = Server.getInstance().dbm
	def dir = cfg.dir
	def model = cfg.model
	def vocab = cfg.vocab
	def data = cfg.data
	def tags = cfg.tags
	def domain = cfg.domain
	def images = cfg.images
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
		def status = HttpServletResponse.SC_OK
		setState(path)
		if (cfg.verbose) println "$path ${query?:""}"
		//dbm.reload()	// optional: useful for dev
		switch (path) {

			case "/model":
				sendTextFile(response,"$model/cwva.ttl")
				break

			case "/2021/07/16/model":
				sendTextFile(response,"$model/cwva.ttl")
				break

			case "/data":
				sendModel(response, dbm.instances)
				break

			case "/vocab":
				sendModel(response, dbm.vocab)
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

			case "/vocab.graph":
				tmpFile = tmp.getTemp("dot",".html")
				new DotDriver().doGet(vocab,"svgVocab",tmpFile)
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

			case ~/\/dist.*/:
				sendJSFile(response,"$dir${path}")
				break

			case ~/\/html.*/:
				sendHtmlFile(response,"$dir/${path}")
				break

			case ~/\/work.*/:
				def jl2h = new JsonLd2Html()
				def relPath = jl2h.parsePath(path)
				def guid = jl2h.parseGuid(path)

				if (query) {
					def fmt = (query =~ /^format=([a-zA-Z-\/]+)[&]?.*$/)[0][1]
					def qs = new QuerySupport(dbm.rdfs)
					def m = qs.getOneInstanceModel("work",guid)

					if (m.size()==0) status = HttpServletResponse.SC_NOT_FOUND
					else sendModel(response, m, fmt.toLowerCase())
				} else {
					def s = jl2h.process(dbm.rdfs,domain,relPath,ns,guid,cfg.host)
					sendHtml(response,s)
				}
				break

			case ~/\/thesaurus.*/:
				def jl2h = new JsonLd2Html()
				def relPath = jl2h.parsePath(path)
				def guid = jl2h.parseConcept(path)

				if (query) {
					def fmt = (query =~ /^format=([a-zA-Z-\/]+)[&]?.*$/)[0][1]
					def qs = new QuerySupport(dbm.rdfs)
					def m = qs.getOneInstanceModel("work",guid)
					
					if (m.size()==0) status = HttpServletResponse.SC_NOT_FOUND
					else sendModel(response, m, fmt.toLowerCase())
				} else {
					def s = jl2h.process(dbm.rdfs,domain,relPath,"the",guid,cfg.host)
					sendHtml(response,s)
				}
				break

			case "/browse":
				def s = new BrowseWorks().browse(cfg.host,dbm.rdfs)
				sendHtml(response,s)
				break

			case "/browse2":
				sendHtmlFile(response,"$dir/browse.html")
				break

			case "/":
				def s = new IndexHtml(cfg).get()
				sendHtml(response,s)
				break

			case "/copyright":
				sendHtmlFile(response,"$dir/html/copyright.html")
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

			case "/fxai/invert":
				new File("./fxaiState.txt").text = "-1"
				sendTextFile(response,"./fxaiState.txt")
				break

			default:
				serve(cfg,path,query,response)
				break
		}
		response.setStatus(status);
		tmp.rmTemps()
	}

	def logOut(s) {
		Server.getInstance().logOut(s)
	}
	
}
