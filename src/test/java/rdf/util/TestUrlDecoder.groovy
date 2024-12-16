package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

class TestUrlDecoder {

	@Test
	void test() {
		def enc = "format=CSV&submit=Execute&query=select+*+%7B%0D%0A+%3Fs+a%09schema%3AOrganization+%0D%0A%7D%0D%0A&queries=select+*+%7B%0D%0A+%3Fs+a%09schema%3AOrganization+%0D%0A%7D%0D%0A"
		//def enc = "select+*+%7B%0D%0A+%3Fs+a%09schema%3AOrganization+%0D%0A%7D%0D%0A&queries=select+*+%7B%0D%0A+%3Fs+a%09schema%3AOrganization+%0D%0A%7D%0D%0A"
		def dec = URLDecoder.decode(enc,StandardCharsets.UTF_8.toString())
		println dec
	}

}
