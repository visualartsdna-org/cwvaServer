package services
import services.Svg2Html
import util.Args

class OntoToD3Driver {
	
	/**
	 * Perform the ontology 
	 * rendering in svg/html via dot
	 * @param args
	 */
	public static void main(String[] args){

		def map = Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, services.OntoToDotDriver -ttl ttl -dot dot -svg svg -html html
Perform the ontology rendering in svg/html via dot
"""
			return
		}
		def ttl = map["ttl"]
		assert ttl , "no ttl"
		def html = map["html"]
		assert html , "no html"
		def scope = map["scope"]
		assert scope , "no scope"
		def types = map["types"]
		if (scope != "all") {
			assert types , "no types"
		}

		switch(scope) {
			case "basic":
			InstToD3.driverSelectType(ttl,html,types)
			break;
			
			case "physical":
			InstToD3.driverSelectTypePhysical(ttl,html,types)
			break;
			
			case "NFT":
			InstToD3.driverSelectTypeNFT(ttl,html,types)
			break;
			
			case "all":
			InstToD3.driverAll(ttl,html)
			break;
			
		}
	}


}
