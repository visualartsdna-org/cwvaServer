package support

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import rdf.tools.SparqlConsole
import groovy.io.FileType
import util.Gcp

/**
 * supports both current site metric stats
 * and summary metric charts over current 
 * and logged metrics
 */
class StatsReport {

	def ju = new JenaUtils()
	def reportMap=[:]
	def guid = new util.Guid()
	def Prefixes = """
${rdf.Prefixes.forQuery}
prefix st:    <http://example.com/>
"""
	

	def handleQueryParams(m) {
		
		def site = "${m.tgt}/metrics"
		def stats = new URL(site).text
		def model = getStats(stats)
		loadQueries("${cwva.Server.getInstance().cfg.dir}/res/metrics.sparql")
		reportHtml(model.add(cwva.Server.getInstance().dbm.rdfs))
	}

	def cnt = 0
	def getGuid() {
		guid.get()	// metric scope is just one report
		//"metric${cnt++}"
	}
	
	// extract metrics data to ttl
	def getStats(String json) {
		def c = new JsonSlurper().parseText(json)
		getStats(c)
	}
	
	// extract metrics data to ttl
	def getStats(Map c) {
		//def dir = "C:/work/stats/metrics"
		def ttl = """
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix st: <http://example.com/> .
"""
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
					if (k3.startsWith("/")) {
						ttl += """st:link	"$k3" ;
"""
						ttl += """st:linkCnt	[
							st:link "$k3" ;
							st:count ${v3.count} ;
						] ;
"""
					}
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
		ju.saveStringModel(ttl,"TTL")
	}
	
	def loadQueries(qf) {
		def s = ""
		new File(qf).eachLine{
			s += "$it\n"
		}
		def fs = s.split(/\n\n/)
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
	
	// old--initial metric report for printing
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
		if (!i) {
			m.keySet().each{
				print "$it\t"
			}
			println ""
		}
		
		m.each{k,v->
				print "$v\t"
		}
		println ""
	}
	
	// above this line is current metric report
	// below is current and archive metric summary charts
	// ref, https://www.w3schools.com/ai/ai_chartjs.asp
	
	// creates an n-period SMA
	// over the list 
	def smaCalc(List l,int n) {
		def sum = 0
		int i=0
		if (l.size() >= n) {
			for (int j=l.size()-n;j<l.size();j++){
				sum += l[j]
				i++
			}
			return sum / i
		} else {
			l.each{
				sum += it
				i++
			}
			sum / i
		}
	}

	// html display of metric charts
	def formatJson(m,i,label) {
		def s = ""
		def maxY = 0
		def listY = []
		def sma = []
		def x = """
const xValues$i = ["""
		def y = []
		
		m.each{m2->
				x += "\"${m2.dt}\","
				y += m2.scnt as int
				maxY = Math.max(maxY,m2.scnt as int)
				listY += m2.scnt as int
				sma += smaCalc(listY,10 )
		}
		x += "];"
		
		s += """
$x

new Chart("myChart$i", {
  type: "line",
  data: {
    labels: xValues$i,
    datasets: [{
	  label: "$label",
      fill: false,
      lineTension: 0,
      backgroundColor: "rgba(0,0,255,1.0)",
      borderColor: "lightBlue",
	  pointRadius: '0',
      data: $y
    },{
	  label: "SMA(10)",
      fill: false,
      lineTension: 0,
      backgroundColor: "rgba(0,0,255,1.0)",
      borderColor: "grey",
	  pointRadius: '0',
      data: $sma
    }]
  },
  options: {
    legend: {display: true,
             position: 'bottom',
             align: 'rgt'
	},
    scales: {
      yAxes: [{ticks: {min: 1, max:${maxY + 50}}}],
    }
  }
});"""
		s
	}

	
	/**
	 * Run a cwva server
	 * @param args
	 */
	public static void main(String[] args){

		def map = util.Args.get(args)
		def sr = new StatsReport()
		def html = sr.driver("https://visualartsdna.org/metrics",
			"C:/work/stats/metricsSummary.sparql",
			"C:/work/stats/log.zip"
			)

//		new File("/work/stats/metrics.html").text = html
		// send metrics.html to gcp/metrics folder
		// overwriting any previous file
			
		def src = "/work/stats/chart.html"
		new File(src).text = html
		def oa = Gcp.gcpCpBucket(src,"stats/chart.html")
//		html

	}


	// generate metric summary graphs
	@Test
	void test() {
		long ctms = System.currentTimeMillis()
		def html = driver("https://visualartsdna.org/metrics",
			"C:/work/stats/metricsSummary.sparql",
			"C:/work/stats/log.zip"
//			"C:/work/stats/log"
			)
//		println html
		new File("/work/stats/test4.html").text = html
		println "${(System.currentTimeMillis()-ctms)/1000} sec"
	}

	// returns html
	def driver(site, queries, logFiles) {
		loadQueries(queries)
		
		// load current metric data to model
		def stats = new URL(site).text
		def c = new JsonSlurper().parseText(stats)
		def mod = getStats(c)
//		def mod = ju.newModel()
		
		// add all archived log-metric data to model
		mod.add loadLogZipTtl(logFiles)
//		mod.add loadLogZip(logFiles)
//		mod.add loadLogDir(logFiles)
		
		// generate html from model
		def date = "${new Date()}"
		def sb = """

<!DOCTYPE html>
<html>
<head>
<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.4/Chart.js">
</script>
</head>
<body>
<br>
<h3>Activity Metrics</h3>
$date
<br>
<br>
"""
		def canvas=0
		reportMap.each{k,v->
			try {
				def rl =  ju.queryListMap1(mod,Prefixes,v)
				def i=0
				def label = k.split(" ")[0]
				def s = formatJson(rl,canvas,label)
				sb += """
$k
<br>
<canvas id="myChart${canvas++}" style="width:100%;max-width:600px"></canvas>
<script>
"""
			sb += "$s\n"
			sb += """
</script>
<br>
"""
			} catch (org.apache.jena.query.QueryParseException qpe) {
				println k
				println v
				println qpe
			}

		}
			sb += """
</body>
</html>
"""
		"$sb"
	}

	// loads metric data from archived log files
	// into local permanent ttl files
	// adding new ones as necessary
	// more efficient
	def loadLogZipTtl(ds) {
		def ttlDir = "C:/work/stats/ttl"

		def lzf = new File(ds)
		def zipFile = new java.util.zip.ZipFile(lzf)
		zipFile.entries().each {file->
			if (file.name.startsWith("log/out")) {
				def tfn = file.name
				.replaceAll(".log",".ttl")
				.replaceAll("log/","${ttlDir}/")
				if (!new File(tfn).exists()) {
					def s = loadFromLog(zipFile.getInputStream(file).text)
					def c = new JsonSlurper().parseText(s)
					def m = getStats(c)
					ju.saveModelFile(m,tfn,"ttl")
				}
			}
		}
		ju.loadFiles(ttlDir)
	}
	
	// loads metric data from archived log files
	def loadLogZip(ds) {
		def ttl = "C:/work/stats/logMetric.ttl"
		def msf = new File(ttl)
		def lzf = new File(ds)
		if (msf.exists()
			&& msf.lastModified() 
			> lzf.lastModified()
			) {
				return ju.loadFiles(ttl)
			}
		
//		def dir = new File(ds)
		Map m = [:]
		def zipFile = new java.util.zip.ZipFile(lzf)
		zipFile.entries().each {file->
			if (file.name.startsWith("log/out")) {
				def s = loadFromLog(zipFile.getInputStream(file).text)
				def c = new JsonSlurper().parseText(s)
				m += c
			}
		}
		def model = getStats(m)
		ju.saveModelFile(model,ttl,"ttl")
		model
	}
	
	def loadFromLog(File fs) {
		loadFromLog(fs.text)
	}
	
	// extract the log metric data from zip
	def loadFromLog(String s) {
		def json = ""
		def found = false
		s.eachLine{
			if (it == "/cestfini ") {
				found = true
				return
			}
			if (it =~ /[0-9]+-[0-9]+-[0-9]+T[0-9]+:[0-9]+:[0-9]+\tfini$/) {
				return ;
			}
			//println it
			if (found
				&& it =~ /[0-9]+-[0-9]+-[0-9]+T[0-9]+:[0-9]+:[0-9]+\t\{$/) {
				json += "{\n"
			}
			else if (found) {
				json += "$it\n"
			}
		}
		json
	}

	// loads metric data from archived log files
	def loadLogDir(ds) {
		
		def dir = new File(ds)
		Map m = [:]
		if (dir.isDirectory()) {
			dir.eachFileRecurse (FileType.FILES) { file ->
				if (file.name.startsWith("out")) {
					def s = loadFromLog(file)
					def c = new JsonSlurper().parseText(s)
					m += c
				}
			}
		} else { // a single file to load
			def file = dir
			def s = loadFromLog(file)
			def c = new JsonSlurper().parseText(s)
			m += c
		}
		getStats(m)
	}
	
	@Test
	void testSparql() {
		long ctms = System.currentTimeMillis()
		def site = "http://visualartsdna.org/metrics"
		def logFiles ="C:/work/stats/log.zip"
		
		// load current metric data to model
		def stats = new URL(site).text
		def c = new JsonSlurper().parseText(stats)
		def mod = getStats(c)
		
		ju.saveModelFile(mod,"/work/stats/testMetricsNow.ttl","ttl")
		
		// add all archived log-metric data to model
		mod.add loadLogZip(logFiles)
		new SparqlConsole().show(mod)
		
	}

	@Test
	void testSparql2() {
		long ctms = System.currentTimeMillis()
		def site = "http://visualartsdna.org/metrics"
		def logFiles ="C:/work/stats/log.zip"
		
		// load current metric data to model
		def stats = new URL(site).text
		def c = new JsonSlurper().parseText(stats)
		def mod = getStats(c)
		
		ju.saveModelFile(mod,"/work/stats/testMetricsNow.ttl","ttl")
		
		// add all archived log-metric data to model
		mod.add loadLogZipTtl(logFiles)
		new SparqlConsole().show(mod)
		
	}

}
