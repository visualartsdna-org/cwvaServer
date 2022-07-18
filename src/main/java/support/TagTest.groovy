package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities

class TagTest {

// TODO: <dt> tag, https://www.w3schools.com/tags/tag_dt.asp
	def ju = new JenaUtilities()
	
	@Test
	void test() {
		def html = "/temp/junk/tag.html"
		def vocab = "/temp/git/cwva/ttl/data/vocab"
		def data = "/temp/git/cwva/ttl/data"
		
		def s = new TagModel().process(data,vocab)
		new File(html).text = s
	}
	
	
	

}
