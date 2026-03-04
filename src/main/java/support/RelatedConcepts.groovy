package support

import groovy.json.JsonOutput
import org.apache.jena.rdf.model.Model
import rdf.JenaUtilities
import rdf.Prefixes

/**
 * RelatedConcepts - Provides JSON API for the static Related Concepts HTML page.
 * 
 * This class queries SKOS concept data from the RDF model and returns it as JSON,
 * organized by conceptScheme with broader-narrower hierarchy support.
 * 
 * The static HTML page fetches data from /api/related-concepts and manages all
 * state client-side, eliminating server-side HTML generation.
 * 
 * API Endpoints:
 *   GET /api/related-concepts - Returns all concepts organized by scheme
 *   GET /api/config - Returns server configuration (home URL, etc.)
 *   GET /related-concepts.html - Serves the static HTML page
 * 
 * @version 3.0
 */
class RelatedConcepts {
    
    def prefixes = Prefixes.forQuery
    def ju = new JenaUtilities()
    
    // Cache for concept data to avoid repeated queries
    private Map cachedData = null
    private long cacheTimestamp = 0
    private static final long CACHE_TTL = 300000 // 5 minutes
    
    RelatedConcepts() {
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
            version: "3.0"
        ]
    }
    
    /**
     * Handle HTTP requests - route to appropriate handler
     */
//    def handleRequest(String path, Map params) {
//        switch (path) {
//            case "/api/related-concepts":
//                return handleConceptsApi()
//            case "/api/config":
//                return handleConfigApi()
//            case "/related-concepts.html":
//            case "/related.entry":
//            case "/related":
//                return handleHtmlPage()
//            default:
//                return [status: 404, body: "Not found"]
//        }
//    }
    
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
        // In production, this would serve the static HTML file
        // The HTML file should be placed in the appropriate static resources directory
        [
            status: 200,
            contentType: "text/html",
            body: getHtmlContent()
        ]
    }
    
    /**
     * Get all concepts data organized by scheme
     * Uses caching to improve performance
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
     * In production, this would read from a file
     */
    String getHtmlContent() {
		def dir = cwva.Server.getInstance().cfg.dir
		new File("$dir/html/related-concepts.html").text
    }
}
