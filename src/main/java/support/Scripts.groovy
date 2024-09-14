package support

import static org.junit.jupiter.api.Assertions.*
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile
import groovy.io.FileType
import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import util.Token

// Scripts to run the distribute data and metadata processes
class Scripts {

	def bucket = System.getProperty("gcp_bucket") ?: "n/a"
	def host = "visualartsdna.org"
	def gs = "gs://$bucket"
	def imageArchive = "G:/My Drive/CWVA/images"
	def root = "C:/stage"
	
	def distributeData = """
cp $root/data/*.ttl C:/temp/git/cwvaContent/ttl/data
mv IMG_*.JPG old
cp $root/data/*.gif "$imageArchive"
cp $root/data/*.jpg "$imageArchive"
cp $root/data/*.JPG "$imageArchive"
rem
gsutil -m cp $root/data/*.jpg $gs/images
gsutil -m cp $root/data/*.gif $gs/images
rem gsutil -m cp $root/data/*.JPG $gs/images
gsutil cp $root/data/*.ttl $gs/ttl/data
rem
restart
rem
mv $root/data/*.ttl $root/data/old
mv $root/data/*.jpg $root/data/old
"""
	
	def distributeMetadata = """
gsutil -m cp $root/metadata/*.jpg $gs/images
gsutil cp $root/metadata/model/*.ttl $gs/ttl/model
gsutil cp $root/metadata/provenance/*.ttl $gs/ttl/provenance
gsutil cp $root/metadata/tags/*.ttl $gs/ttl/tags
gsutil cp $root/metadata/tko/*.ttl $gs/ttl/tko
gsutil cp $root/metadata/topics/*.ttl $gs/ttl/topics
rgsutil cp $root/metadata/vocab/*.ttl $gs/ttl/vocab
rem
restart
rem
cp $root/metadata/*.jpg "$imageArchive"
mv $root/metadata/model/*.ttl $root/metadata/old
mv $root/metadata/provenance/*.ttl $root/metadata/old
mv $root/metadata/tags/*.ttl $root/metadata/old
mv $root/metadata/tko/*.ttl $root/metadata/old
mv $root/metadata/topics/*.ttl $root/metadata/old
mv $root/metadata/vocab/*.ttl $root/metadata/old
mv $root/metadata/*.jpg $root/metadata/old
	"""
	
	def registry = [
		"distributeData": distributeData,
		"distributeMetadata": distributeMetadata
		]


	@Test
	void test() {
		//process("cat LICENSE")
		//process(distributeMetadata)
		def s = run("distributeMetadata")
		println s
	}
	
	def run(script) {
		process(registry[script])
	}
	
	def process(String s) {
		def root = "./"
		def result=""
		s.eachLine{it
			def l = it.split(" ")
			def cmd	= l[0].trim()
			result += "$it\n"
			
			switch (cmd) {
				case "cp":
				case "mv":
				case "rm":
				case "ls":
				case "cat":
//				result += new util.Exec().exec("$it")
				break

				case "gsutil":
				// two ways to implement
				//1. with exec, expedient
				//2. via API, currently Gcp API has no copy local to bucket
//				result += new util.Exec().exec("$it")
				break
				
				case "restart": // the cwva server
				// call restart
//				def token = new Token().getTimeToken()
//				def url = "http://$host/cmd?token=$token&cmd=$cmd"	// &parm=$parm"
//				result += "${new JsonSlurper().parse(url.toURL())}"
				break
				
				default:
				break
			}
			result += "\n"
		}
		result
	}

}
