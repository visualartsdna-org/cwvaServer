package function
import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import util.*

class Main {

	@Test
	void test() {
		new Server().startJetty()
		
	}


	/**
	 * Run a cwva server
	 * @param args
	 */
	public static void main(String[] args){

		def map = Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, 
function.Server -cfg fcnServer.rson
"""
			return
		}
		def cfg = map["cfg"]
		assert cfg , "no cfg"
		
		def cfgMap = Rson.load(cfg)
		new Server(cfgMap).startJetty()
	
	}
}
