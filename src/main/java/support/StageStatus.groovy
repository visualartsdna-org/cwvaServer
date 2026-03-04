package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import groovy.json.JsonOutput

class StageStatus {

	def root = "/stage"
	def dirList = [
		"$root/data",
		"$root/conceptQueue",
		"$root/proof",
		"$root/metadata/model",
		"$root/metadata/provenance",
		"$root/metadata/tags",
		"$root/metadata/topics",
		"$root/metadata/vocab",
		]
	
	@Test
	void test() {
		println getStatusAsJson()
		
	}
	
	def getStatusAsJson() {
		def m = getStatus()
		JsonOutput.prettyPrint(JsonOutput.toJson(m))
	}
	
	def getStatus () {
		def map=[:]
		dirList.each{
			def dir = new File("$it")
			// Optional: filter to count only actual files, excluding subdirectories
			map[it] = dir.listFiles().count {
				it.isFile()
				it.name.endsWith(".ttl")
				}
		}
		map
	}

}
