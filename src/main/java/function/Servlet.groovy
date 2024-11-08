package function
import jakarta.servlet.ServletException;
import cwva.Server
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import cwva.ServletBase
import groovy.json.JsonBuilder
import java.text.SimpleDateFormat
import rdf.util.ViaToTtl
import support.*
import support.util.*
import util.Tmp

class Servlet extends ServletBase {

	def vm = new ConceptModel(vocab)
	def rm = new RelatedConcepts(vocab)
	def im = new Interpretation(data)
	def ib = new ImageBrand(data)
//	def tm = new TagModel(data,vocab,tags,cfg.host)
//	def sm = new StudiesModel(dbm().rdfs,cfg.studies)
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
			
		def mq=parse(query)
		def tmp
		setState(path)
		def lowLevelRequest = 
		(request && request.getMethod() == "POST") || (path =~ /\/artist\/.*\.jpg/)
		
		if (cfg.verbose && !lowLevelRequest) 
			println "$path ${query?:""}"
		else if (cfg.verboseAll && lowLevelRequest) 
			println "$path ${query?:""}"
			
		switch (path) {

			case "/certificate":
				def status = new CertAuthenticity().handleUpload(mq)
				sendHtmlFile(response,"/temp/html/ca.html")
				//sendText(response,"$status")
				break

			case "/registry":
				def status = new CollectionReport().handleUpload(mq)
				sendHtmlFile(response,"/temp/html/register.html")
				//sendText(response,"$status")
				break

			case "/fileScale":
				def status = new ImageGraphMgtTest().handleUpload(mq)
				sendText(response,"$status")
				break

			case "/fileConvert":
				def status = new ImageTypeMgt().handleUpload(mq)
				sendText(response,"$status")
				break

			case ~/\/distributeData/: // distribute data
				def s = new util.Exec().exec("C:/stage/bin/distributeData.bat")
				sendText(response,"$s")
				break

			case ~/\/distributeMetadata/:	// distribute metadata
				def s = new util.Exec().exec("C:/stage/bin/distributeMetadata.bat")
				sendText(response,"$s")
				break

			case ~/\/loadKeepData/:	// load g keep notes data
				new rdf.util.TakeoutTtl2Notes().testDriver()
				break

			case ~/\/related/:
				def s = rm.process()
				sendHtml(response, "$s")
				break

			case "/related.entry":
				def s = rm.handleQueryParams(mq)
				sendHtml(response, "$s")
				break

			case ~/\/html.*/:
				sendHtmlFile(response,"$dir/${path}")
				break

			case "/":
				sendHtmlFile(response,"$dir/html/function.html")
				break

			case "/vocab":
				def s = vm.initial("the:visualArtTerm")
				sendHtml(response, "$s")
				break

			case "/vocab.entry":
				def s = vm.handleQueryParams(mq)
				sendHtml(response, "$s")
				break

			case "/vocab.entry.text":
				def inst = URLDecoder.decode(m.instance, "UTF-8")
				mq.instance = inst
				def s = vm.handleQueryParamsText(mq)
				sendHtml(response, "$s")
				break

			case "/file/synch":
				def dir = URLDecoder.decode(query, "UTF-8")
				try {
					def text = new util.CompDirs().insynchReport(dbm().data, dir)
					sendText(response, text)
				} catch (java.io.FileNotFoundException fne) {
					sendText(response, "File not found")
				}
				break

			case "/file/compare":
				def dirs = URLDecoder.decode(query, "UTF-8")
				def d1 = (dirs=~/(.*)\&.*/)[0][1]
				def d2 = (dirs=~/.*\&(.*)/)[0][1]
				try {
					def text = new util.CompDirs().compareReport(d1,d2)
					sendText(response, text)
				} catch (java.io.FileNotFoundException fne) {
					sendText(response, "File not found")
				}
				break

			case "/imageBrand":
				def s = ib.printHtml()
				sendHtml(response,s)
				break

			case "/imageBrand.entry":
				def tpls = ib.handleQueryParams(mq)
				sendText(response,"$tpls")
				break

			case "/interpretation":
				def s = im.printHtml()
				sendHtml(response,s)
				break

			case "/interpretation.entry":
				def tpls = im.handleQueryParams(mq)
				sendText(response,"$tpls")
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

				def tpls = new ParseRDF().parse(mq)
				sendText(response,"$tpls")

				break

			case "/rdf_digital.entry":

				def tpls = new ParseDigitalRDF().parse(mq)
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
