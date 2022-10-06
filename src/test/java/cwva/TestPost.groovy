package cwva

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class TestPost {
	
	def postFile(file,url) {
		def bytes = new File(file).bytes
		def site = new URL(url)

		HttpURLConnection  connection = site.openConnection()
		connection.setRequestMethod("POST")
		connection.setDoOutput(true)
		connection.setRequestProperty("Content-Type", "image/jpeg")
		
		def os = connection.getOutputStream()
			os.withCloseable {
				it << bytes
			}
		connection.getResponseCode()
	}
	
	@Test
	void test() {
		
		def bytes = new File("C:/test/git/lsysFx/f.jpg").bytes
		def url = new URL('http://192.168.1.71:8082/artist/rs')

		
		int respCode = postFile("C:/test/git/lsysFx/f.jpg",
			'http://192.168.1.71:8082/artist/rs')
		println respCode
	}

}
