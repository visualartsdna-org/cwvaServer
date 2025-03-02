package misc

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import rdf.tools.SparqlConsole
import org.apache.commons.io.FileUtils
import groovy.io.FileType
class TestJunk {

	def ju = new JenaUtils()
	def guid = new support.Guid()
	
	// Remove .ext from filename
	@Test
	void test10() {
		println "file.name.with.dots.tgz" - ~/\.\w+$/
	}


	@Test
	void test13() {
		// test load lock

		println "read lock =${readLoadLock()}"
		writeLoadLock()

		def i=0
		while (readLoadLock()) {
			println "sleeping ${i++}"
			sleep(1000)
			if (i>5)
				deleteLoadLock()
		}

		println "read lock =${readLoadLock()}"
	}

	def loadLock = ".loadLock"
	def readLoadLock() {
		new File(loadLock).exists()
	}

	def writeLoadLock() {
		new File(loadLock).text="locked"
	}

	def deleteLoadLock() {
		new File(loadLock).delete()
	}

	@Test
	void test4() {
		// test load lock

		println "read lock =${readLoadLock()}"

		writeLoadLock()
		println "read lock =${readLoadLock()}"

		deleteLoadLock()
		println "read lock =${readLoadLock()}"
	}

	@Test
	void test3() {
		//def s = "/garbage"
		//def s = "/sparql"
		def s = "/artist.rspates"
		println policyAccept(s)
	}

	def policyAccept(s) {
		def rx = util.Rson.load("C:/temp/git/cwva/res/servletPolicy.json")
		rx.function.path.any { it != "" && s =~ /$it/}
	}

	@Test
	void test2() {
		def m = [guid:" work:0a8cb92b-39e7-4dc9-972e-25007d8c6efc".trim()]
		def guid = (m.guid =~ /([a-f0-9\-]+)$/)[0][1]
		//		def guidM = (m.guid =~ /.*([0-9a-f\-]+)$/)
		//		def guid0 = guidM[0]
		println guid
	}

	@Test
	void test1() {
		def sl2 = []
		def sl = [
			"abcdefg\n123456",
			"this is here\nthat is there"
		]
		sl.each{
			sl2+= it.replaceAll("\n","<br>")
		}
		println sl2
	}

	@Test
	void test0() {
		def userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/133.0  Mobile/15E148 Safari/605.1.15"
		def isMobile = userAgent ==~ /.*(Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini).*/
		//def isMobile = userAgent ==~ /.*(webOS|iPhone).*/
		println isMobile
	}

	@Test
	void test11() {
		def s2 = """It didn’t turn out to be as lucrative as I had hoped.  My time in New York City wasn’t a total loss."""
		println sanitize(s)
	}

	def sanitize(s) {
		s.trim().replaceAll("’","'")
	}

	@Test
	void test20() {

		def c = new JsonSlurper().parse(new File("stats.json"))
		c.each{k,v->
			println "$k"
			v.each{k2,v2->

				println "\t$k2"
				if (v2 instanceof Map) v2.each{k3,v3->
					if (k3 =~ /\/thesaurus\//)
						println "\t\t$k3 = $v3"
				}
				else println "\t\t$v2"
				//				v.find{k3,v3->
				//					//				it =~ /\/work\//
				//					v3 =~ /\/thesaurus\//
				//				}.each {k4,v4->
				//					println v4
				//				}
			}
		}
	}

	@Test
	void test22() {

		def c = new JsonSlurper().parse(new File("stats.json"))
		c.each{k,v->
			println "$k"
			v.each{k2,v2->

				println "\t$k2"
				if (v2 instanceof Map) v2.each{k3,v3->
					if (k3 =~ /\/thesaurus\//)
						println "\t\t$k3 = $v3"
				}
				else println "\t\t$v2"
				//				v.find{k3,v3->
				//					//				it =~ /\/work\//
				//					v3 =~ /\/thesaurus\//
				//				}.each {k4,v4->
				//					println v4
				//				}
			}
		}
	}
	
	def getGuid() {
		guid.get()
	}
	
	// extract metrics data to ttl
	@Test
	void testStats() {
		def dir = "C:/work/stats/metrics"
		def ttl = """
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix st: <http://example.com/> .
"""
		def c = new JsonSlurper().parse(new File("$dir/stats.json"))
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
		def m = ju.saveStringModel(ttl,"TTL")
		println ju.saveModelString(m,"TTL")
		println "${m.size()}"
		ju.saveModelFile(m,"$dir/stats.ttl","TTL")
	}
	

	// extract artpal data to ttl
	// needs date input
	@Test
	void testArtPal() {

		def dir = "C:/work/stats/artpal"
		def target = "$dir/artPal.ttl"
		def date
		def l=[]
		def gMap = [:]
		def workName
		def ipData
		def galleryData
		def keys
		def s = ""
		new File("$dir/artpal.tsv").text.eachLine {
			if (it.trim() == "") 
				return
			else if (it =~ /^IP Address	Location.*/) {
				ipData = true
				keys = it.split("\t")
			}
			else if (!ipData 
				&& !date
				&& it =~ /^(Saturday|Sunday|Monday|Tuesday|Wednesday|Thursday|Friday).*/) {
				def dt =  new SimpleDateFormat("EEE, MMM dd'th', yyyy").parse(it)
				date = new SimpleDateFormat( "yyyy-MM-dd" ).format(dt)
			}
			else if (it =~ /^Note: Locations are approximate./) {
				ipData = false
			}
			else if (it =~ /^Gallery$/) {
				galleryData = true
			}
			else if (it =~ /^ArtPal/) {
				galleryData = false
			}
			else if (ipData){
				if (it =~ /[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+/
						|| it =~ /[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+/
						) {
					s += "|$it"
				}
				else if (it =~ /^[0-9]*\.[0-9]*\.[0-9]*\.[0-9]*/
						|| it =~ /^[0-9a-f]*:[0-9a-f]*:.*/
						) {
					s += "|BAD_ADDRESS-$it"
				}
				else {
					s += " $it"
				}
			}
			else if (galleryData 
				&& it =~ /^[0-9]+.*/
				&& workName
				){
				gMap[workName]=it
				workName = null
			}
			else if (galleryData){
				workName = it
			}
		}
		
		// save gallery data
		def gRec = [:]
		gMap.each{k,v->
			//println "$k = $v"
			gRec[k] = [:]
			gRec[k].count= (v.split(" ")[0]) as int
			gRec[k].label= k.split(",")[0]
			gRec[k].text=v
			gRec[k].key= k
		}
		def gallery = [date:date,gallery:gRec]
		new File("$dir/gallery/${date}.json").text = 
			JsonOutput.prettyPrint(new JsonOutput().toJson(gallery))
		// need to map the Label to a work id
		// to make ttl
		

		def recs = s.tokenize("|")
		recs.each{

			def m = [:]
			def i=0
			def vals = it.split("\t")
			vals.each{
				m[keys[i++]] = it
			}
			if (!m["IP Address"].contains("BAD_ADDRESS"))
				l += m
		}

		def ttl = """
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix st: <http://example.com/> .
"""
		l.each{
		ttl += """

st:${getGuid()}
		a st:ArtPal ;
		st:date "$date"^^xs:date ;
		st:ip "${it["IP Address"]}" ;
		st:location "${it["Location"]}" ;
"""
		["Thumb","Large","Zoom","Room","Share","Frameshop","Add to Cart","Purchase"].each{stat->
		
		if (it.containsKey(stat)) 
			ttl += """
			st:${stat.replaceAll(" ","")} ${it[stat]?it[stat]:0} ;
"""
		}
			
		ttl += """
."""
		}

		
				//println ttl
				def m = ju.saveStringModel(ttl,"TTL")
				//println ju.saveModelString(m,"TTL")
				println "${m.size()}"
				ju.saveModelFile(m,"$dir/${date}.ttl","TTL")
		
	}
	
	@Test
	void test21() {

		def target = "c:/temp/git/cwvaServer/artPal.json"
		def date = "2025-02-14"

		def l=[]
		def startData
		def keys
		def s = ""
		new File("artpal.tsv").text.eachLine {

			if (it =~ /^IP Address	Location.*/) {
				startData = true
				keys = it.split("\t")
			}
			else if (startData){
				if (it =~ /[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+/
						|| it =~ /[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+[0-9a-f]+:[0-9a-f]+:[0-9a-f]+:[0-9a-f]+/
						) {
					s += "|$it"
				}
				else if (it =~ /^[0-9]*\.[0-9]*\.[0-9]*\.[0-9]*/
						|| it =~ /^[0-9a-f]*:[0-9a-f]*:.*/
						) {
					s += "|BAD_ADDRESS-$it"
				}
				else {
					s += " $it"
				}
			}
		}

		def recs = s.tokenize("|")
		recs.each{

			def m = [:]
			def i=0
			def vals = it.split("\t")
			vals.each{
				m[keys[i++]] = it
			}
			if (!m["IP Address"].contains("BAD_ADDRESS"))
				l += m
		}

		// incomplete
		//		def col = ["$date":[:]]
		//		def map = [:]
		//		l.each{m->
		//			col["$date"][m["IP Address"]]=[m["Location"]]
		//			if (m.Thumb)
		//			col["$date"][map["IP Address"]].add map
		//		}
		//
		//
		//		println new JsonOutput(col).prettyPrint()
		l.each{
			it.each{k,v->
				println "$k=$v"
			}
			println ""
		}
	}
	
	@Test
	void testQrcFix() {
		def img="C:/stage/qrcFix/img"
		def tgt="C:/stage/qrcFix/renamed"
		def l = []
		new File("C:/stage/qrcFix/qrcFix.txt").eachLine{
			def fs = it.split(/[\t]+/) 
			l += fs
		}
			l.each{
				println "$img/${it[0]} -> $tgt/${it[1]}"
				
				FileUtils.copyFile(new File("$img/${it[0]}"), new File("$tgt/${it[1]}"))
			}
	}

	@Test
	void testQrcRebrand() {
		//def img="C:/stage/qrcFix/img"
		def src="C:/stage/qrcFix/renamed"
		def tgt="c:/stage/qrcFix/rebranded"
		def dir = new File(src)
		dir.eachFileRecurse (FileType.FILES) { file ->
				println "branding $file -> $tgt/${file.name}"
				
				def filename = (file.name - ~/\.\w+$/)
				FileUtils.copyFile(new File("$file"), new File("$tgt/${file.name}"))
				support.ImageMgt2.makeStampedFile("","$file.name",filename,
				"C:/stage/qrcFix/rebranded","rickspates.art")
		}
		
	}

	@Test
	void testSPARQL() {
		def l = [
			//"C:/work/stats/artpal/artPal.ttl",
			"C:/work/stats/metrics/stats.ttl",
			]
		def m2 = ju.loadListFilespec(l)
		def m = ju.loadFiles("C:/temp/git/cwvaContent/ttl")
		m.add m2
		new SparqlConsole().show(m)
	}

	@Test
	void testReport() {
		def l = [
			//"C:/work/stats/artpal/artPal.ttl",
			"C:/work/stats/metrics/stats.ttl",
			]
		def m2 = ju.loadListFilespec(l)
		def m = ju.loadFiles("C:/temp/git/cwvaContent/ttl")
		m.add m2
		
		def rl =  ju.queryListMap1(m,rdf.Prefixes.forQuery,"""
# count of work refs
prefix st:    <http://example.com/>
select (count(?s) as ?sum){
	?s a st:Metric ;
		st:link ?u
	filter(regex(?u,"work"))
}

			""")
		println rl
	}

	@Test
	void testListImagesInTtl() {
		def m = ju.loadFiles("C:/stage/server/cwvaContent/ttl")
		
		def rl =  ju.queryListMap1(m,rdf.Prefixes.forQuery,"""
# list images in ttl
select ?l ?f {
	?s schema:image ?f .
	{?s rdfs:label ?l} union {?s skos:prefLabel ?l}
} order by ?f

			""")
		def s = ""
		rl.each{
			//println "${it.l}\t${it.f}"
			def n = "http://visualartsdna.org/images/".length()
			def file = it.f.replaceAll("%20"," ")
			s += "${file.substring(n)}\t${it.l}\n"
		}
		new File("C:/work/images/bucket/ttlRefsByFile.txt").text = s
	}
	
	@Test
	void testCompareBucketLocalImages() {
		def path = "C:/work/images/bucket"
		def m=[:]
		new File("$path/files.txt").eachLine{
			m[it] = null
		}
		println "files: ${m.size()}"
		
		int i=0,j=0
		def dir = new File("/temp/images")
		dir.eachFileRecurse (FileType.FILES) { file ->
			if (!m.containsKey(file.name)) {
				m[file.name] = 1
				j++
				println file
			}
			i++
		}
		println "files in /images: $i"
		println "repo files matched in /images: $j"
		
//		m.each{k,v->
//			if (!v) println k
//		}
		
	}
	
	@Test
	void testCompareBucketInstance1Images() {
		def path = "C:/work/images/bucket"
		def m=[:]
		new File("$path/files.txt").eachLine{
			m[it] = null
		}
		println "files: ${m.size()}"
		
		int i=0,j=0
		new File("$path/instance1Images.txt").eachLine{file->
			
			if (!m.containsKey(file)) {
				m[file] = 1
				j++
				println file
			}
			i++
		}
		println "files in /images: $i"
		println "repo files matched in /images: $j"
		
//		m.each{k,v->
//			if (!v) println k
//		}
		
	}
	
	// find files from repo w/no 
	// ttl reference
	@Test
	void testCompareImagesInRepos() {
		def path = "C:/work/images/bucket"
		def m=[:]
		new File("$path/files.txt").eachLine{
			m[it] = null
		}
		println "files: ${m.size()}"
		
		def c0 = 0
		new File("$path/ttlRefsByFile.txt").eachLine{
			def lf = it.split("\t")
			m[lf[0]] = lf[1]
			c0++
		}
		println "refs: $c0"
		
		int c = 0
		m.each{k,v->
			//if (v) println "$k\t$v"
			if (v) c++
		}
		println "refs: $c"
		println "not used: ${m.size()-c}"
		
		c=0
		m.each{k,v->
			if (!k.startsWith("qrc") && !v) {
				println "$k"
				c++
			}
		}
		println "non qrc, no-ref bucket files = $c"
	}
	
	@Test
	void testFindReposImgsWJPG() {
		
		def path = "C:/work/images/bucket"
		new File("$path/files.txt").eachLine{
			if (it.endsWith(".JPG"))
				println it
		}
	}


	
	@Test
	void testDate() {
		def ds = "Sunday, February 16th, 2025 - Sunday, February 16th, 2025"
		//def ds = "Saturday, February 15th, 2025 - Saturday, February 16th, 2025"
		//def ds = "Saturday, February 15th, 2025"
		def date =  new SimpleDateFormat("EEE, MMM dd'th', yyyy").parse(ds)
		def dt = new SimpleDateFormat( "yyyy-MM-dd" ).format(date)
		
		
		println date
		println dt
	}
}
