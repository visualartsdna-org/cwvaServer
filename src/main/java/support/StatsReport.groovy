package support

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtils

class StatsReport {

	def ju = new JenaUtils()
	def reportMap=[:]
	def guid = new support.Guid()
	def Prefixes = """
${rdf.Prefixes.forQuery}
prefix st:    <http://example.com/>
"""
	

	def handleQueryParams(m) {
		
		def site = "http://${m.tgt}/metrics"
		def stats = new URL(site).text
		def model = getStats(stats)
		loadQueries("C:/work/stats/query.txt")
		reportHtml(model.add(cwva.Server.getInstance().dbm.rdfs))
	}

	def getGuid() {
		guid.get()
	}
	
	// extract metrics data to ttl
	def getStats(json) {
		def dir = "C:/work/stats/metrics"
		def ttl = """
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix st: <http://example.com/> .
"""
		//def c = new JsonSlurper().parse(new File("$dir/stats.json"))
		def c = new JsonSlurper().parseText(json)
		c.each{k,v->
			//			println "$k"
			v.each{k2,v2->

				//			println "\t$k2"

				ttl += """

st:${getGuid()}
		a st:Metric ;
		st:date "$k"^^xs:date ;
		st:ip \"\"\"$k2"\"\" ;
"""
			if (v2 instanceof Map)
				v2.each{k3,v3->
					if (k3.startsWith("/"))
						ttl += """st:link	"$k3" ;
"""
					else if (k3 == "count") { // ip hits
						ttl += """st:count $v3 ;"""
					}
					else if (k3 == "unknownPath") {
							
						ttl += """st:unknown	${v3.count} ;
"""
					}
				}
				
				ttl += """
.
"""

			}
		}
		//println ttl
//		def m = ju.saveStringModel(ttl,"TTL")
//		println ju.saveModelString(m,"TTL")
//		println "${m.size()}"
//		ju.saveModelFile(m,"$dir/stats.ttl","TTL")
		ju.saveStringModel(ttl,"TTL")
	}
	

	
	@Test
	void test() {
		loadQueries("C:/work/stats/query.txt")
//		reportMap.each{k,v->
//			println k
//			println v
//			println ""
//		}
		new File("C:/work/stats/junk.html").text = reportHtml()
	}
	
	def loadQueries(qf) {
		def s = ""
		new File(qf).eachLine{
			s += "$it\n"
		}
		def fs = s.split(/\n\n/)
//		fs.each{
//			println it
//			println "-"
//		}
		fs.each{
			int i=0
			def key
			it.eachLine{ln->
				if (!i++) {
					key = ln.substring(2)
					reportMap[key] = ""
				}
				reportMap[key] += "$ln\n"
			}
		}
	}

	def reportHtml(m) {
		def report = ""
//		def l = [
//			//"C:/work/stats/artpal/artPal.ttl",
//			"C:/work/stats/metrics/stats.ttl",
//		]
//		def m2 = ju.loadListFilespec(l)
//		def m = ju.loadFiles("C:/temp/git/cwvaContent/ttl")
//		m.add m2
		
		report += """
<html>
<head>
</head>
<body>
<style type="text/css">
  td {
    padding: 0 15px;
  }
</style>
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<br>
<h2>Metrics Report</h2>
${new Date()}
<br>
"""

		reportMap.each{k,v->
			try {
				def rl =  ju.queryListMap1(m,Prefixes,v)
				def i=0
				report += "<h3>$k</<h3><table>\n"
				rl.each{
					report += formatHtml(it, i++)
				}
				report += "</table>\n"
			} catch (org.apache.jena.query.QueryParseException qpe) {
				println k
				println v
				println qpe
			}
		}
		
		report += """
</body>
</html>
"""
		
		report
	}
	
	def formatHtml(m, i) {
		def report = ""
		if (!i) {
			report += "<tr>\n"
			m.keySet().each{
				report += "<th>$it</th>\n"
			}
			report += "</tr>\n"
		}
			report += "<tr>\n"
		m.each{k,v->
				report += "<td>$v</td>\n"
		}
			report += "</tr>\n"
		report
	}

	def report() {
		def l = [
			//"C:/work/stats/artpal/artPal.ttl",
			"C:/work/stats/metrics/stats.ttl",
		]
		def m2 = ju.loadListFilespec(l)
		def m = ju.loadFiles("C:/temp/git/cwvaContent/ttl")
		m.add m2

		reportMap.each{k,v->
			def rl =  ju.queryListMap1(m,Prefixes,v)
			def i=0
			println k
			rl.each{
				format(it,i++)
			}
		}
	}
	
	def format(m,i) {
		if (!i)
			m.keySet().each{
				print "$it\t"
			}
		println ""
		
		m.each{k,v->
				print "$v\t"
		}
		println ""
		
	}

//		def reportMap=[
//			"sum unknown paths by ip by date":
//			"""# sum unknown paths by ip by date
//prefix st:    <http://example.com/>
//select ?dt ?ip (sum(?u) as ?sum){
//?s st:unknown ?u ;
//	st:ip	?ip ;
//	st:date ?dt .
//	filter(?u > 1)
//} group by ?dt ?ip order by ?dt desc(?sum)
//""",
//			"sum all unknown paths by ip":"""
//# sum all unknown paths by ip
//prefix st:    <http://example.com/>
//select ?ip (sum(?u) as ?sum){
//?s st:unknown ?u ;
//	st:ip	?ip ;
//	st:date ?dt .
//	filter(?u > 10)
//} group by ?ip order by desc(?sum)
//"""
//		]
	}
