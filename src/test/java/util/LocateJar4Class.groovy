package util

import static org.junit.jupiter.api.Assertions.*
import util.*
import org.junit.jupiter.api.Test

class LocateJar4Class {

	@Test
	void test() {
		// locate a jar for a class!!
		println com.google.common.io.ByteStreams.class.getProtectionDomain().getCodeSource().getLocation()
		println rdf.JenaUtils.class.getProtectionDomain().getCodeSource().getLocation()
	}

}
