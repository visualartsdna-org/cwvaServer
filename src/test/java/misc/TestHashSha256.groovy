package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.security.MessageDigest

class TestHashSha256 {

	@Test
	void test() {
		File myJpeg = new File("G:/My Drive/art/projects/3d/femaleT3/TensionT3.jpg")
		try {
			String hash = calculateSHA256(myJpeg)
			println "SHA-256 hash for ${myJpeg.name}:"
			println hash
		} catch (FileNotFoundException e) {
			println e.message
		}
	}

	String calculateSHA256(File file) {
		if (!file.exists() || !file.isFile()) {
			throw new FileNotFoundException("File not found: ${file.path}")
		}

		MessageDigest digest = MessageDigest.getInstance("SHA-256")
		file.eachByte(4096) { buffer, bytesRead ->
			digest.update(buffer, 0, bytesRead)
		}

		// Convert the resulting byte array to a hexadecimal string
		return digest.digest().encodeHex().toString()
	}

}
