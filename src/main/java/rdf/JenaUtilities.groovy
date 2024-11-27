package rdf
import groovy.io.FileType
import groovy.json.JsonSlurper
import org.apache.jena.rdf.model.*
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
				model.add( loadFile(""+it) )
			}

		} else {

			model = loadFileModelFilespec(spec)
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


}
