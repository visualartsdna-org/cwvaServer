package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import util.Rson
class JsonRdfUtilTest {
	
	@Test
	public void test33() {
		def src = "C:/temp/tratsi/Takeout/Keep/ttl/covid.ttl"
		def m = new JenaUtils().loadFiles(src)
		println m.size()
	}
	
	// json to ttl
	@Test
	public void testVia() {
		def src = "C:/test/via/json"
		def dest = "C:/test/via/ttl"
		def type = "vad:Via" // in this case
		def prefix = "via"
		new Takeout2Ttl().process(src,dest,type,prefix)
	}
	
	// json to ttl
	@Test
	public void testNotTko2() {
		def src = "C:/temp/junk/rules"
		def dest = "C:/temp/tratsi/Takeout/Keep/ttl"
		def type = "vad:LsysRules" // in this case
		def prefix = "tko"
		process(src,dest,type,prefix)
	}
	
	// see RewriteJson.test() for release version
	@Test
	public void test2() {
		def src = "C:/temp/generatedFiles"
		def dest = "C:/temp/generatedFiles"
		def js = new JsonSlurper()
		def ju = new JenaUtils()
		new File(src).eachFile {file->
			
			if (!(file.name.endsWith(".json"))) return
		//def file = new File("C:/temp/Takeout/Takeout/Keep/Publish.json")
			println "$file"
			def model = ju.newModel()
			file.eachLine {
				def c
				try {
					c = js.parseText(it)
					//c.message.remove("synopses")
				} catch (Exception ex) {
					println """
$file
$it
$ex
"""
				}
				if (!c) return
				def sb = new StringBuilder()
				sb.append  """
@prefix tko: <http://visualartsdna.org/takeout#> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .

"""
				try {
					JsonRdfUtil.jsonToTtl([tko:[c]], sb, "tko:")
					//new File("premodel.ttl").text= "$sb"
					def s = (""+sb).replaceAll(/([^\\])\\([^tbnrf\\'"])/,/$1\\\\$2/)

					def m = ju.saveStringModel(s, "ttl")
					model.add(m)
				} catch (Exception ex) {
					println """
$file
$sb
$ex
"""
				}
			}
			ju.saveModelFile(model,
				"$dest/${(file.name=~/(.*)\.json/)[0][1]}.ttl", "ttl")
			//"C:/temp/Takeout/Takeout/ttl/junk.ttl", "ttl")

		}
	}

	@Test
	public void test1() {
		def js = new JsonSlurper()
		def ju = new JenaUtils()
		def dir = "C:/temp/Takeout/Takeout/Keep"
		new File(dir).eachFile {file->
			
			if (!(file =~ /.*.json/)) return
		//def file = new File("C:/temp/Takeout/Takeout/Keep/Publish.json")
			println "$file"
			def model = ju.newModel()
			file.eachLine {
				def c
				try {
					c = js.parseText(it)
					//c.message.remove("synopses")
				} catch (Exception ex) {
					println """
$file
$it
$ex
"""
				}
				if (!c) return
				def sb = new StringBuilder()
				sb.append  """
@prefix tko: <http://visualartsdna.org/takeout#> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .

"""
				try {
					JsonRdfUtil.jsonToTtl([tko:[c]], sb, "tko:")
					//new File("premodel.ttl").text= "$sb"
					def s = (""+sb).replaceAll(/([^\\])\\([^tbnrf\\'"])/,/$1\\\\$2/)

					def m = ju.saveStringModel(s, "ttl")
					model.add(m)
				} catch (Exception ex) {
					println """
$file
$sb
$ex
"""
				}
			}
			ju.saveModelFile(model,
				"C:/temp/Takeout/Takeout/ttl/${(file.name=~/(.*)\.json/)[0][1]}.ttl", "ttl")
			//"C:/temp/Takeout/Takeout/ttl/junk.ttl", "ttl")

		}
	}

		@Test
		public void test0() {
			def js = new JsonSlurper()
			def ju = new JenaUtils()
			def dir = "C:/temp/Takeout/Takeout/Keep"
			//new File(dir).eachFile {file->
			def file = new File("C:/temp/Takeout/Takeout/Keep/Publish.json")
				println "$file"
				def model = ju.newModel()
				file.eachLine {
					def c
					try {
						c = js.parseText(it)
						//c.message.remove("synopses")
					} catch (Exception ex) {
						println """
$file
$it
$ex
"""
					}
					if (!c) return
					def sb = new StringBuilder()
					sb.append  """
@prefix tko: <http://visualartsdna.org/takeout#> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .

"""
					try {
						JsonRdfUtil.jsonToTtl([tko:[c]], sb, "tko:")
						//new File("premodel.ttl").text= "$sb"
						def s = (""+sb).replaceAll(/([^\\])\\([^tbnrf\\'"])/,/$1\\\\$2/)
	
						def m = ju.saveStringModel(s, "ttl")
						model.add(m)
					} catch (Exception ex) {
						println """
$file
$sb
$ex
"""
					}
				}
				ju.saveModelFile(model,
					//"C:/temp/Takeout/ttl/${(file.name=~/(.*)\.json/)[0][1]}.ttl", "ttl")
				"C:/temp/Takeout/Takeout/ttl/junk.ttl", "ttl")
	
			}
	
	
}
