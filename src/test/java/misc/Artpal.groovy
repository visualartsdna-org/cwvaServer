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

class Artpal {

	@Test
	void test() {
		query()
	}
	
	def query() {
		def ju = new JenaUtils()
		def site = "http://visualartsdna.org/metrics"
		// load current metric data to model
		def stats = new URL(site).text
		def c = new JsonSlurper().parseText(stats)
		def mod = new support.StatsReport().getStats(c)
		mod.add ju.loadFiles("c:/work/stats/logMetric.ttl")
		mod.add ju.loadFiles("c:/work/stats/artpal")
		
		new SparqlConsole().show(mod)
		
	}

	// extract artpal data to ttl
	// needs date input
	@Test
	void testArtPal() {
		def src = "C:/work/stats/artpal/tsv"
		def tgt = "C:/work/stats/artpal"
		def dir = new File(src)
		dir.eachFileRecurse (FileType.FILES) { file ->
			extractArtPal(file,tgt)
		}
		def ju = new JenaUtils()
		println "size = ${ju.loadFiles(tgt).size()}"
	}
	
	def extractArtPal(file,tgt) {

		def dir = "C:/work/stats/artpal"
		def date
		def l=[]
		def gMap = [:]
		def workName
		def ipData
		def galleryData
		def keys
		def s = ""
		def nfMap = [
			/[0-9]+st,/:"EEE, MMM dd'st', yyyy",
			/[0-9]+nd,/:"EEE, MMM dd'nd', yyyy",
			/[0-9]+rd,/:"EEE, MMM dd'rd', yyyy",
			/[0-9]+th,/:"EEE, MMM dd'th', yyyy",
			]
		file.text.eachLine { // .text?
			if (it.trim() == "")
				return
			else if (it =~ /^IP Address	Location.*/) {
				ipData = true
				keys = it.split("\t")
			}
			else if (!ipData
				&& !date
				&& it =~ /^(Saturday|Sunday|Monday|Tuesday|Wednesday|Thursday|Friday).*/) {
				def fmt = nfMap.find{k,v->
					it =~ k
				}
				def dt =  new SimpleDateFormat(fmt.getValue()).parse(it)
				date = new SimpleDateFormat( "yyyy-MM-dd" ).format(dt)
				println dt
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
//					def ls = it.split("\t")
//					s += "|${ls[0]}\t${ls[1]},"
				}
				else if (it =~ /^[0-9]*\.[0-9]*\.[0-9]*\.[0-9]*/
						|| it =~ /^[0-9a-f]*:[0-9a-f]*:.*/
						) {
					s += "|BAD_ADDRESS-$it"
				}
				else if (it.contains(",")){ // Tokyo, Tokyo	45	1
					def m = (it =~ /([A-Za-z. \-'\p{L}\p{M}]+),[ ]+([A-Za-z0-9. \-'\p{L}\p{M}‘]+)\t(.*)/)[0]
					s += ",${m[1]},${m[2]}\t${m[3]}"
				}
				else if (it =~ /^[0-9\t]+$/){
					s += "\t$it"
				}
				else {
					def m = (it =~ /([A-Za-z. \-'\p{L}\p{M}]+)\t(.*)/)[0]
					s += ",${m[1]}\t${m[2]}"
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
		
		//println s
		def recs = s.tokenize("|")
		recs.each{

			def m = [:]
			def i=0
			//it = it.replaceAll("\t",",")
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
			
		def la = it["Location"].split(",")
		ttl += """

st:${getGuid()}
		a st:ArtPal ;
		st:date "$date"^^xs:date ;
		st:ip "${it["IP Address"]}" ;
		st:location "${it["Location"]}" ;
		st:country "${la.size()>=1 ? la[0] : ""}" ;
		st:region "${la.size()>=2 ? la[1] : ""}" ;
		st:city "${la.size()==3 ? la[2] : ""}" ;
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
				ju.saveModelFile(m,"$tgt/${date}.ttl","TTL")
		
	}

}
