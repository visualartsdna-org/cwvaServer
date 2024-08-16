package util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import java.nio.charset.*
import java.util.zip.ZipFile
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Paths
import org.apache.james.mime4j.mboxiterator.*
import groovy.io.FileType

// backup

class TestMbox {
	def printMessage
	def tgt = "C:/temp/Takeout/Larry"
	def dataPath = "C:/art/photographers/Larry Minerich/mbox"
	def mboxPath = "C:/temp/Takeout/Larry/Takeout/Mail/Larry.mbox"
	def downloads = "C:/Users/ricks/Downloads"

	@Test
	void test() {

		FileUtils.deleteDirectory(new File("$tgt/Takeout"))
		
		def file = getLatestFileType(downloads,".zip")
		if (!file) {
			println "no zip file"
			return
		}
		unzipFile(file,tgt)

		def ids = loadPastIds()
		CharsetEncoder ENCODER = Charset.forName("UTF-8").newEncoder();
		def msg=0
		for (CharBufferWrapper message : MboxIterator.fromFile(new File(mboxPath)).charset(ENCODER.charset()).build()) {
			def m = getTokens(message,ids)
			if (!m.isEmpty()) {
			println "\nMessage ${msg++}\t${m["part0"]["Date"]}"

			m.each{k,v->
				println k
				if (printMessage) v.each{k1,v1->
					if (k1=="content")
						println "${k}--${k1}:\n\t${v1.substring(0,Math.min(100,v1.size()))}"
					else println "${k}--${k1}:${v1}"
				}

				if (m[k].containsKey("Content-Type")
					//&& m[k]["Content-Type"]
						&& m[k]["Content-Type"] instanceof Map
						&& m[k]["Content-Type"]["Content-Type"] == "image/jpeg") {
					println "saving ${m[k]["Content-Type"]["name"]}"
					def jpg = removeQuotes(m[k]["Content-Type"]["name"])
					byte[] decoded = Base64.getDecoder().decode(m[k]["content"]);
					final ByteArrayOutputStream os = new ByteArrayOutputStream()
					os.withCloseable {
						it << decoded
					}

					new File("$dataPath/$jpg").withOutputStream { stream ->
						os.writeTo(stream)
					}
				}
				if (m[k]["Content-Type"]
						&& m[k]["Content-Type"] instanceof Map
						&& m[k]["Content-Type"]["Content-Type"] == "video/quicktime") {
					println "saving ${m[k]["Content-Type"]["name"]}"
					def jpg = removeQuotes(m[k]["Content-Type"]["name"])
					byte[] decoded = Base64.getDecoder().decode(m[k]["content"]);
					final ByteArrayOutputStream os = new ByteArrayOutputStream()
					os.withCloseable {
						it << decoded
					}

					new File("$dataPath/$jpg").withOutputStream { stream ->
						os.writeTo(stream)
					}
				}
			}
			// remove content to store in json
			def m2=[:]
			m.each{k,v->
				m2[k] = [:]
				v.each{k1,v1->
					if (k1 == "content"
							&& (v["Content-Type"]
							&& m[k]["Content-Type"] instanceof Map
							&& (v["Content-Type"]["Content-Type"] =="image/jpeg"
							|| v["Content-Type"]["Content-Type"] =="video/quicktime")
							)
							) {
						m2[k]["content"] = "[streamed to file]"
					}
					else m2[k][k1] = v1
				}
			}
			
			def id = ""
			if (m["part0"]["Message-Id"] instanceof String) {
				id = m["part0"]["Message-Id"]
			} else {
				id = m["part0"]["Message-Id"]["Message-Id"]
			}
			id = id.replaceAll(/[<>]/,"")
			new File("$dataPath/${id}.json").text = JsonOutput.prettyPrint(
					new JsonOutput().toJson(m2)
					)
			}
		}
		
		file.delete() // rm the zip
		
	}
	
	@Test
	void testReport() {
		def list = []

		int i=0
		def dir = new File(dataPath)
		dir.eachFileRecurse (FileType.FILES) { file ->
			if ((""+file).endsWith(".json"))
			list << file
		}
		list.each{
			i++
			//println it
			def m = new JsonSlurper().parse(it)

			m.each{k,v->
				if (v instanceof String ) println "$v"
				else if (v["Subject"]
					&& v["Subject"] instanceof Map
					)
					println "${removeEmojis(v["Subject"]["Subject"])}"
				if (v["content"]
					&& v["Content-Type"] instanceof Map
					&& v["Content-Type"]["Content-Type"] == "text/plain"
					&& v["content"] != "Larry")
					println "${removeEmojis(v["content"])}"
				if (v["Content-Type"]
					&& v["Content-Type"] instanceof Map
					&& v["Content-Type"]["name"])
					println "\t${removeEmojis(removeQuotes(v["Content-Type"]["name"]))}"
			}
		}
		
		println "$i files"
	}
	
	def removeQuotes(s) {
		s.replaceAll('"',"")
	}

	def getTokens(message,ids) {
		def m=[:]
		def part = 0
		m["part$part"] = [:]
		m["part$part"]["content"] = ""
		def key = ""
		try {
		(""+message).eachLine {
			if (it =~ /[A-Za-z0-9\-]+: .*/) {
				key = (it =~ /([A-Za-z0-9\-]+): (.*)/)[0][1]
				if (key =~ /(?i)Message-Id/)
					key = "Message-Id"
				def val = ((it =~ /([A-Za-z0-9\-]+:) (.*)/)[0][2])
				if (key == "Message-Id"
					&& ids.containsKey(val))
					throw new RuntimeException(val)
				m["part$part"][key] = val
			} else if (it =~ /^--[A-Za-z0-9\-]+/) {
				m["part$part"].each{k,v->
					if (k != "content") {
						def m2 = parseFields(k,v)
						if (!m2.isEmpty())
							m["part$part"][k] = m2
					}
				}
				part++
				m["part$part"] = [:]
				m["part$part"]["content"] = ""
			} else if (it =~ /^[\t ]+.*/){
				m["part$part"][key] += it
			} else {
				m["part$part"]["content"] += it
			}
		}
		} catch (RuntimeException rex) {
			println "old message ${rex.message}"
			return [:]
		}
		m
	}

	def parseFields(f,s) {
		def m=[:]
		if (s.trim() == "") return m
		//def other=0
		def l = s.split(";")
		l.each{
			def l2 = it.split("=")
			if (l2.size()==2)
				m[l2[0].trim()] = l2[1].trim()
			else
				m[f] = it.trim()
		}
		m
	}

	def unzipFile(File file,tgt) {
		//cleanupFolder()
		def zipFile = new ZipFile(file)
		zipFile.entries().each { it ->
			def path = Paths.get("$tgt/${it.name}")
			if(it.directory){
				Files.createDirectories(path)
			}
			else {
				def parentDir = path.getParent()
				if (!Files.exists(parentDir)) {
					Files.createDirectories(parentDir)
				}
				Files.copy(zipFile.getInputStream(it), path)
			}
		}
	}
	
	def getLatestFileType(folder, ext) {
		def dir = new File(folder)
		def list = []
		dir.eachFileRecurse (FileType.FILES) { file ->
			if ((""+file).endsWith(ext))
		  list << file
		}
		list.sort{a,b->
			b.lastModified() <=> a.lastModified()
		}
		list.first()
	}

	def removeEmojis(s) {
		if (!s) return ""
		def s1 = s.replaceAll(/=\?utf-8\?Q\?/,"")
		def s2 = s1.replaceAll(/=[A-Z0-9][A-Z0-9]/,"")
		def s3 = s2.replaceAll(/_\?=/,"")
		def s4 = s3.replaceAll("_"," ")
		def s5 = s4.replaceAll("=","")
		s5.replaceAll("([0-9])([A-Z])",'$1 $2')
	}
	
	@Test
	void testRemoveEmojis() {
		def s = """
=?utf-8?Q?Great_Horned_Owl_=F0=9F=A6=89_?=
=?utf-8?Q?Eagle_=F0=9F=A6=85_River_Nature_Center_?=
=?utf-8?Q?Bull_Moose_=F0=9F=AB=8E_?=
Taken at Moose Pond not far from my home in Eagle =F0=9F=A6=85 River a coupl=e of years ago.
Eagle =F0=9F=A6=85 River=20
January 16, 2022=20Eagle =F0=9F=A6=85 River=20
Sunrise from the Beaver =F0=9F=A6=AB viewing deck. Eagle =F0=9F=A6=85 River N=ature Center=20December 17, 2015
=?utf-8?Q?Bald_Eagle_=F0=9F=A6=85_?=
January 13,2016Eagle =F0=9F=A6=85 River Nature Center=20
=?utf-8?Q?Great_Horned_Owl_=F0=9F=A6=89_?=
=?utf-8?Q?Eagle_=F0=9F=A6=85_River_Nature_Center_?=
=?utf-8?Q?Bull_Moose_=F0=9F=AB=8E_?=
Taken at Moose Pond not far from my home in Eagle =F0=9F=A6=85 River a coupl=e of years ago.
Eagle =F0=9F=A6=85 River=20
January 16, 2022=20Eagle =F0=9F=A6=85 River=20
Sunrise from the Beaver =F0=9F=A6=AB viewing deck. Eagle =F0=9F=A6=85 River N=ature Center=20December 17, 2015
=?utf-8?Q?Bald_Eagle_=F0=9F=A6=85_?=
January 13,2016Eagle =F0=9F=A6=85 River Nature Center=20
"""
		s.eachLine{
			println "${removeEmojis(it)}"
		}
	}
	
	@Test
	void testFields() {

		"""
text/plain;
	charset=us-ascii
7bit
image/jpeg;	name=IMG_0114.jpg;	x-apple-part-url=9EA403B9-F3BC-41EB-823A-A7A85891D8AE
	""".eachLine{
					def m = parseFields("Content-Type",it.trim())
					m.each{k,v->
						println "$k = $v"
					}
				}
	}

	@Test
	void testFields0() {

		def s = "image/jpeg;	name=IMG_0114.jpg;	x-apple-part-url=9EA403B9-F3BC-41EB-823A-A7A85891D8AE"
		def m = parseFields("Content-Type",s)
		m.each{k,v->
			println "$k = $v"
		}
	}

	// too slow
	def getMessages(mbox) {

		def m=[:]
		def key
		new File(mbox).eachLine{
			if (it =~ /From [0-9]+@xxx.*/) {
				key = it
				m[key] = ""
			} else {
				m[key] += it
			}
		}
	}


	@Test
	void test01() {

		def mboxPath = "C:/temp/Takeout/Larry/Takeout0/Mail/Larry.mbox"
		def m = getMessages(mboxPath)

		m.each{ k,v->
			println v
		}
	}

	@Test
	void test0() {

		def mboxPath = "C:/temp/Takeout/Larry/Takeout0/Mail/Larry.mbox"
		CharsetEncoder ENCODER = Charset.forName("UTF-8").newEncoder();
		final File mbox = new File(mboxPath);

		for (CharBufferWrapper message : MboxIterator.fromFile(mbox).charset(ENCODER.charset()).build()) {
			println(message);
		}
	}
	
	def loadPastIds() {
		def list = []
		def ids = [:]

		def dir = new File(dataPath)
		dir.eachFileRecurse (FileType.FILES) { file ->
			if ((""+file).endsWith(".json"))
		  list << file
		}
		list.each{
			def m = new JsonSlurper().parse(it)
			def id = ""
			def dt = ""
			if (m["part0"]["Message-Id"] instanceof String)
				id = m["part0"]["Message-Id"]
			else 
				m["part0"]["Message-Id"]["Message-Id"]
				
			if (m["part0"]["Date"] instanceof String)
				dt = m["part0"]["Date"]
			else
				dt = m["part0"]["Date"]["Date"]
			ids[id] = dt
		}
		ids
	}

	@Test
	void testGetIds() {
		def ids = loadPastIds()
		ids.each{k,v->
			println "$k, $v"
		}
	}



}


