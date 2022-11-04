package util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class BuildProperties {

	@Test
	void test() {
		println getProperties("build.version")
	}

	@Test
	void test2() {
		println getProperties()
	}

	static def propFile = "version.properties"
	static def getProperties() {
		getProperties("build.version")
	}
	static def getProperties(s) {
		def version = ""
		try {
			InputStream input = BuildProperties.class.getClassLoader().getResourceAsStream(propFile)
			Properties prop = new Properties();

			prop.load(input);
			version =  prop.getProperty(s)

		} catch (IOException ex) {
			
		}
		version
	}
}
