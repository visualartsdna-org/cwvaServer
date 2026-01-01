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
import java.nio.charset.StandardCharsets
import org.apache.jena.query.*
import util.Tmp

class Servlet extends ServletBase {


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

			case "/model/":
			case "/model":
			//case "/2025/06/22/model":
				sendTextFile(response,"$model/cwva.ttl")
				break

			case "/2021/07/16/model":
				sendTextFile(response,"$model/../archive/cwva-20210716.ttl")
				break

			case "/data":
				sendModel(response, dbm().instances)
				break

			case "/rdfs":
				sendModel(response, dbm().rdfs)
				break

			case "/vocab":
				sendModel(response, dbm().vocab)
				break

			case ~/\/d3\..*/:
				def n = path.lastIndexOf(".")
				def kind="all"
				if (n>=0) kind = path.substring(n+1)
				tmpFile = Tmp.getTemp("d3f",".html")
				new D3Driver().doGet(data,kind,tmpFile)
				sendHtmlFile(response,tmpFile)
				Tmp.delTemp(tmpFile)
				break
				
			case "/modelviewer":
				if (!mq.containsKey("isMobile"))  // maybe redirected from other server
					mq["isMobile"] = ""+isMobile
 				def s = new ModelViewer().process(mq, cfg.host, dbm().rdfs)
				sendHtml(response,s)
				break
				
			case "/modelviewer.bkgnd":
				if (!mq.containsKey("isMobile"))  // maybe redirected from other server
					mq["isMobile"] = ""+isMobile
				def s = new ModelViewer().process(mq, cfg.host, dbm().rdfs)
				sendHtml(response,s)

				break

			case "/model.graph":
				tmpFile = Tmp.getTemp("dot",".html")
				new DotDriver().doGet(model,"svg",tmpFile)
				sendHtmlFile(response,tmpFile)
				Tmp.delTemp(tmpFile)
				break

			case "/vocab.graph":
				tmpFile = Tmp.getTemp("dot",".html")
				new DotDriver().doGet(vocab,"svgVocab",tmpFile)
				sendHtmlFile(response,tmpFile)
				Tmp.delTemp(tmpFile)
				break

			case "/dataModel.graph":
				tmpFile = Tmp.getTemp("dot",".html")
				new DotDriver().doGet(data,"svgImpOnto",tmpFile)
				sendHtmlFile(response,tmpFile)
				Tmp.delTemp(tmpFile)
				break

			case ~/\/lsys\..*/:
				def n = path.lastIndexOf(".")
				def kind="all"
				if (n>=0) kind = path.substring(n+1)
				tmpFile = Tmp.getTemp("lsys",".jpg")
				new LsysDriver().doGet(data,kind,tmpFile)
				sendJpegFile(response,tmpFile)
				Tmp.delTemp(tmpFile)
				break

			case "/vocabTree":
				if (!mq.containsKey("isMobile"))  // maybe redirected from other server
					mq["isMobile"] = ""+isMobile
 				def s = new VocabTree().process(mq, cfg.host, dbm().vocab)
				sendHtml(response,s)
				break
				
			case "/agentClient":
				if (!mq.containsKey("isMobile"))  // maybe redirected from other server
					mq["isMobile"] = ""+isMobile
 				def s = new AgentClient().process(mq, cfg.agentUrl, cfg.host)
				sendHtml(response,s)
				break
				
			case ~/\/dist.*/:
				sendJSFile(response,"$dir${path}")
				break

			case ~/\/html.*/:
				sendHtmlFile(response,"$dir/${path}")
				break

			case ~/\/model\/.*/:
				def jl2h = new JsonLd2Html()
				def relPath = jl2h.parsePath(path)
				def guid = jl2h.parseClass(path)

				if (query) {
					def fmt = (query =~ /^format=([a-zA-Z0-9-\/]+)[&]?.*$/)[0][1]
					def qs = new QuerySupport(dbm().rdfs)
					def m = qs.getOneInstanceModel("vad",guid)

					if (m.size()==0) status = HttpServletResponse.SC_NOT_FOUND
					else sendModel(response, m, fmt)
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
					def fmt = (query =~ /^format=([a-zA-Z0-9-\/]+)[&]?.*$/)[0][1]
					def qs = new QuerySupport(dbm().rdfs)
					def m = qs.getOneInstanceModel("work",guid)

					if (m.size()==0) status = HttpServletResponse.SC_NOT_FOUND
					else sendModel(response, m, fmt)
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
					def fmt = (query =~ /^format=([a-zA-Z0-9-\/]+)[&]?.*$/)[0][1]
					def qs = new QuerySupport(dbm().rdfs)
					def m = qs.getOneInstanceModel("the",guid)
					
					if (m.size()==0) status = HttpServletResponse.SC_NOT_FOUND
					else sendModel(response, m, fmt)
				} else {
					def s = jl2h.process(dbm().rdfs,domain,relPath,"the",guid,cfg.host)
					sendHtml(response,s)
				}
				break

			case "/":
				if (query)
					query += "&isMobile=$isMobile"
				else query = "isMobile=$isMobile"
				mq=parse(query)
				mq.order="Date"
				mq.artist="all"
				mq.offset="0"
				mq.limit="20"
				mq.page="1"
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

			case "/browseFilter":
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

			case "/about":
				def s = new About(cfg).get()
				sendHtml(response,s)
				break

			case "/otherStuff":
				def s = new OtherStuff().get(cfg.host)
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
	}	

}
