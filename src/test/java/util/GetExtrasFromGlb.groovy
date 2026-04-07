package util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import java.nio.ByteBuffer
import org.junit.jupiter.api.Test
import groovy.json.JsonSlurper
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GetExtrasFromGlb {

	def glb = "G:/My Drive/art/projects/lsys/cwvaTree3.1.0.glb"
	
	@Test
	void test() {

		byte[] bytes = new File(glb).bytes

// GLB Header is 12 bytes: Magic (4), Version (4), Total Length (4)
// The first chunk starts at byte 12.
// Chunk Header: Length (4 bytes), Type (4 bytes: "JSON" is 0x4E4F534A)		
		def gltf = get(bytes)
//		println c.extras
		gltf.nodes.each { node ->
			if (node.extras?.uri) {
				println "Found Node: ${node.name} -> URI: ${node.extras.uri}"
				if (node.translation) {
					println "Position: ${node.translation}"
				}
			}
		}
	
	}
	
	def get(bytes){
	
	ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
	int jsonChunkLength = buffer.getInt(12)
	// The actual JSON string starts at byte 20
	String jsonString = new String(bytes, 20, jsonChunkLength, "UTF-8")
	
	new JsonSlurper().parseText(jsonString)
	
	// Now you can iterate through your nodes
	}

}
