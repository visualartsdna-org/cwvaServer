package drawing

import static org.junit.jupiter.api.Assertions.*

import rdf.JenaUtils
import org.junit.jupiter.api.Test
import util.Args
import util.Tmp

class Main {

	def tmp = new Tmp()
	/**
	 * Run an L-System
	 * @param args
	 */
	public static void main(String[] args){

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
		def qgdrive = new QueryGraphDriverLocal()

		if (!ruleFile) 
			ruleFile = Tmp.getTemp("rule",".json")

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
		Tmp.delTemp(ruleFile)
	}
}
