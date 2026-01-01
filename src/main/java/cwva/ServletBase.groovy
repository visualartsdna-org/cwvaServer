package cwva

import jakarta.servlet.ServletException;
import util.Token
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import rdf.JenaUtilities
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import util.FileUtil
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.InputStreamEntity
import org.apache.jena.query.*
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

class ServletBase extends HttpServlet {

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

	def serve(cfg,path,query,request,response) {
		def dir = cfg.dir
		def model = cfg.model
		def vocab = cfg.vocab
		def data = cfg.data
		def tags = cfg.tags
		def domain = cfg.domain
		def images = cfg.images
		def state
		
		switch(path) {
			
			case "/schema": // default is json-ld
				def mq = parse(query)
				String acceptHeader = request.getHeader("Accept")
				if (mq.format){
					switch(mq.format.toLowerCase()) {
						case "jsonld":
						sendModelAsJsonLD(response, dbm().schema)
						break
						
						case "n-triples":
						sendModel(response, dbm().schema, "n-triples")
						break
						
						case "n3":
						sendModel(response, dbm().schema, "n3")
						break
						
						case "xml":
						sendModel(response, dbm().schema, "rdf/xml")
						break
						
						case "ttl":
						sendModel(response, dbm().schema, "ttl")
						break
					}
				
				} else if (acceptHeader) {
					switch (acceptHeader){
						case "text/turtle":
						sendModel(response, dbm().schema, "ttl")
						break
						
						case "application/rdf+xml":
						sendModel(response, dbm().schema, "rdf/xml")
						break
						
						case "application/n-triples":
						sendModel(response, dbm().schema, "n-triples")
						break
						
						case "text/n3":
						sendModel(response, dbm().schema, "n3")
						break

						case "application/ld+json":
						sendModelAsJsonLD(response, dbm().schema)
						break
						}
				}
				else sendModel(response, dbm().schema, "ttl") // default
			break

			case ~/\/sparqlEndpoint/:
				def q = ""
				if (request && request.getMethod() == "POST") {
					StringBuilder sb = new StringBuilder()
					java.io.BufferedReader reader = request.getReader()
					String line;
					while ((line = reader.readLine()) != null) {
						sb.append(line)
					}
					q = ""+sb
				} else { // GET
					def mq=parse(query)
					q = mq.query
				}
				if (q.startsWith("query="))
					q = q.substring("query=".size())
			
				try {
					// Using a specific Charset (Recommended for Java 10+)
					q = URLDecoder.decode(q, java.nio.charset.StandardCharsets.UTF_8);
		
				} catch (UnsupportedEncodingException e) {
					// Handle the exception if the named encoding is not supported
					throw new RuntimeException(e)
				}
		
				//println "$q\n"
				
				ResultSet resultSet = ju.queryResultSet(cwva.Server.getInstance().dbm.rdfs,
					rdf.Prefixes.forQuery,q)
				ByteArrayOutputStream baos = new ByteArrayOutputStream()
					ResultSetFormatter.outputAsJSON(baos,resultSet) // ok
				def json = new String( baos.toByteArray())
				
				sendJson(response, json)
				break

			case ~/\/agent\/query/:
			
				def httpClient = HttpClients.createDefault()
		        try {
					def url = "${cfg.agentUrl}/query"
					
		            HttpPost post = new HttpPost(url)
		
		            // 1. Copy headers from original request to the new request
		            // Note: You might want to filter some headers (e.g., Host, Connection)
		            def headerNames = request.getHeaderNames()
		            while (headerNames.hasMoreElements()) {
		                def headerName = headerNames.nextElement()
		                def headerValue = request.getHeader(headerName)
		                // Exclude headers that should be managed by the client or target server
		                if (!['Host', 'Connection', 'Content-Length'].contains(headerName)) {
		                    post.addHeader(headerName, headerValue)
		                }
		            }
					
					// TEST for logging IP/query
					byte[] bytes = request.getInputStream().readAllBytes()
					InputStream is1 = new ByteArrayInputStream(bytes)
					InputStream is2 = new ByteArrayInputStream(bytes)
					
					def IP = getIP(request)
					def content = is1.getText()
					println "toAgent\t$IP=$content"
					
					// Check rate limit
					def meterResult = checkAndIncrement(IP)
					
					if (!meterResult.allowed) {
						println "Rate limited: ${IP} - ${content?.take(50)}"
						response.status = 429
						response.setHeader('Retry-After', meterResult.retryAfter.toString())
						response.contentType = 'application/json'
						response.writer.write(JsonOutput.toJson([
							error: 'Rate limit exceeded. Please wait before trying again.',
							retry_after: meterResult.retryAfter
						]))
						return
					}
				
					
		            // 2. Copy the request body (input stream)
		            // Use InputStreamEntity to efficiently stream the body
		            post.setEntity(new InputStreamEntity(is2, request.getContentLength()))
		
		            // 3. Execute the request to the target server
		            def targetResponse = httpClient.execute(post)
		
		            try {
		                // 4. Copy status code and headers back to the original client's response
		                response.setStatus(targetResponse.getStatusLine().getStatusCode())
		
		                def targetHeaders = targetResponse.getAllHeaders()
		                targetHeaders.each { header ->
		                    response.setHeader(header.getName(), header.getValue())
		                }
		
		                // 5. Copy the response body back to the original client's output stream
		                def entity = targetResponse.getEntity()
		                if (entity != null) {
		                    entity.writeTo(response.getOutputStream())
		                    EntityUtils.consume(entity) // Ensure the entity is fully consumed
		                }
		            } finally {
		                targetResponse.close()
		            }
		        } catch (Exception e) {
					throw new RuntimeException("Error: agent qery passthrough, ${HttpServletResponse.SC_INTERNAL_SERVER_ERROR}, ${e.getMessage()}")
		        } finally {
		            httpClient.close()
		        }
			
				break

			case ~/\/images.*\.glb/:
				def f = FileUtil.loadImage(images,path)
				sendGlbFile(response, f)
				break

			case ~/\/images.*\.(jpg|JPG)/:
				def f = FileUtil.loadImage(images,path)
				sendJpegFile(response, f)
				break

			case ~/\/images.*\.png/:
				def f = FileUtil.loadImage(images,path)
				sendPngFile(response, f)
				break

			case ~/\/images.*\.gif/:
				def f = FileUtil.loadImage(images,path)
				sendGifFile(response, f)
				break

			case ~/\/images.*\.webp/:
				def f = FileUtil.loadImage(images,path)
				sendWebPFile(response, f)
				break

			case ~/\/images.*\.ico/:
				def f = FileUtil.loadImage(images,path)
				sendIconFile(response, f)
				break

			case ~/\/images.*\.usdz/:
				def f = FileUtil.loadImage(images,path)
				sendUsdzFile(response, f)
				break

//			case ~/\/images.*/:
//				def f = FileUtil.loadImage(images,path)
//				sendJpegFile(response,f)
//				break

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
							s +=  "db\n"
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
				//logOut(payload)
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

			case "/favicon.png":
				sendIconFile(response,"$images/favicon.png")
				break

			default:
				state = "unknownPath"
				logOut "unknown path $path, ${query?:""}"
				//throw new RuntimeException("unrecognized command $path, ${query?:""}")
				break
		}
		setState(request,state)
		response.setStatus(HttpServletResponse.SC_OK);
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
	def sendImageFile(response, String file) {
		sendImageFile(response, new File(file))
	}
	def sendImageFile(response, File file) {
		def payload = file.readBytes()
		def os = response.getOutputStream()
		os.write(payload)
	}
	def sendJpegFile(response, file) {
		response.setContentType("image/jpeg");
		sendImageFile(response, file)
	}
	def sendPngFile(response, file) {
		response.setContentType("image/png");
		sendImageFile(response, file)
	}
	def sendWebPFile(response, file) {
		response.setContentType("image/webp");
		sendImageFile(response, file)
	}
	def sendGlbFile(response, file) {
		response.setContentType("model/gltf-binary");
		sendImageFile(response, file)
	}
	def sendGifFile(response, file) {
		response.setContentType("image/gif");
		sendImageFile(response, file)
	}
	def sendIconFile(response, file) {
		response.setContentType("image/x-icon")
		sendImageFile(response, new File(file))  // why does this take a File?
	}
	def sendUsdzFile(response, file) {
		response.setContentType("image/usdz")
		sendImageFile(response, file)
	}
	def sendModelFile(response, fileSpec) {
		def m = ju.loadFiles(fileSpec)
		sendModel(response, m, "text/turtle")
	}
	def sendModelAsJsonLD(response, model) {
		sendModel(response, model, "jsonld")
	}
	def sendModel(response, model) {
		sendModel(response, model, "text/turtle")
	}
	// supported types: ttl rdf/xml jsonld json-ld nt nq trig trix rt trdf
	// TODO: add: rdfa microdata
	def sendModel(response, m, type) {
		def s = ju.saveModelString(m, type)
		def mimetype = "text/turtle"
		switch(type.toLowerCase()) {
			case "rdf/xml":
			case "rdf/xml-abbrev":
			mimetype = "application/rdf+xml"
			break
			
			case "n-triples":
			mimetype = "application/n-triples"
			break
			
			case "n3":
			mimetype = "text/n3"
			break
			
			case "jsonld":
			case "json-ld":
			mimetype = "application/json"
			break
			
			case "ttl":
			default:
				mimetype = "text/turtle"
			break
		}
		response.setContentType(mimetype);
		response.getWriter().println("$s")
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

	def getMetrics() {
		def s=""
		metrics.sort().each{k,v->
			s += "$v\t$k\n"
		}
		s
	}
	
	def setState(request) {
		setState(request,null)
	}
	
	def setState(request, facet) {
		def path = request._uri._path
		def ip = getIP(request)
		
		if (path == "/favicon.ico" 
			|| path == "/favicon.png") 
			return
		if (path.contains("/images/"))
			path = "/images"
		if (facet == "unknownPath")
			path = facet
		
		def date = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
		if (!metrics[date]) {
			metrics[date]= new TreeMap()
			metrics[date].count=0
		}
		metrics[date].count++
		
		if (!metrics[date][ip]) {
			metrics[date][ip]= new TreeMap()
			metrics[date][ip].count=0
		}
		metrics[date][ip].count++
		
		if (facet && facet != "unknownPath") {
			if (!metrics[date][ip][facet])
				metrics[date][ip][facet]=0
			metrics[date][ip][facet]++
		}
		
		if (!metrics[date][ip][path]) {
			metrics[date][ip][path] = new TreeMap()
			metrics[date][ip][path].count = 0
		}
println "date:$date, ip:$ip, path:$path, metrics:${metrics[date][ip][path]}"
		metrics[date][ip][path].count++; 
	}
	
//	def getIP(request) {
//		String ipAddress = request.getHeader("X-FORWARDED-FOR");
//		if (ipAddress == null) {
//			ipAddress = request.getRemoteAddr();
//		}
//		ipAddress
//	}
	
	/**
	 * Get client IP from request, handling proxies
	 */
	def getIP(HttpServletRequest request) {
		String forwarded = request.getHeader('X-Forwarded-For')
		if (forwarded) {
			// Take first IP if multiple (client, proxy1, proxy2...)
			return forwarded.split(',')[0].trim()
		}
		return request.remoteAddr
	}
	
	private static final int MAX_QUERIES = 10          // Max queries per window
	private static final long QUERY_WINDOW_MS = 600000  // Window in milliseconds (10 minutes)

	// Track query counts and window start times per IP
	private static final ConcurrentHashMap<String, QueryTracker> trackers = new ConcurrentHashMap<>()

	static class QueryTracker {
		int count = 0
		long windowStart = System.currentTimeMillis()
	}

	/**
	 * Check if a query is allowed for the given IP.
	 * Returns [allowed: boolean, retryAfter: seconds or null]
	 */
	static Map checkAndIncrement(String clientIP) {
		long now = System.currentTimeMillis()

		QueryTracker tracker = trackers.compute(clientIP) { ip, existing ->
			if (existing == null) {
				// New IP - create tracker
				return new QueryTracker(count: 1, windowStart: now)
			}

			// Check if window has expired
			if (now - existing.windowStart >= QUERY_WINDOW_MS) {
				// Reset window
				existing.count = 1
				existing.windowStart = now
			} else {
				// Same window - increment
				existing.count++
			}
			return existing
		}

		if (tracker.count > MAX_QUERIES) {
			// Calculate seconds until window resets
			long msRemaining = QUERY_WINDOW_MS - (now - tracker.windowStart)
			int retryAfter = Math.max(1, (int) Math.ceil(msRemaining / 1000.0))
			return [allowed: false, retryAfter: retryAfter]
		}

		return [allowed: true, retryAfter: null]
	}

	/**
	 * Optional: Clean up old entries periodically
	 */
	static void cleanup() {
		long now = System.currentTimeMillis()
		trackers.entrySet().removeIf { entry ->
			now - entry.value.windowStart > QUERY_WINDOW_MS * 2
		}
	}

}
