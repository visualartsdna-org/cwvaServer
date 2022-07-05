package drawing

import static org.junit.jupiter.api.Assertions.*

import rdf.JenaUtils
import org.junit.jupiter.api.Test
import util.Args

class Main {

	@Test
	void testQuery() {
		//draw("basic",
		//draw("physical",
		draw("NFT",
			"vad:Watercolor, vad:Drawing", 
			"http://visualartsdna.org/data", 
			null, 
			null, 
			"C:/stage/march2022/node/ttl/art.ttl", 
			null, 
			"lsys.jpg", 
			null)
	}

	@Test
	void testInst() {
		draw("inst",
			"vad:Watercolor", 
			"http://visualartsdna.org/data", 
			null, 
			null, 
			"C:/stage/march2022/node/ttl/art.ttl", 
			null, 
			"lsys.jpg", 
			null)
	}

	//-ruleFile lsys.json -imgFile lsys.jpg
	@Test
	void testOnto() {
		draw("onto",
			null, 
			"http://visualartsdna.org/data", 
			"owl:Thing", 
			null, 
			"C:/stage/plannedSeptember/node/ttl/cwva.ttl", 
			null, 
			"lsys.jpg", 
			null)
	}

	/**
	 * Run an L-System
	 * @param args
	 */
	public static void main(String[] args){
		/*
		 -scope inst -path http://visualartsdna.org/data -inFile C:/stage/march2022/node/ttl/art.ttl -ruleFile lsys.json -imgFile lsys.jpg
		 -scope onto -path http://visualartsdna.org/data -inFile C:/stage/plannedSeptember/node/ttl/cwva.ttl -ruleFile lsys.json -imgFile lsys.jpg -base owl:Thing
		 -scope onto -path http://mc.movielabs.com/omc -inFile "C:/temp/CreativeWorksOnt/omc_v1.1.ttl -ruleFile lsys.json -imgFile lsys.jpg -base owl:Thing
		 -scope onto -path https://spec.edmcouncil.org/fibo/ontology/LOAN/AllLOAN/AllLOAN.rdf -inFile https://spec.edmcouncil.org/fibo/ontology/LOAN/AllLOAN/AllLOAN.rdf -ruleFile lsys.json -imgFile lsys.jpg -base owl:Thing
		 
		 -scope basic -types "vad:Drawing" -path http://visualartsdna.org/data -inFile C:/stage/march2022/node/ttl/art.ttl -ruleFile lsys.json -imgFile lsys.jpg
		 */

		def map = Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, 
"""
			return
		}
		def scope = map["scope"] ? map["scope"].toLowerCase() : "inst"
		def path = map["path"]
		def base = map["base"]
		def inFile = map["inFile"]
		def ruleFile = map["ruleFile"]
		def imgFile = map["imgFile"]
		def ontoPath = map["ontoPath"]
		def query = map["query"]
		def types = map["types"]

		//assert path , "no path"
		assert inFile , "no inFile"
		//assert imgFile , "no imgFile"
		//assert ruleFile , "no ruleFile"
		
		new Main().draw(scope, types, path, base, ruleFile, inFile, ontoPath, imgFile, query)
	}
	
	def draw(scope, types, path, base, ruleFile, inFile, ontoPath, imgFile, query) {
		
		def dgdrive = new DataGraphDriver()
		def qgdrive = new QueryGraphDriver()

		if (!ruleFile) 
			ruleFile = dgdrive.getTemp("rule",".json").getAbsolutePath()

		switch(scope) {
			case "onto":
				if (!base) {
					def m = new JenaUtils().loadOntImports(inFile)
					base = new JenaUtils().findAllRootsList(m)
				}
				dgdrive.translate(inFile,ruleFile,ontoPath,base,8500)
				dgdrive.driver(ruleFile,imgFile,0.02)
				break

			case "inst":
				dgdrive.translate(inFile,ruleFile,path,5000)
				dgdrive.driver(ruleFile,imgFile,0.02)
				break

			case "query":
				qgdrive.translate(inFile,ruleFile,query,6000)
				dgdrive.driver(ruleFile,imgFile,0.02)
				break

			case "basic":
				query = qgdrive.driverSelectType(types)
				qgdrive.translate(inFile,ruleFile,query,6000)
				dgdrive.driver(ruleFile,imgFile,0.02)
				break;

			case "physical":
				query = qgdrive.driverSelectTypePhysical(types)
				qgdrive.translate(inFile,ruleFile,query,6000)
				dgdrive.driver(ruleFile,imgFile,0.02)
				break;

			case "nft":
				query = qgdrive.driverSelectTypeNFT(types)
				qgdrive.translate(inFile,ruleFile,query,6000)
				dgdrive.driver(ruleFile,imgFile,0.02)
				break;

//			case "all": // not construct or describe
//				query = qgdrive.driverAll()
//				qgdrive.translate(inFile,ruleFile,query,6000)
//				dgdrive.driver(ruleFile,imgFile,0.02)
//				break;
//
			default:
				println "$scope is not a valid scope"
				return
		}
	}
}
