package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes
import groovy.io.FileType
import org.apache.commons.io.FileUtils

class StoreListing {
	
	def prefixes = Prefixes.forQuery
	def listBase = "G:/My Drive/art/eBay/listings"
	def listings = "$listBase/listings.ttl"
	def base = "c:/stage/store"

	def ju = new JenaUtilities()
	
	StoreListing(){
	}

	def handleQueryParams(m) {
		process(m)
	}

	def process(m) {
		def gp = "eBay gp 1"
		def gMap = load()
		def wMap = loadWork()
		if (!m.isEmpty()) {
			gp = m.selectGroup
		}
		def work = gMap[gp][0]
		if (!m.isEmpty() && m.selectWork) {
			work = m.selectWork
		}
	
		
		def html = """
<html>
<head>
</head>
<body>
<a href="${cwva.Server.getInstance().cfg.host}">Home</a>
<br>
<h3>Store Listing</h3>
Select work to populate /stage/store<br/>


<br>
<br>
<form id="myForm" action="/storeGroup.pick" method="get">
Group: 
<select name="selectGroup" id="selectGroup" onchange="this.form.submit()">
""" 
		gMap.each{k,v->
			def selected = (k == gp ? "selected" : "")
			html += """
<option value="$k" $selected>$k</option>
"""
		}
		html += """
</select>

<br>
<br>
<br>
Work: 
<select name="selectWork" id="selectWork" >
""" 
		gMap[gp].each{
			def selected = (it == work ? "selected" : "")
			html += """
<option value="$it" $selected>$it</option>
"""
		}
		html += """
</select>
<br/>
<br/>

<input type = "submit" name="submit1" id="populate" value="Populate" />
(Previous contents are deleted.)
<br/>
<br/>
<br/>
"""
	if (m.submit1 && m.submit1=="Populate") {
		html += "Populate /stage/store with listing imagery for \"$work\"<br/>"
		html += "Including image: \"${wMap[work].image}\"<br/>"
		
		def wl = loadImages(gp,work)
		delete()
		copy(gp,wl)
		imageFile(wMap[work].image)
	}
	
html += """
</form>

</body>
</html>
"""
		html 
	}
	
	def load() {
		def gMap = [:]
			def m = cwva.Server.getInstance().dbm.rdfs
			m.add ju.loadFiles(listings)
			
			
			def ms = [:]
			def l = ju.queryListMap1(m, prefixes, """
select distinct ?g ?l {
?s a vad:Listing ;
	vad:group ?g .
 ?s vad:reference ?w .
 ?w rdfs:label ?l
} order by ?g ?l
""")
		l.each{
			if (!gMap[it.g]) gMap[it.g]= []
			gMap[it.g] += it.l
		}
		gMap
	}
	
	def loadWork() {
		def map = [:]
			def m = cwva.Server.getInstance().dbm.rdfs
			m.add ju.loadFiles(listings)
			
			
			def ms = [:]
			def l = ju.queryListMap1(m, prefixes, """
select distinct ?g ?l ?w ?i {
?s a vad:Listing ;
	vad:group ?g .
 ?s vad:reference ?w .
 ?w rdfs:label ?l .
 ?w schema:image ?i
} order by ?g ?l
""")
		l.each{
			if (!map[it.l]) map[it.l]= [:]
			map[it.l].group = it.g
			map[it.l].guid = it.w
			map[it.l].image = it.i
			
		}
		map
	}
	
	def loadImages(gp,work) { // for store listing
		def list = []
			def m = cwva.Server.getInstance().dbm.rdfs
			m.add ju.loadFiles(listings)
			
			
			def l = ju.queryListMap1(m, prefixes, """
select ?i{
bind("$gp" as ?g)
bind("$work" as ?l)
?s a vad:Listing ;
	vad:group ?g .
 ?s vad:reference/rdfs:label ?l .
 ?s schema:image ?i
}""")
		l.each{
			list+= it.i
		}
		list
	}
	

	
	@Test
	void test() {
		delete()
		copy("eBay gp 1",[
			"IMG_9783.JPG",
			"IMG_9785.JPG",
			"IMG_9787.JPG",
			])
		println dirList()
	}
	
	def imageFile(url) {
		def name = url.drop(url.lastIndexOf('/'))
		def src = new File("/temp/images/$name")
		def tgt = new File("$base/$name")
		FileUtils.copyFile(src,tgt)
		
	}
	
	def delete() {
		def dir = new File("$base")
		dir.eachFileRecurse (FileType.FILES) { file ->
			file.delete()
		}

	}
	
	def copy(g,wl) {
		def dir = new File("$listBase/$g")
		dir.eachFileRecurse (FileType.FILES) { file ->
			def tgt = file.name.drop(file.name.lastIndexOf('/'))
			if (tgt in wl)
			FileUtils.copyFile(file,new File("$base/$tgt"))
		}
	}
	
	def dirList() {
		def list = []
		def dir = new File("$base")
		dir.eachFileRecurse (FileType.FILES) { file ->
			list << file
		}
		list
	}

		
}
