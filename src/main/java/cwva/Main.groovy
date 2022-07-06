package cwva
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
cwva.Server -cfg cwvaServer.rson
"""
			return
		}
		def cfg = map["cfg"]
		//assert cfg , "no cfg"
		if (cfg==null) {
			new Server().startJetty()
		}
		else {
			def cfgMap = Rson.load(cfg)
			new Server(cfgMap).startJetty()
		}
		
		def cfgMap = Rson.load(cfg)
		new Server(cfgMap).startJetty()
	
	}
}
