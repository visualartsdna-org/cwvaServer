package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import net.glxn.qrgen.javase.*
import net.glxn.qrgen.core.image.ImageType

// https://github.com/kenglxn/QRGen
class QRCode {


	def ns = "http://visualartsdna.org/work"
	@Test
	public void test() {
		def dir = "C:/stage/temp"
		def guids=[
			"bdb05de5-0e82-4d05-9b13-a25063cf2cb9",
			"9da4bdb5-cd9d-42c1-b5f5-97066967b2d2",
			"21adf909-3eec-4771-9cb5-9368484b76fa",
		].each{guid->
			qrcode(guid,dir)
		}
	}
	

	def qrcode(guid,dir) {
		def qrcName = "qrc_${guid}.jpg"
		def qrcFile = new File("$dir/$qrcName")
		if (!qrcFile.exists()) {
			File file = net.glxn.qrgen.javase.QRCode.from("$ns/$guid").to(ImageType.JPG).file()
			def newFile = new File("$dir/$qrcName")
			newFile << file.bytes
		}

	}

}
