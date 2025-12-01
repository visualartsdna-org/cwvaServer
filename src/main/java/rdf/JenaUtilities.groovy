package rdf
import groovy.io.FileType
import groovy.json.JsonSlurper
import java.nio.charset.CharacterCodingException
import org.apache.jena.rdf.model.*
import org.apache.jena.riot.RiotException
import rdf.util.JsonRdfUtil
import org.apache.jena.query.*

/**
 * The following section handles RDF list retrieval and creation
supporting the use case of an RDF list containing URIs.
Relies on TTL serialization and regex matching; 
assumes list members are in prefix URI form
TODO: change regex below to handle absolute URIs

 * @author ricks
 *
 */
class JenaUtilities extends JenaUtils {

	
	/**
	 * Get a given data property's value
	 * for each of the URI refs in the RDF list at prop
	 * for the initial list of URIs
	 * @param m model with all data
	 * @param dataProp a property to get like skos:definition
	 * @param prop name of the RDF list property to follow
	 * @param lw initial list of URIs
	 * @return list of dataproperty results in order of nested URIs
	 */
	def getListData(m,dataProp,prop,lw) {
		
		def l= getListDataList(m,prop,lw)
		def rl = []
		def pres = getPrefixes(m)
		l.each{
			def l2 = queryListMap1(m, pres,"""
		select ?d {
			$it $dataProp ?d
		}
""")
			l2.each{
				rl.add(it.d)
			}
		}
		rl
	}
	
	/**
	 * Recursively get nested list of URIs from RDF list
	 * for given propery using list of URIs
	 * @param m model with all data
	 * @param prop name of the RDF list property to follow
	 * @param lw initial list of URIs
	 * @return URIs from nested RDF lists on property
	 */
	def getListDataList(m,prop,lw) {
		def rl = []
		lw.each{
			rl += it
			def l= getList( m,prop,it)
			rl.addAll getListDataList(m,prop,l)
		}
		rl
	}
	
	/**
	 * Canonical one-instance model
	 * to set the ordered list items
	 * in the instance
	 * @param m one instance model
	 * @param property with range rdf:List
	 * @param l replacement list of items for the property
	 * @return a model with the revised instance
	 */
	def setList(m,property,l) {
		def s =  saveModelString(m,"TTL")
		def lrev = ""
		l.each{
			lrev += "$it "
		}
		def s2 = s.replaceAll(/[ \t]*${property}[ \t]+\([A-Za-z0-9_\:\- ]+\)/,
			"""		${property}	( ${lrev.trim()} ) ;\n"""
			)
		saveStringModel(s2,"TTL")
	}

	/**
	 * Multi-instance model
	 * to return the ordered list items
	 * for list of URIs
	 * @param m one instance model
	 * @param property with range rdf:List
	 * @param uri instance to get from the modelmodel
	 * @return a List of the entries in property for the instances in m
	 */
	def getList(Model m,property,List uris) {
		def m2 = newModel()
		def rl = []
		def pres = getPrefixes(m)
		uris.each{uri->
			m2.add queryDescribe(m, pres, """
				describe $uri
	""")
			def l = getList(m2,property)
			rl.addAll l
		}
		rl
	}

	/**
	 * Multi-instance model
	 * to return the ordered list items
	 * for single given URI
	 * @param m one instance model
	 * @param property with range rdf:List
	 * @param uri instance to get from the modelmodel
	 * @return a List of the entries in property for the instance in m
	 */
	def getList(Model m,property,String uri) {
		def pres = getPrefixes(m)
		def m2 = queryDescribe(m, pres, """
			describe $uri
""")
		getList(m2,property)
	}

	/**
	 * Canonical one-instance model
	 * to return the RDF list items
	 * @param m one instance model
	 * @param property with range rdf:List
	 * @return a List of the entries in property for the instance in m
	 */
	def getList(m,property) {
		def s =  saveModelString(m,"TTL")
		
		def match = (s =~ /[ \t]*${property}[ \t]+\(([A-Za-z0-9_\:\- ]+)\)/)
		def ls = match ? match[0][1] : ""
		ls == "" ? [] : ls.trim().split(" ")
	}

	/**
	 * Load selectively and verbosely
	 * Load a model from a dir of files of any model type
	 * type is determined from file extension
	 * or a single file of any model type
	 * @param dirSpec or fileSpec
	 * @return model
	 */
	def loadFiles(spec){
		def model = newModel()
		if (new File(spec).isDirectory()) {
			new File(spec).eachFileRecurse(FileType.FILES) {
				if (!(""+it).toLowerCase().endsWith(".ttl")) return
				println "loading $it"
				model.add( loadTtlFile(it) )
			}

		} else {

			model = loadTtlFile(spec)
		}
		model
	}

	/**
	 * Print a formatted result of the query on the model
	 * @param model
	 * @param prefixes
	 * @param queryString
	 * @return string
	 */
	def queryResultSet(Model model, prefixes, queryString){
		Query query = QueryFactory.create(prefixes + queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model)
		qexec.execSelect() // returns ResultSet
	}

	def loadTtlFile(file){
		try {
		loadJenaModelWithEncodingFallback(new FileInputStream(file))
		} catch (RuntimeException re) {
			throw new RuntimeException("$file, $re")
		}
	}
	

	/**
	 * Loads an Apache Jena Model from an InputStream with a robust encoding fallback.
	 *
	 * This method attempts to load the stream as UTF-8. If it fails, it falls back
	 * to Windows-1252, and if that also fails, it attempts UTF-16. This provides
	 * a comprehensive way to handle different source encodings.
	 *
	 * @param inputStream The InputStream containing the RDF data.
	 * @return A Jena Model loaded with the correctly decoded data, or null if loading fails.
	 */
	def loadJenaModelWithEncodingFallback(InputStream inputStream) {
		if (!inputStream) {
			//println "Input stream is null. Cannot load model."
			return null
		}
			// Read the entire stream into a byte array
			byte[] bytes = inputStream.bytes
			//println new String(bytes,  "UTF-16")
	
		try {
	
			// Attempt 1: UTF-8 (most common)
			//println "Attempting to load model with UTF-8 encoding..."
			def decodedString = new String(bytes, 'UTF-8')
			def model = ModelFactory.createDefaultModel()
			model.read(new StringReader(decodedString), null, "TURTLE")
			//println "Model loaded successfully with UTF-8 encoding."
			return model
	
		} catch (CharacterCodingException|RiotException e) {
			// This catch block handles the case where the input stream is NOT valid UTF-8.
			//println "UTF-8 decoding failed. Falling back to Windows-1252..."
			try {
				// Attempt 2: Windows-1252 (common for legacy files)
				def decodedString = new String(bytes, 'Windows-1252')
				
				def model = ModelFactory.createDefaultModel()
				model.read(new StringReader(decodedString), null, "TURTLE")
				//println "Model loaded successfully with Windows-1252 fallback."
				return model
	
			} catch (CharacterCodingException|RiotException ex) {
				// This catch block handles the case where the input is neither UTF-8 nor Windows-1252.
				//println "Windows-1252 decoding failed. Falling back to UTF-16..."
				try {
					// Attempt 3: UTF-16 (another common encoding)
					def decodedString = new String(bytes, 'UTF-16')
					
					def model = ModelFactory.createDefaultModel()
					model.read(new StringReader(decodedString), null, "TURTLE")
					//println "Model loaded successfully with UTF-16 fallback."
					return model
	
				} catch (Exception finalEx) { // report original exception
						throw new RuntimeException("Loading failed. $e")
				}
			} catch (Exception ex) {
				throw new RuntimeException( "An unexpected error occurred during Windows-1252 fallback.")
			}
		} catch (Exception e) {
			throw new RuntimeException( "An unexpected error occurred during model loading.")
		}
		return null
	}
	
}
