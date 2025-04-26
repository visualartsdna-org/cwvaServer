package util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class Secrets {

	@Test
	void test() {
		fail("Not yet implemented")
	}

	static def get(param) {
		try {
			def home = System.getProperty("user.home")
			def cfg = Rson.load("$home/.secrets.rson")
			return cfg.secrets[param]
		} catch (Exception e) {
			println "Error accessing secrets, $e"
		}

	}
}
