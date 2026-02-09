package support

import rdf.JenaUtilities
import groovy.io.FileType
import org.jsoup.Jsoup

class UpdateWorkOnSite {
	
	def ju = new JenaUtilities()
	
	def process(mq) {
		def logSb = new StringBuilder()
		
		for (site in ["rspates.art","rickspates.art"]) {
			def text = scrapeSite(site)
			extract (site,text, logSb)
		}
		logSb
	}
	
	def scrapeSite(site) {
		def doc = Jsoup.connect("https://$site").userAgent("Mozilla/5.0").get()
		
		// 1. Manually append a placeholder before block elements
		doc.select("p, h1, h2, h3, h4, h5, h6, div, li").prepend("____NEWLINE____")
		doc.select("br").append("____NEWLINE____")
		
		// 2. Extract text and replace the placeholder with a real Java newline (\n)
		def finalResult = doc.body().text().replace("____NEWLINE____", "\n")
	}
	
	def extract(site, text, logSb) {
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
				case "See the complete work in 3D":
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
			logSb.append """$it
"""
			def lm = ju.queryListMap1(m,rdf.Prefixes.forQuery, """
			select ?s {
				?s rdfs:label "$it"
			}
""")
			if (lm.size()) {
				def uri = lm[0].s.replaceAll("http://visualartsdna.org/work/","")
				logSb.append """work:${uri} vad:workOnSite <https://$site> .
"""
				sb.append """work:${uri} vad:workOnSite <https://$site> .
"""
			}
		}
		new File("/stage/data/workOnSite_${site}.ttl").text = ""+sb
	}
}
