package util
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile
import groovy.io.FileType

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class TestZip {

	@Test
	void testLatest() {

		def downloads = "C:/Users/ricks/Downloads"
		def tgt = getLatestFileType(downloads,".zip")
		println tgt
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

	@Test
	void test() {

		def file = "C:/temp/Takeout/Larry/takeout-20240519T191905Z-001.zip"
		def tgt = "C:/temp/Takeout/Larry/Takeout/test"
		unzipFile(new File(file),tgt)
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
	
	@Test
	void testRm() {

		def tgt = "C:/temp/Takeout/Larry/Takeout"
		FileUtils.deleteDirectory(new File(tgt))
		def file = "C:/temp/Takeout/Larry/takeout-20240519T191905Z-001.zip"
		new File(file).delete()
	}

	private cleanupFolder(folder) {
		FileUtils.deleteDirectory(new File(folder))
	}
}
