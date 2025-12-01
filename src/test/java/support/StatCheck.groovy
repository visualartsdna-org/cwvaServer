package support

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.*

class StatCheck {

	static def host = "192.168.1.71:80"

	// from queries on the instance data
	@Test
	void testAllUris() {
		
		def paths = []
		def ju = new JenaUtilities()
		def dir = "/stage/server/cwvaContent/ttl"
//		new File(dir).eachFileRecurse { file ->
			println "Processing: ${dir}"
			def m = ju.loadFiles(dir)
			def lm = ju.queryListMap1(m,Prefixes.forQuery,"""

select distinct ?s{
?s ?p ?o
filter(!isBlank(?s))
} order by ?s
""")
			lm.each{m0->
				if (!m0.s) {
					println "here"
				}
				paths += m0.s
			}
			paths = paths.toSet().sort()
			genReport2(paths)
//		}
		
	}
	
	static def rehost(r) {
		r.replaceAll("http://visualartsdna.org","http://$host")
	}


	// from queries on the stats ttl
	@Test
	void test() {
		
		def paths = []
		def ju = new JenaUtilities()
		def dir = "/work/stats/ttl"
		new File(dir).eachFileRecurse { file ->
			println "Processing: ${file.absolutePath}"
			def m = ju.loadFiles(""+file)
			def lm = ju.queryListMap1(m,Prefixes.forQuery,"""
prefix st:    <http://example.com/> 
prefix xs:    <http://www.w3.org/2001/XMLSchema#>

select ?l{
?s st:link ?l
}
""")
			lm.each{m0->
				paths += m0.l
			}
			paths = paths.toSet().sort()
			genReport(paths)
		}
		
	}

	// from queries on the stats ttl
	@Test
	void testCollectUniquePaths() {
		
		def allPaths = []
		def ju = new JenaUtilities()
		def dir = "/work/stats/ttl"
		new File(dir).eachFileRecurse { file ->
			def paths = []
			println "Processing: ${file.absolutePath}"
			def m = ju.loadFiles(""+file)
			def lm = ju.queryListMap1(m,Prefixes.forQuery,"""
prefix st:    <http://example.com/> 
prefix xs:    <http://www.w3.org/2001/XMLSchema#>

select ?l{
?s st:link ?l
}
""")
			lm.each{m0->
				paths += m0.l
			}
			allPaths += paths.toSet().sort()
		}
		allPaths = allPaths.toSet().sort()
		new File("/stage/tmp/allPaths.json")
		.text = new JsonOutput().toJson(allPaths)
	}

	// from a json file list of paths
	@Test
	void test0() {
		def jsonListFile = "/stage/tmp/allPaths.json"

		List<String> paths = loadPaths(jsonListFile)
		if (!paths) {
			System.err.println("No paths found in ${jsonListFile}")
			return
		}
		genReport(paths)
	}

	def genReport2(paths) {
		// Header row
		final int codeW = 5
		final int lenW  = 10
		final int typeW = 30

		println(String.format("%-${codeW}s %-${lenW}s %-${typeW}s %s",
				"CODE", "LENGTH", "TYPE", "PATH"))
		println("-" * codeW + " " + "-" * lenW + " " + "-" * typeW + " " + "-" * 40)

		paths.each { path ->
			//String path = normalizePath(p)
			//Map result = fetchHeadOrGet(host, path)
			Map result = fetchGet2(host, path)

			String code   = result.code?.toString() ?: "ERR"
			String length = result.length?.toString() ?: "-"
			String type   = result.type ?: "-"

			println(String.format("%-${codeW}s %-${lenW}s %-${typeW}s %s",
					code, length, type, path))
		}
	}

	def genReport(paths) {
		// Header row
		final int codeW = 5
		final int lenW  = 10
		final int typeW = 30

		println(String.format("%-${codeW}s %-${lenW}s %-${typeW}s %s",
				"CODE", "LENGTH", "TYPE", "PATH"))
		println("-" * codeW + " " + "-" * lenW + " " + "-" * typeW + " " + "-" * 40)

		paths.each { p ->
			String path = normalizePath(p)
			//Map result = fetchHeadOrGet(host, path)
			Map result = fetchGet(host, path)

			String code   = result.code?.toString() ?: "ERR"
			String length = result.length?.toString() ?: "-"
			String type   = result.type ?: "-"

			println(String.format("%-${codeW}s %-${lenW}s %-${typeW}s %s",
					code, length, type, path))
		}
	}

	private static List<String> loadPaths(String filePath) {
		def f = new File(filePath)
		if (!f.exists()) {
			System.err.println("File not found: ${f.absolutePath}")
			return []
		}
		def data = new JsonSlurper().parse(f)
		if (data instanceof List) {
			return data.collect { it?.toString() }
		}
		System.err.println("Expected a JSON array of strings.")
		return []
	}

	private static String normalizePath(String raw) {
		if (!raw) return "/"
		String p = raw.trim()
		if (!p.startsWith("/")) p = "/" + p
		return p.replaceFirst("^/+", "/")
	}

	private static Map fetchHeadOrGet(String host, String path) {
		String url = "http://${host}${path}"
		try {
			def conn = (HttpURLConnection) new URL(url).openConnection()
			conn.setInstanceFollowRedirects(false)
			conn.setConnectTimeout(8000)
			conn.setReadTimeout(8000)

			conn.setRequestMethod("HEAD")
			int code = conn.responseCode
			Map<String, List<String>> headers = conn.headerFields
			conn.disconnect()

			if (code == HttpURLConnection.HTTP_BAD_METHOD) {
				return fetchGet(host, path)
			}
			return prepareHeaderResult(code, headers, url)
		} catch (IOException e) {
			try {
				return fetchGet(host, path)
			} catch (IOException ioe) {
				return [code: "ERR", length: null, type: null, url: url]
			}
		}
	}

	private static Map fetchGet2(String host, String path) throws IOException {
		String url = "${rehost(path)}"
		def conn = (HttpURLConnection) new URL(url).openConnection()
		conn.setInstanceFollowRedirects(false)
		conn.setConnectTimeout(8000)
		conn.setReadTimeout(8000)
		conn.setRequestMethod("GET")

		int code = conn.responseCode
		Map<String, List<String>> headers = conn.headerFields

		try {
			(code >= 400 ? conn.getErrorStream() : conn.getInputStream())?.close()
		} catch (Exception ignore) { }
		conn.disconnect()

		return prepareHeaderResult(code, headers, url)
	}

	private static Map fetchGet(String host, String path) throws IOException {
		String url = "http://${host}${path}"
		def conn = (HttpURLConnection) new URL(url).openConnection()
		conn.setInstanceFollowRedirects(false)
		conn.setConnectTimeout(8000)
		conn.setReadTimeout(8000)
		conn.setRequestMethod("GET")

		int code = conn.responseCode
		Map<String, List<String>> headers = conn.headerFields

		try {
			(code >= 400 ? conn.getErrorStream() : conn.getInputStream())?.close()
		} catch (Exception ignore) { }
		conn.disconnect()

		return prepareHeaderResult(code, headers, url)
	}

	private static Map prepareHeaderResult(int code,
			Map<String, List<String>> headers,
			String url) {

		String length = headers["Content-Length"]?.getAt(0)
		String type   = headers["Content-Type"]?.getAt(0)

		return [
			code  : code,
			length: length,
			type  : type,
			url   : url
		]
	}
}

// Example:
// StatCheck.test("192.168.1.71:8080", "/mnt/data/statUri.json")


