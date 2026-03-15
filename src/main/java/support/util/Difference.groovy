package support.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import util.Tmp

class Difference {

	@Test
	void test() {
		fail("Not yet implemented")
	}

	static def difference(mf1,mf2) {
		def sb = new StringBuilder()
		def ju = new JenaUtilities()
		def text1 = getPartText(mf1)
		def text2 = getPartText(mf2)
		def m1 = ju.saveStringModel(text1,"ttl")
		def m2 = ju.saveStringModel(text2,"ttl")
		def md = m1.difference(m2)
		sb.append """
difference(m), Create a new, independent, model 
containing all the statements 
in this model which are not in another.\n"""
		sb.append "--------------------------------\n"
		sb.append "In m1 not in m2 difference size = ${md.size()}\n"
		sb.append ju.saveModelString(md,"ttl")
		def md2 = m2.difference(m1)
		sb.append "--------------------------------\n"
		sb.append "In m2 not in m1 difference size = ${md2.size()}\n"
		sb.append ju.saveModelString(md2,"ttl")

		""+sb
	}

	static def getPartText(filePart) {
		try {
			if (filePart != null) {
				InputStream inputStream = filePart.inputStream
				inputStream.text
			} else {
				throw new Exception("Part 'modelFileX' not found")
			}
		} catch (Exception e) {
			throw new RuntimeException( e.message)
		}
	}
}
