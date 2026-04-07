package support

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes

/**
 * AnnotateWork - Provides JSON API for the static Annotate Work HTML page.
 * 
 * This class queries SKOS concept data from the RDF model and returns it as JSON,
 * organized by conceptScheme with broader-narrower hierarchy support.
 * It also handles submission of work URI + selected concept CURIs to link
 * annotations directly to a work.
 * 
 * The static HTML page fetches data from /api/related-concepts and manages all
 * state client-side, eliminating server-side HTML generation.
 * 
 * API Endpoints:
 *   GET  /api/related-concepts - Returns all concepts organized by scheme
 *   GET  /api/config            - Returns server configuration (home URL, etc.)
 *   GET  /annotateWork.html     - Serves the static HTML page
 *   POST /annotateWork          - Accepts { work, related } JSON and links concepts to work
 * 
 * @version 1.0
 */
class AnnotateWork {
    
    def prefixes = Prefixes.forQuery
    def ju = new JenaUtilities()
    
    // Cache for concept data to avoid repeated queries
    private Map cachedData = null
    private long cacheTimestamp = 0
    private static final long CACHE_TTL = 300000 // 5 minutes
    
    AnnotateWork() {
    }
    
    /**
     * Get the RDF model containing concept definitions
     */
    Model getConceptModel() {
        cwva.Server.getInstance().dbm.vocab
    }
    
    /**
     * Get server configuration for client
     */
    Map getConfig() {
        [
            host: cwva.Server.getInstance().cfg.host,
            version: "1.0"
        ]
    }
    
    /**
     * Handle /api/related-concepts - Returns JSON with all concepts
     */
    def handleConceptsApi() {
        def data = getConceptsData()
        [
            status: 200,
            contentType: "application/json",
            body: JsonOutput.toJson(data)
        ]
    }
    
    /**
     * Handle /api/config - Returns server configuration
     */
    def handleConfigApi() {
        [
            status: 200,
            contentType: "application/json",
            body: JsonOutput.toJson(getConfig())
        ]
    }
    
    /**
     * Handle HTML page request - serve static file
     */
    def handleHtmlPage() {
        [
            status: 200,
            contentType: "text/html",
            body: getHtmlContent()
        ]
    }

    /**
     * Handle POST /annotateWork
     * 
     * Expects a JSON body: { "work": "work:abcd", "related": ["the:tag", "the:draggingPaint", ...] }
     * Links the provided concept CURIs to the given work URI.
     *
     * @param requestBody  the raw request body string (JSON)
     * @return a response map with status and body
     */
    def handleAnnotateWork(String requestBody) {
        def parsed
        try {
            parsed = new JsonSlurper().parseText(requestBody)
        } catch (Exception e) {
            return [
                status: 400,
                contentType: "application/json",
                body: JsonOutput.toJson([error: "Invalid JSON: ${e.message}"])
            ]
        }

        def workUri = parsed?.work?.toString()?.trim()
        def related = parsed?.related

        if (!workUri) {
            return [
                status: 400,
                contentType: "application/json",
                body: JsonOutput.toJson([error: "Missing 'work' field"])
            ]
        }

        if (!related || !(related instanceof List) || related.isEmpty()) {
            return [
                status: 400,
                contentType: "application/json",
                body: JsonOutput.toJson([error: "Missing or empty 'related' field"])
            ]
        }

        def curis = related.collect { it.toString().trim() }.findAll { it.startsWith("the:") }

        if (curis.isEmpty()) {
            return [
                status: 400,
                contentType: "application/json",
                body: JsonOutput.toJson([error: "No valid thesaurus CURIs found in 'related'"])
            ]
        }

        try {
            linkConceptsToWork(workUri, curis, cwva.Server.getInstance().dbm.rdfs)
        } catch (Exception e) {
            return [
                status: 500,
                contentType: "application/json",
                body: JsonOutput.toJson([error: "Failed to annotate work: ${e.message}"])
            ]
        }

        [
            status: 200,
            contentType: "application/json",
            body: JsonOutput.toJson([
                success: true,
                work: workUri,
                linked: curis
            ])
        ]
    }

    /**
     * Link concept CURIs to a work URI in the RDF model.
     * Override or extend this method to match your triple-store write strategy.
     *
     * @param workUri  the CURI or full URI of the work (e.g. "work:abcd")
     * @param curis    list of thesaurus CURIs (e.g. ["the:tag", "the:draggingPaint"])
     */
    def linkConceptsToWork(String workUri, List<String> curis, rdfs) {

		def model = ju.newModel()
		curis.each{cpt->
			def query = """
CONSTRUCT {
  ?w ?op ?cpt .
} WHERE {
  BIND ($workUri AS ?w)
  BIND ($cpt AS ?cpt)
  
  {
    ?cpt a ?rt .
    FILTER (?rt NOT IN (skos:Concept, rdfs:Resource))
    ?op rdfs:range ?rt .
    ?op a owl:ObjectProperty .
    
    FILTER NOT EXISTS {
      ?rt2 rdfs:subClassOf+ ?rt .
      ?cpt a ?rt2 .
      FILTER (?rt2 != ?rt)
      ?op2 rdfs:range ?rt2 .
    }
  }
  UNION
  {
    ?cpt skos:broader+ ?rtc .
    ?rtc a ?rt .
    FILTER (?rt NOT IN (skos:Concept, rdfs:Resource))
    ?op rdfs:range ?rt .
    ?op a owl:ObjectProperty .
    
    FILTER NOT EXISTS {
      ?rt2 rdfs:subClassOf+ ?rt .
      ?rtc a ?rt2 .
      FILTER (?rt2 != ?rt)
      ?op2 rdfs:range ?rt2 .
    }
    
    FILTER NOT EXISTS {
      ?cpt skos:broader+ ?rtc2 .
      ?rtc2 skos:broader+ ?rtc .
      ?rtc2 a ?rt .
    }
  }
  
  # Final filter: exclude if there's a more specific property from either branch
  FILTER NOT EXISTS {
    {
      ?cpt a ?rtX .
      ?opX rdfs:range ?rtX .
      ?rtX rdfs:subClassOf+ ?rt .
      FILTER (?rtX != ?rt)
    }
    UNION
    {
      ?cpt skos:broader+ ?rtcX .
      ?rtcX a ?rtX .
      ?opX rdfs:range ?rtX .
      ?rtX rdfs:subClassOf+ ?rt .
      FILTER (?rtX != ?rt)
    }
  }
}
	"""
			//println query
			def m= ju.queryExecConstruct(
				rdfs,
				prefixes,query)
			//println "${lm[0].w} ${lm[0].op} ${lm[0].cpt}"
			//println "$cpt, ${m.size()}"
			model.add m
		}
		//println ju.saveModelString(mod,"ttl")
		
		// try to get work from DB
		// add in anno model
		
		def guid = workUri.substring(5)
		def wmod = ju.queryDescribe(
			rdfs,
				prefixes,"""
			describe $workUri
""")
		// if no work in DB
		// look in /stage/data for incipient work
		// if exists load work
		// add in anno model
		if (wmod.size()==0) {
			if (new File("/stage/data/${guid}.ttl").exists())
				wmod = ju.loadFiles("/stage/data/${guid}.ttl")
			if (wmod.size()==0) {
			// else throw error
			// no such work
	            throw new Exception("No existing ${workUri}.ttl")
				}
		}
		
		
		// Intended for adding attributes
		// to work.  For editing, 
		// handle manually
		
		
		// write to /stage/data as work
		wmod.add model
		ju.saveModelFile(wmod,"/stage/data/${guid}.ttl","ttl")
		
    }
    
    /**
     * Get all concepts data organized by scheme.
     * Uses caching to improve performance.
     */
    Map getConceptsData() {
        def now = System.currentTimeMillis()
        
        // Return cached data if still valid
        if (cachedData != null && (now - cacheTimestamp) < CACHE_TTL) {
            return cachedData
        }
        
        def schemes = []
        
        // Query all concept schemes
        def schemeList = queryConceptSchemes()
        
        schemeList.each { scheme ->
            def schemeConcepts = queryConceptsByScheme(scheme.uri)
            if (schemeConcepts) {
                schemes << [
                    name: scheme.curi,
                    label: scheme.label,
                    concepts: schemeConcepts
                ]
            }
        }
        
        cachedData = [schemes: schemes]
        cacheTimestamp = now
        
        return cachedData
    }
    
    /**
     * Query all concept schemes from the model
     */
    List queryConceptSchemes() {
        def m = getConceptModel()
        ju.queryListMap1(m, prefixes, """
            SELECT DISTINCT ?s ?label WHERE {
                ?s a skos:ConceptScheme ;
                   rdfs:label ?label .
			filter (?s in ( 
				the:paintingTerms,
				the:tensegrityTerms,
				the:WatercolorPaintScheme,
				the:watercolorPainting,
				the:DigitalFineArtProduction,
				the:WatercolorCriticismScheme
				))
            } ORDER BY ?label
        """).collect { row ->
            [
                uri: row.s.replaceAll(/[<>]/, ""),
                curi: row.s.replaceAll(/[<>]/, "")
                          .replaceAll("http://visualartsdna.org/thesaurus/", "the:"),
                label: row.label
            ]
        }
    }
    
    /**
     * Query concepts belonging to a specific concept scheme
     */
    List queryConceptsByScheme(String schemeUri) {
        def m = getConceptModel()
        ju.queryListMap1(m, prefixes, """
            SELECT DISTINCT ?s ?label ?alt ?broader WHERE {
                ?s a skos:Concept ;
                   rdfs:label ?label ;
                   skos:inScheme <$schemeUri> .
                OPTIONAL {
                    ?s schema:brand/skos:altLabel ?alt .
                }
                OPTIONAL {
                    ?s skos:broader ?broader .
                }
            } ORDER BY ?label
        """).collect { row ->
            def concept = [
                curi: row.s.replaceAll(/[<>]/, "")
                          .replaceAll("http://visualartsdna.org/thesaurus/", "the:"),
                label: row.label
            ]
            if (row.alt) {
                concept.brand = row.alt
            }
            if (row.broader) {
                concept.broader = row.broader.replaceAll(/[<>]/, "")
                                            .replaceAll("http://visualartsdna.org/thesaurus/", "the:")
            }
            concept
        }
    }
    
    /**
     * Query concepts by broader concept type (legacy method)
     */
    List queryConcepts(String broaderType) {
        def m = getConceptModel()
        ju.queryListMap1(m, prefixes, """
            SELECT DISTINCT ?s ?label ?alt ?broader WHERE {
                ?s a skos:Concept ;
                   rdfs:label ?label ;
                   skos:broader ?cp .
                OPTIONAL {
                    ?s schema:brand/skos:altLabel ?alt .
                }
                OPTIONAL {
                    ?s skos:broader ?broader .
                }
                FILTER (?cp IN ($broaderType))
            } ORDER BY ?label
        """).collect { row ->
            def concept = [
                curi: row.s.replaceAll(/[<>]/, "")
                          .replaceAll("http://visualartsdna.org/thesaurus/", "the:"),
                label: row.label
            ]
            if (row.alt) {
                concept.brand = row.alt
            }
            if (row.broader) {
                concept.broader = row.broader.replaceAll(/[<>]/, "")
                                            .replaceAll("http://visualartsdna.org/thesaurus/", "the:")
            }
            concept
        }
    }
    
    /**
     * Clear the cache (useful for testing or when data changes)
     */
    void clearCache() {
        cachedData = null
        cacheTimestamp = 0
    }
    
    /**
     * Get the static HTML content
     */
    String getHtmlContent() {
        def dir = cwva.Server.getInstance().cfg.dir
        new File("$dir/html/annotateWork.html").text
    }
}
