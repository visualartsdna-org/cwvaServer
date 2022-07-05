package rdf
import groovy.io.FileType
import groovy.json.JsonSlurper
import org.apache.jena.rdf.model.*
import rdf.util.JsonRdfUtil
class JenaUtilities extends JenaUtils {

	/**
	 * Load a model from a dir of files of any model type
	 * type is determined from file extension
	 * or a single file of any model type
	 * @param dirSpec or fileSpec
	 * @return model
	 */
	// TODO: add support for json found at spec
	// conversion to model
	def loadFiles(spec){
		def model = newModel()
		if (new File(spec).isDirectory()) {
			new File(spec).eachFileRecurse(FileType.FILES) {
				//println it
				model.add( loadFile(""+it) )
			}

			//files.each { println it }
		} else {

			model = loadFileModelFilespec(spec)
		}
		model
	}

	/**
	 * Load any model file
	 * Also translates JSON to analogous model file
	 * the subdir of the JSON profides the namespace context
	 * @param spec
	 * @return model
	 */
	def loadFile(spec) {
		def ext = (spec =~ /^.*\.([a-zA-Z-]+)$/)[0][1]
		def model = newModel()

		if (ext in [
					"ttl",
					"rdf",
					"jsonld",
					"json-ld",
					"nt",
					"nq",
					"trig",
					"trix",
					"rt",
					"trdf"
				]) {
			model = loadFileModelFilespec(spec,ext)
//		} else if (ext in ["json"]) {
//			def c
//			try {
//				c = util.Rson.load(spec)
////				c = new JsonSlurper().parse(new File(spec))
//				//c.message.remove("synopses")
//			} catch (Exception ex) {
//				println """
//					$spec
//					$ex
//					"""
//			}
//			if (!c) return model
//			
//			// extract last sub dir for context
//			def dir = (spec =~ /^.*[\/\\]([a-zA-Z]+)[\/\\].*$/)[0][1]
//			def sb = new StringBuilder()
//			sb.append  """
//@prefix $dir: <http://visualartsdna.org/$dir#> .
//@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
//
//"""
//			try {
//				JsonRdfUtil.jsonToTtl(["$dir":[c]], sb, "$dir:")
//				// clean up escapes
//				def s = (""+sb).replaceAll(/([^\\])\\([^tbnrf\\'"])/,/$1\\\\$2/)
//				model = saveStringModel(s, "ttl")
//			} catch (Exception ex) {
//				println """
//					$spec
//					$sb
//					$ex
//					""" 
//				ex.printStackTrace(System.out)
//			}
		}
		model
	}
	
	/**
	 * Save a string representation of RDF
	 * in type (e.g., TTL) format to a model
	 * @param ttldata
	 * @param type
	 * @return model
	 */
	def saveStringModel(ttldata, type){
		Model data = ModelFactory.createDefaultModel();
//		def is = 
//		
//		new BufferedReader(new InputStreamReader(
//		new ByteArrayInputStream(ttldata.getBytes())
//		,"utf-8"))
		def is = new ByteArrayInputStream(
			ttldata.getBytes(
				java.nio.charset.StandardCharsets.UTF_8))
		data.read(is,null,type)
//		//println "size=${data.size()}"
		return data
	}
	

}
