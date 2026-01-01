package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class GenPrompts {
	
	def pre = "Provide a public-facing art criticism of this watercolor"
	def pre2 = """Provide a public-facing art criticism of this 
blender composition digital sculpture viewed in <model-viewer>
"""
	
//15x11 on 300 lb cold press entitled "Cups and Bottle""

	@Test
	void test() {
		new File("/test/prompts2.txt").eachLine{
			def sf = it.split("\t")
			
// Changing of the Guard	2025-12-22T10:52:22	11	15	Fabriano Artistico Grana Fina cold press	Cold press	300
			println """
$pre2 called "${sf[0]}"
"""
		}
	}

	@Test
	void test0() {
		new File("/test/prompts.txt").eachLine{
			def sf = it.split("\t")
			
// Changing of the Guard	2025-12-22T10:52:22	11	15	Fabriano Artistico Grana Fina cold press	Cold press	300
			println """
$pre on ${sf[2]} x ${sf[3]} ${sf[6]}lb ${sf[5]} called "${sf[0]}"
"""
		}
	}

}
