package misc

import static org.junit.jupiter.api.Assertions.*

import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import util.Tmp
import groovy.io.FileType
import org.jsoup.Jsoup
class TestJunk3 {

	def ju = new JenaUtilities()
	
	@Test
	void testLoad() {
		def m = ju.loadFiles("/stage/data")
		
	}
	
	@Test
	void testSelectFiles() {
		def src= "/stage/server/cwvaContent/ttl/data"
		def tgt= "/stage/data"
		
		new File(src).eachFileRecurse(FileType.FILES){file->
			if (file.text.contains ("vad:workOnSite")) {
				def dest = new File("$tgt/${file.name}")
				dest << file.text
			}
		}
	}
	
	@Test
	void testWebText() {
		
		for (site in ["rspates.art","rickspates.art"]) {
			def text = scrapeSite(site)
			extract (site,text)
		}
	}
	
	def scrapeSite(site) {
		def doc = Jsoup.connect("https://$site").userAgent("Mozilla/5.0").get()
		
		// 1. Manually append a placeholder before block elements
		doc.select("p, h1, h2, h3, h4, h5, h6, div, li").prepend("____NEWLINE____")
		doc.select("br").append("____NEWLINE____")
		
		// 2. Extract text and replace the placeholder with a real Java newline (\n)
		def finalResult = doc.body().text().replace("____NEWLINE____", "\n")
	}
	
	def extract(site, text) {
		def l = []
		def save = false
		def c=0
		
		text.eachLine{
			def s = it.trim()
			switch (s) {
				case "":
				case "Linked data for the work.":
				case "Linked data for this work.":
				case "Linked data for this work. ":
				case "View in 3D":
				case "View in 3D.":
				case "Fun Graphics":
				case "Details":
				case "Ontologies Represented in Lindenmeyer-System Graphics":
				break
				
				case "Watercolors":
				case "rspates":
				save = true
				break
				
				case "Sketches and Studies":
				case "rspates.art@gmail.com Copyright © 2025 rspatesThese works are licensed under Creative Commons Attribution-NoDerivatives 4.0 International (CC BY-ND).Linked Data Defined":
				save = false
				break
				
				default:
				if (save) {
					l += s
					c++
				}
				break
			}
		}
		
		def sb = new StringBuilder()
		sb.append rdf.Prefixes.forFile
		sb.append "\n"
		def m = ju.loadFiles("/stage/server/cwvaContent/ttl/data")
		l.each{
			println it
			def lm = ju.queryListMap1(m,rdf.Prefixes.forQuery, """
			select ?s {
				?s rdfs:label "$it"
			}
""")
			if (lm.size()) {
				def uri = lm[0].s.replaceAll("http://visualartsdna.org/work/","")
				println """work:${uri} vad:workOnSite <https://$site> ."""
				sb.append """work:${uri} vad:workOnSite <https://$site> .
"""
			}
		}
		new File("/stage/data/workOnSite_${site}.ttl").text = ""+sb
	}

	def extract0(text) {
		def l = []
		def save = false
		def c=0
		
		text.eachLine{
			def s = it.trim()
			switch (s) {
				case "":
				case "Linked data for the work.":
				case "Linked data for this work.":
				case "Linked data for this work. ":
				break
				
				case "Watercolors":
				save = true
				break
				
				case "Sketches and Studies":
				save = false
				break
				
				default:
				if (save) {
					l += s
					c++
				}
				break
			}
		}
		
		l.each{
			println it
		}
		println "count $c"
		
	}


	@Test
	void testWebText3() {
		def doc = Jsoup.connect("https://rickspates.art").userAgent("Mozilla/5.0").get()
		
		// 1. Manually append a placeholder before block elements
		doc.select("p, h1, h2, h3, h4, h5, h6, div, li").prepend("____NEWLINE____")
		doc.select("br").append("____NEWLINE____")
		
		// 2. Extract text and replace the placeholder with a real Java newline (\n)
		def finalResult = doc.body().text().replace("____NEWLINE____", "\n")
		
		println finalResult
	}
	
	@Test
	void testWebText2() {
		def url = "https://rickspates.art"
		
		// 1. Connect and get the document
		def doc = Jsoup.connect(url)
		               .userAgent("Mozilla/5.0") 
		               .get()
		
		// 2. Select block-level elements and inject a newline before them
		// This mimics the visual break you get when copying from a browser
		doc.select("p, div, li, br, h1, h2, h3, h4, h5, h6").each { element ->
		    element.before("\n")
		}
		
		// 3. Extract the text
		// doc.body().text() will now respect the newly injected breaks
		def formattedText = doc.body().text()
		
		println formattedText	
	}
	
	@Test
	void testWebText1() {
		def doc = Jsoup.connect("https://rickspates.art")
		.userAgent("Mozilla/5.0") // Fixes the "empty" issue
		.get()

		println doc.body().text()
	}
	
	@Test
	void testWebText0() {
		def url = "https://rickspates.art".toURL()
		def connection = url.openConnection()
		
		// Mimic a real web browser
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
		
		def html = connection.getInputStream().getText()
		println html
	}
	
	@Test
	void testWebsite() {
		
		def site = "rickspates.art"
		def l = []
		def save = false
		def c=0
		
		new File("/stage/tmp/workOnSite.txt").eachLine{
			if (it == "The Egg Hunt") {
				println "here"
			}
			switch (it) {
				case "":	
				case "RS Art":	
				case "HomeSketches":
				case "Rick Spates Art":
				case "Twitter":
				case "Linked data for the work.":
				case "Linked data for this work.":
				case "Linked data for this work. ":
				case "Sketches":
				case "Watercolors and pencil drawings.":
				case "rickspatesart@gmail.com":
				case "Rick Spates gallery on ArtPal":
				case "Twitter":
				case "LinkedIn Profile":
				case "Linked Data Defined":
				break
				
				case "Watercolors":
				save = true
				break
				
				case "Sketches and Studies":
				save = false
				break
				
				default:
				if (save) {
					l += it
					c++
				}
				break
			}
		}
		
//		l.each{
//			println it
//		}
		println "# count $c"
		
		def m = ju.loadFiles("/stage/server/cwvaContent/ttl/data")
		l.each{
			def lm = ju.queryListMap1(m,rdf.Prefixes.forQuery, """
			select ?s {
				?s rdfs:label "$it"
			}
""")
			println """<${lm[0].s}> vad:workOnSite $site ;"""
		}
	}
	@Test
	void testWebsite0() {
		def l = []
		def save = false
		def c=0
		
		new File("/stage/tmp/workOnSite.txt").eachLine{
			if (it == "The Egg Hunt") {
				println "here"
			}
			switch (it) {
				case "":
				case "RS Art":
				case "HomeSketches":
				case "Rick Spates Art":
				case "Twitter":
				case "Linked data for the work.":
				case "Linked data for this work.":
				case "Linked data for this work. ":
				case "Sketches":
				case "Watercolors and pencil drawings.":
				case "rickspatesart@gmail.com":
				case "Rick Spates gallery on ArtPal":
				case "Twitter":
				case "LinkedIn Profile":
				case "Linked Data Defined":
				break
				
				case "Watercolors":
				save = true
				break
				
				case "Sketches and Studies":
				save = false
				break
				
				default:
				if (save) {
					l += it
					c++
				}
				break
			}
		}
		
//		l.each{
//			println it
//		}
		println "count $c"
		
		def m = ju.loadFiles("/stage/server/cwvaContent/ttl/data")
		l.each{
			def lm = ju.queryListMap1(m,rdf.Prefixes.forQuery, """
			select ?s {
				?s rdfs:label "$it"
			}
""")
			println lm
			println it
		}
	}

	
	@Test
	void testDate() {
		def formatPattern = "MM/dd/yyyy, HH:mm:ss a"
		def ts = "10/6/2025, 8:09:54 PM"
		def newDate = new SimpleDateFormat(formatPattern).parse(ts)
		println newDate
	}

	
	@Test
	void testDelete2() {
		
		def svg = Tmp.getTemp(".svg")
		def dot = Tmp.getTemp(".dot")
		
		println svg
		println dot
		
		Tmp.delTemp(svg)
		Tmp.delTemp(dot)
		
		println Tmp.temps
	}

	@Test
	void testHost() {
		println containsHost("r=http://192.168.1.71/thesaurus/visualArtsProcess")
	}
	def ip="192.168.1.119"
	def host="http://192.168.1.71:80"
	def containsHost(s) {
		def h0 = new URL(host)
		s.contains(h0.getHost()) || s.contains(ip)
		
	}
		

	def worksOnSite = [
]


@Test
void testWorksOnSite() {
	//worksOnSite
		def list = ""
		int i=0
		worksOnSite.each{
			if (i++) list += ",\n"
			list += """"$it" """
		}
		def query = """
select * {
	?s a vad:WatercolorWork ;
		rdfs:label ?label ;
		vad:workOnSite ?site ;
.
		filter(?label not in( $list))
}
"""
	new File("/stage/server/cwvaContent/ttl/data").eachFileRecurse(FileType.FILES){file->
		println file.name
		def path = file.path
		def m = ju.loadFiles(path)
//		def lm = ju.queryListMap1(m,rdf.Prefixes.forQuery,query)
//		lm.each{
//			println it
//		}
		def lm = ju.queryListMap1(m,rdf.Prefixes.forQuery,"""
select ?s {
 ?s 	a vad:WatercolorWork ;
		vad:workOnSite ?site 
}

""")
		
		ju.queryExecUpdate(m,rdf.Prefixes.forQuery,"""
delete { ?s vad:workOnSite ?site }
where {
	?s a vad:WatercolorWork ;
		rdfs:label ?label ;
		vad:workOnSite ?site ;
.
		filter(?label in( $list))
}
""")
		if (lm.size())
			ju.saveModelFile(m,"/stage/data/${file.name}","ttl")
	}
	}
}
