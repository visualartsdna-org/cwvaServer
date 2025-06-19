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
import java.nio.charset.StandardCharsets

class Servlet extends ServletBase {


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		try {
			handler(request._uri._path,request._uri._query,response,request)
		} catch (Exception e) {
			logOut e.printStackTrace()
			throw new RuntimeException("Something went wrong")
		}
	}
	
	def handler(path,query,response,request) {
		def state = 1
		def userAgent = request.getHeader("User-Agent")
		def isMobile = userAgent ==~ /.*(Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini).*/

		def mq=parse(query)
		def tmpFile
		if (cfg.verbose) println "$path ${query?:""}"
		//dbm.reload()	// optional: useful for dev
		
		if (policyAccept("main",path))
		switch (path) {

			case "/model":
			case "/2025/04/26/model":
				sendTextFile(response,"$model/cwva.ttl")
				break

			case "/2021/07/16/model":
				sendTextFile(response,"$model/../archive/cwva-20210716.ttl")
				break

			case "/data":
				sendModel(response, dbm().instances)
				break

			case "/vocab":
				sendModel(response, dbm().vocab)
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

//			case ~/\/3d.*\.glb/:
//				sendGlbFile(response, "C:/temp/git/cwva${path}")
//				break
//
//			case ~/\/3d.*\.png/:
//				sendPngFile(response, "C:/temp/git/cwva${path}")
//				break
//
//			case ~/\/3d.*\.ico/:
//				sendIconFile(response, "C:/temp/git/cwva${path}")
//				break
//
//			case ~/\/3d.*\.js/:
//			case ~/\/3d.*\.css/:
//				sendTextFile(response,"C:/temp/git/cwva${path}")
//				break
//
//			case ~/\/3d.*/:
//				sendHtmlFile(response,"C:/temp/git/cwva${path}")
//				break
//
			case ~/\/html.*/:
				sendHtmlFile(response,"$dir/${path}")
				break

			case ~/\/2025\/04\/26\/model\/.*/:
				def jl2h = new JsonLd2Html()
				def relPath = jl2h.parsePath(path)
				def guid = jl2h.parseClass(path)

				if (query) {
					def fmt = (query =~ /^format=([a-zA-Z-\/]+)[&]?.*$/)[0][1]
					def qs = new QuerySupport(dbm().rdfs)
					def m = qs.getOneInstanceModel("vad",guid)

					if (m.size()==0) status = HttpServletResponse.SC_NOT_FOUND
					else sendModel(response, m, fmt.toLowerCase())
				} else {
					def s = jl2h.process(dbm().rdfs,domain,relPath,"vad",guid,cfg.host)
					sendHtml(response,s)
				}
				break
				
			// this occurs with model-viewer needing icons from a work context
			case ~/\/work\/images.*\.png/:
				def p2 = path.replaceAll("/work/images","${cfg.images}")
//				sendPngFile(response, path.replaceAll("/work/images","${cfg.images}"))
				def f = util.FileUtil.loadImage(cfg.images,p2)
				sendPngFile(response, f)
			break

			case ~/\/work.*/:
				def jl2h = new JsonLd2Html()
				def relPath = jl2h.parsePath(path)
				def guid = jl2h.parseGuid(path)

				if (query) {
					def fmt = (query =~ /^format=([a-zA-Z-\/]+)[&]?.*$/)[0][1]
					def qs = new QuerySupport(dbm().rdfs)
					def m = qs.getOneInstanceModel("work",guid)

					if (m.size()==0) status = HttpServletResponse.SC_NOT_FOUND
					else sendModel(response, m, fmt.toLowerCase())
				} else {
					def s = jl2h.process(dbm().rdfs,domain,relPath,ns,guid,cfg.host)
					sendHtml(response,s)
				}
				break

			case ~/\/thesaurus.*/:
				def jl2h = new JsonLd2Html()
				def relPath = jl2h.parsePath(path)
				def guid = jl2h.parseConcept(path)

				if (query) {
					def fmt = (query =~ /^format=([a-zA-Z-\/]+)[&]?.*$/)[0][1]
					def qs = new QuerySupport(dbm().rdfs)
					def m = qs.getOneInstanceModel("work",guid)
					
					if (m.size()==0) status = HttpServletResponse.SC_NOT_FOUND
					else sendModel(response, m, fmt.toLowerCase())
				} else {
					def s = jl2h.process(dbm().rdfs,domain,relPath,"the",guid,cfg.host)
					sendHtml(response,s)
				}
				break

			case "/browse":
				if (query)
					query += "&isMobile=$isMobile"
				else query = "isMobile=$isMobile"
				mq=parse(query)
				mq.order="Date"
 				def s = new BrowseWorks().browse(cfg.host,dbm().rdfs, mq)
				sendHtml(response,s)
				break
				
			case "/browseSort":
				if (query)
					query += "&isMobile=$isMobile"
				else query = "isMobile=$isMobile"
				mq=parse(query)
				def s = new BrowseWorks().browse(cfg.host,dbm().rdfs, mq)
				sendHtml(response,s)
				break

			case "/browse2":
				sendHtmlFile(response,"$dir/browse.html")
				break

			case "/":
				def s = new IndexHtml(cfg).get()
				sendHtml(response,s)
				break

			case "/otherStuff":
				def s = new OtherStuff().get()
				sendHtml(response,s)
				break

			case "/copyright":
				sendHtmlFile(response,"$dir/html/copyright.html")
				break

			case "/AiInformationPage":
				sendHtmlFile(response,"$dir/html/AiInformationPage.html")
				break

			case "/aiInterpretation":
				def html = new AIInterpretation().handleQueryParams(mq)
				sendHtml(response,html)
				break
				
			case "/metricTables":
				def html = new OtherStuff().getMetrics()
				sendHtml(response,html)
				break

			case ~/\/sparql/:
//				def userAgent = request.getHeader("User-Agent")
//				def isMobile = userAgent ==~ /.*(Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini).*/
				if (query)
					query += "&isMobile=$isMobile"
				else query = "isMobile=$isMobile"
					
				def url = "${cfg.twinHost}$path"
				if (query) url = "$url?$query"
				def s = new URL(url).getText()
				
				def i = s.indexOf("<!-header->")
				def s2 = HtmlTemplate.head(cfg.host) + s.substring(i) + HtmlTemplate.tail
				sendHtml(response, "$s2")
				break

			default:
				serve(cfg,path,query,request,response)
				state = 0
				break
		}
		if (state) setState(request)
		response.setStatus(HttpServletResponse.SC_OK);
		tmp.rmTemps()
	}	

}
