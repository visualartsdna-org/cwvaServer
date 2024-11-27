package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class InterpretationTest {

	@Test
	void test() {
		def tag = "work:da4bccc6-dfd3-4062-ae5b-3756e4eed354"
		def kind = "Gallery Description"
		def label = "Stuff for Fun"
		def s = Interpretation.makeGuid(tag,kind,label)
		println s
	}


}
