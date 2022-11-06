package util

import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import groovy.io.FileType
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat


class CompDirs {

	@Test
	void test() {
		def ttl = "/temp/git/cwvaContent/ttl/data"
		def model = new rdf.JenaUtilities().loadFiles(ttl)
		def d = "gcp://images"
		//def d = "file://G:/My Drive/CWVA/images"
		
		def s = insynchReport(model, d)
		println s
	}
	
	def insynchReport(model, d) {
		def l = insynch(model, d)
		def s = "${new Date()}\n"
		s += "Model image file references not found in $d\n"
		l.each{
			s += "$it\n"
		}
		s
	}

	// check for ttl/model refs to jpgs 
	// vs. existence of files in a given dir
	def insynch(model, d) {
		def ju = new rdf.JenaUtilities()
		def lm = ju.queryListMap1(model,"","""
prefix schema: <https://schema.org/>
prefix vad: <http://visualartsdna.org/2021/07/16/model#>
select ?img ?qrc {
  ?s schema:image ?img .
  ?s vad:qrcode ?qrc .
} order by ?img
""")
		def set=[:]
		lm.each{
			set[getFilename(it.img)]=null
			set[getFilename(it.qrc)]=null
		}

		def m = dirAny(d)
		
		def l = []
		set.sort().each{k,v->
			if (!m.containsKey(k))
				l+= "$k"
		}
		l
	}
	
	def dirAny(d) {
		def m=[:]
		if (d.startsWith("gcp://")) {
			m = dirGcp(d.substring("gcp://".length()))
		} else if (d.startsWith("file://")) {
			m = dir(d.substring("file://".length()))
		}
		m
	}

	
	// compare content of bucket folder vs file folder
	@Test
	void test2() {
		def d1 = "images"
		def d2 = "G:/My Drive/CWVA/images"
		def m = compare(
		dirGcp(d1),
		dir(d2)
		)
		println "d1: $d1, d2: $d2"
		m.sort().each{k,v->
			println "$k, $v"
		}
	}
	
	@Test
	void test2a() {
		def d1 = "gcp://images"
		def d2 = "file://G:/My Drive/CWVA/images"

		println compareReport(d1,d2)
	}
	
	def compareReport(d1,d2) {

		def m1=dirAny(d1)
		def m2=dirAny(d2)
		
		def m = compare(
		m1,
		m2
		)
		def s = "${new Date()}\n"
		s += "$d1 -- $d2\n"
		
		m.sort().each{k,v->
			s+= "$k, $v\n"
		}
		s
	}

	// gcp dir
	// requires gcp_bucket in sys props
	@Test
	void test1() {
		printMap dirGcp("images")
	}
	
	// filesystem dir
	@Test
	void test0() {
		printMap dir("/temp/images")
	}
	
	// compare name and size
	// results where different
	def compare(d1,d2) {
		
		// create one set of keys
		def set = [:]
		d1.each{k,v-> set[k] = null}
		d2.each{k,v-> set[k] = null}
		
		def m=[:]
		set.each{k,v->
			if (d1.containsKey(k) && !d2.containsKey(k))
				m[k] = "<- only"
			else if (!d1.containsKey(k) && d2.containsKey(k))
				m[k] = "only ->"
			else if (d1[k]["size"] != d2[k]["size"])
				m[k] = "sizes differ d1:${d1[k]["size"]} d2:${d2[k]["size"]}"
		}
		m
	}
	
	def printMap(m) {
		m.each{k,v->
			println "$k = $v"
		}
	}
	
	// remove path from filename
	def getFilename(s) {
		((""+s)=~/^.*[\/\\](.*)$/)[0][1]
	}
	
	// ls of file system
	def dir(d) {
		def list = []
		
		def dir = new File(d)
		dir.eachFileRecurse (FileType.FILES) { file ->
		  list << file
		}
		def m=[:]
		list.each{
			Path file = Paths.get(""+it)
			BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class)
			def sdt = attr.creationTime()
			m[getFilename(it)]=[size:it.size(),date:sdt]
			
		}
		m
	}

	// ls of gcp bucket
	def dirGcp(src) {
		def m=[:]
		def s = Gcp.gcpLs(src, true)[0]
				s.toString().split("\n").each{
			def l = (""+it).trim().split(/[\t ]+/)
			if (l.size()==3)
				m[getFilename(l[2])]=[size:l[0] as int,date:l[1]]
		}
		m
	}

}
