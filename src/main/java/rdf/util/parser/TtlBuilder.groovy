package rdf.util.parser

import groovy.json.JsonSlurper
import rdf.JenaUtils

class TtlBuilder {

	// properties reference an object:
	// string, e.g., [annotation:abc] becomes {tko:annotation "abc"}
	// a single FQN URI, e.g., <http://visualartsdna.org/work/bdb05de5...>
	// one or more Qnames, e.g., [member:tko:abc, tko:def]
	//	becomes {skos:member tko:abc, tko:def}
	static def nsMap = [
		identifier:"schema",
		altLabel            : "skos",
		broadMatch          : "skos",
		broader             : "skos",
		broaderTransitive   : "skos",
		changeNote          : "skos",
		closeMatch          : "skos",
		definition          : "skos",
		editorialNote       : "skos",
		exactMatch          : "skos",
		example             : "skos",
		hasTopConcept       : "skos",
		hiddenLabel         : "skos",
		historyNote         : "skos",
		inScheme            : "skos",
		mappingRelation     : "skos",
		member              : "skos",
		memberList          : "skos",
		narrowMatch         : "skos",
		narrower            : "skos",
		narrowerTransitive  : "skos",
		notation            : "skos",
		note                : "skos",
		prefLabel           : "skos",
		related             : "skos",
		relatedMatch        : "skos",
		scopeNote           : "skos",
		semanticRelation    : "skos",
		topConceptOf        : "skos",
		type	: "rdf",
		seeAlso : "rdfs",
		"annotation":"tko",
		"annotation.publishProperties":"tko"
	]

	def process(m0) {
		process(m0,[:],null)
	}

	// 				${nsMap[k2]}:$k2 ${v2.startsWith("<")?v2:"\"$v2\""} ;
	def process(m0,labelsMap,ttlFile) {

		def ju = new JenaUtils()
		def sb = new StringBuilder()
		sb.append  """
@prefix tko: <http://visualartsdna.org/takeout#> .
@prefix work:	<http://visualartsdna.org/work/> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix schema: <https://schema.org/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .

tko:KeepConceptScheme
  rdf:type owl:Class ;
  rdfs:label "A KeepConceptScheme collects concepts related to one G-Keep note" ;
  rdfs:subClassOf skos:ConceptScheme ;
.

tko:KeepCollection
  rdf:type owl:Class ;
  rdfs:label "A KeepCollection collects concepts related to one G-Keep label.  E.g., the drawings collection" ;
  rdfs:subClassOf skos:Collection ;
.

"""
		m0.each{k1,c->
			def concepts = []
			
			// topConcept
			def v1=c.textContent
			def guid = UUID.randomUUID()

			def uri = util.Text.camelCase(k1.replaceAll(/[^A-Za-z_0-9]/,""))

			// parse the text for annotations
			def m =new Keep().parseKeepConcepts(v1)

			sb.append """
			tko:$uri
				skos:prefLabel "$k1" ;
				skos:definition \"\"\"${m.topConcept.text}\"\"\" ;
"""
			concepts += "tko:$uri"
			
			// top concept annotations
			m.topConcept.ann.each{k2,v2->
				if (v2 == "rdfs:seeAlso") {
					sb.append """
				rdfs:seeAlso <$k2> ;
"""
				} else
					sb.append """
				${nsMap[k2]}:$k2 ${v2=~/^<http[s]?:\/\/[A-Za-z_0-9\-\.\/]+>$|^[a-z]+:.*$/?v2:"\"$v2\""} ;
"""
			}
			// topConcept scheme
			if (!m.topConcept.ann.containsKey("type")) {
				sb.append """
				a skos:Concept ;
				skos:inScheme tko:$guid ;
				.
"""
			}
			
				// create KeepConceptScheme
				sb.append  """
			tko:$guid
				a tko:KeepConceptScheme ;
				skos:prefLabel "$k1 KeepConceptScheme" ;
				skos:hasTopConcept tko:$uri ;
"""
			// weblinks
			if (c.annotations) c.annotations.each {
				if (it.source == "WEBLINK" && it.title== "Google Photos") {
					sb.append """
					schema:image <${it.url}> ;
"""
			}
		}
		// attachments
			if (c.attachments) c.attachments.each {
				if (it.mimetype == "image/jpeg" || it.mimetype == "image/png") {
					sb.append """
					schema:image <http://visualartsdna.org/images/${it.filePath}> ;
"""
				}
			}
			// timestamps
			sb.append """
				schema:datePublished "${Util.now()}"^^xs:dateTime ;
				schema:dateCreated "${Util.getInstantFromMicros(c.createdTimestampUsec)}"^^xs:dateTime ;
				schema:dateModified "${Util.getInstantFromMicros(c.userEditedTimestampUsec)}"^^xs:dateTime ;
				.
"""

		// subConcepts
		try {
			m.each{k,v->
				if (k=="topConcept") return
					//println "$k\n"
					sb.append """
			tko:${util.Text.camelCase(k)}
				a skos:Concept ;
				skos:inScheme tko:$guid ;
"""
				concepts += "tko:${util.Text.camelCase(k)}"
					
				// internet links
				if (v.containsKey("ann"))
					v.ann.each{k2,v2->
						if (v2 == "rdfs:seeAlso") {
							sb.append """
				rdfs:seeAlso <$k2> ;
"""
						} else
							sb.append """
				${nsMap[k2]}:$k2 ${v2.startsWith("<")?v2:"\"$v2\""} ;
"""

				}
				// definition
			sb.append """
				skos:definition \"\"\"${v.text}\"\"\" ;
				.
"""
			}
		} catch (Exception e) {
			println e
		}
		
		labelsMap.each{k,v->
			def ll = c.labels.findAll{it->
				k == it.name
			}
			ll.each{
				v.addAll concepts
			}
		}
	
	}
			
	labelsMap.each{k,v->
		if (!v.isEmpty())
		sb.append """
		tko:${UUID.randomUUID()}
			a tko:KeepCollection ;
			skos:prefLabel "$k" ;
"""
		v.each{
			sb.append """
			skos:member $it ;
"""
		}
		if (!v.isEmpty()) sb.append """
			.
"""
	}
	
	try {
		def model = ju.saveStringModel(""+sb, "ttl")
		println "model size=${model.size()}"
		if (ttlFile) ju.saveModelFile(model,"${ttlFile}.ttl", "ttl")
	
	} catch (Exception ex) {
		println """
	$sb
	$ex
	"""
	}

}


}
