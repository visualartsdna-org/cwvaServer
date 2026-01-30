package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import  org.apache.commons.text.similarity.*

class TestLoadConceptMatch {
	def vm
	def ju = new JenaUtilities()
	def vocab = "/stage/server/cwvaContent/ttl/vocab"
//	def cfgTHold = 0.85
	def cfgTHold = 0.55
	
//	LoadConceptMatch(vm){
//		this.vm = vm
//	}
	
	
	// search for selected list of conepts
	@Test
	void test() {
		def cptList = [
			"Watercolor technical",
			"WATERCOLOR technical",
			"facture",
			"factur",
			"factures",
			"watercolor",
			"WATERCOLOR",
			"watercolor tools",

			]

		loadVocab()
		
		println formatHeader()
		cptList.each{concept->
//			println "findMatchingConcepts"
			def lm = findMatchingConcepts(concept)
//			println concept
			println formatDups(concept, lm)
		}
	}
	
	def formatHeader() {
		"""	Duplicate Detection Threshold = $cfgTHold
	Less Significant  =======>
"""
			
	}
	
	def formatDups(concept, lm) {
		def sb = new StringBuilder()
		
		if (lm.isEmpty()) {
			sb.append """
$concept is unique
"""
		} else
		sb.append """$concept
"""
		lm.each{k,v->
			def score = v.score
			def incr = (score * 10) as int
			def tab = ""
			for (int i=10;i>incr;i--) {
				tab += "\t"
			}
			sb.append """$tab${v.label}
"""
		}
		
		""+sb
	}
	
	// search for every concept in db
	@Test
	void test3() {

		loadVocab()
		
		def lm = ju.queryListMap1(vm, rdf.Prefixes.forQuery, """
        select ?o {
            ?s rdfs:label ?o
        } order by ?o
    """)

		lm.each{concept->
			println "findMatchingConcepts"
			println concept.o
			def lmat = findMatchingConcepts(concept.o)
			println lmat
		}

	}
	
	@Test
	void test1() {
		def concept = "Watercolor technical"

		loadVocab()
		println "findMatchingConcepts"
		def lm = findMatchingConcepts(concept)
		println concept
		println lm

	}
	
	def conceptExistsExact(cpt) {
		def lm = ju.queryListMap1(vm,rdf.Prefixes.forQuery,"""
		select * {
			?s rdfs:label "$cpt"
		}
""")
		lm
	}
	
	def conceptExistsRegex(cpt) {
		def lm = ju.queryListMap1(vm,rdf.Prefixes.forQuery,"""
		select * {
			?s rdfs:label ?o
			filter (regex(?o, "$cpt", "i") 
			|| regex("$cpt", ?o, "i") )
		}
""")
//		def l = []
//		lm.each{
//			l += it.o
//		}
//		l
	}
	

	// Composite Approach with Ranking
	def findMatchingConcepts(cpt) {
		def results = [:]
		
		// Exact match (highest confidence)
		conceptExistsExact(cpt).each { 
			results[it.s] = [score: 1.0, type: 'exact', label: cpt] 
			}
		
		// Regex match (low confidence)
//		conceptExistsRegex(cpt).each { results[it.s] = [score: 0.4, type: 'regex', label: it.o] }
		
		// Normalized match
		conceptExistsNormalized(cpt).each{cen->
			if (!results[cen.s] || results[cen.s].score < 0.9)
				results[cen.s] = [score: 0.9, type: 'normalized', label: cen.o]
		}
		
		// Fuzzy match
		conceptExistsFuzzy(cpt).each{cen->
			if (!results[cen.s] || results[cen.s].score < cen.similarity)
				results[cen.s] = [score: cen.similarity * 0.8, type: 'fuzzy', label: cen.o]
		}
		
		// Token-based Matching
		conceptExistsTokenOverlap(cpt).each{cen->
			if (!results[cen.s] || results[cen.s].score < cen.similarity)
				results[cen.s] = [score: 0.8, type: 'token', label: cen.o]
		}

		results.sort { -it.value.score }
	}
	
	@Test
	void test0() {
		def concept = "Watercolor technical"
		//println normalizeLabel("This is a bunch of junk")
		loadVocab()
		
		println "conceptExistsNormalized"
		def lm = conceptExistsNormalized(concept)
		println lm
		
		println "conceptExistsFuzzy"
		lm = conceptExistsFuzzy(concept)
		println lm
		
		println "conceptExistsTokenOverlap"
		lm = conceptExistsTokenOverlap(concept)
		println lm
	}
	
	
	// Token-based Matching
	def conceptExistsTokenOverlap(cpt, minOverlap = 0.7) {
		def inputTokens = tokenize(cpt)
		
		def lm = ju.queryListMap1(vm, rdf.Prefixes.forQuery, """
        select ?s ?o {
            ?s rdfs:label ?o
        }
    """)
		
		lm.findAll { entry ->
			def labelTokens = tokenize(entry.o.toString())
			def intersection = inputTokens.intersect(labelTokens)
			def union = (inputTokens + labelTokens).unique()
			(intersection.size() / union.size()) >= minOverlap  // Jaccard similarity
		}
	}
	
	def tokenize(text) {
		text.toLowerCase()
			.replaceAll(/[^a-z0-9\s]/, ' ')
			.split(/\s+/)
			.findAll { it.length() > 2 }  // filter stopwords by length or use a list
			.toSet()
	}

	// Levenshtein/Edit Distance (for fuzzy matching)
	def conceptExistsFuzzy(cpt, threshold = cfgTHold) {
		// Get all labels, then compute similarity client-side
		def lm = ju.queryListMap1(vm, rdf.Prefixes.forQuery, """
        select ?s ?o {
            ?s rdfs:label ?o
        }
    """)
		
		lm.findAll { entry ->
			def sim = similarity(cpt.toLowerCase(), entry.o.toString().toLowerCase())
			if (sim >= threshold) {
				entry.similarity = sim
			}
		}
	}
	
	def lev = LevenshteinDistance.getDefaultInstance()
	
	def similarity(s1, s2) {
		def longer = s1.length() >= s2.length() ? s1 : s2
		def shorter = s1.length() < s2.length() ? s1 : s2
		if (longer.length() == 0) return 1.0
		(longer.length() - lev.apply(longer, shorter)) / longer.length()
	}
	
	
	
	
	
	// Normalized Label Matching
	def conceptExistsNormalized(cpt) {
		def normalized = normalizeLabel(cpt)
		def lm = ju.queryListMap1(vm, rdf.Prefixes.forQuery, """
        select ?s ?o {
            ?s rdfs:label ?o
            filter (lcase(replace(str(?o), "[^a-zA-Z0-9]", "")) = "$normalized")
        }
    """)
		lm
	}
	
	def normalizeLabel(label) {
		label.toLowerCase().replaceAll(/[^a-z0-9]/, '')
	}
	
	def loadVocab() {
		if (!vm)
		vm = ju.loadFiles(vocab)
	}
	

}
