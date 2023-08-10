package rdf.util.parser

import groovy.json.JsonSlurper
import rdf.JenaUtils

class TtlBuilder {
	
	def nsMap = [identifier:"schema",
		definition:"skos",
		scopeNote:"skos",
		historyNote:"skos",
		"annotation.publishProperties":"tko"]
	
	def process(m0) {

		def ju = new JenaUtils()
		def sb = new StringBuilder()
		sb.append  """
@prefix tko: <http://visualartsdna.org/takeout#> .
@prefix vad: <http://visualartsdna.org/2021/07/16/model#> .
@prefix work:	<http://visualartsdna.org/work/> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix dct: <http://purl.org/dc/terms/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix schema: <https://schema.org/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

"""
		m0.each{k1,v1->
			
			def uri = util.Text.camelCase(k1)
			
			//println "$k1"
		def m =new Keep().parseKeepConcepts(v1)
			//if (m.topConcept) println "${m.topConcept}\n"
			
			sb.append """
			tko:$uri
				a skos:Concept ;
				skos:prefLabel "$k1" ;
				skos:definition "${m.topConcept}" ;
				.
"""
		
			try {
				m.each{k,v->
					if (k=="topConcept") return
					//println "$k\n"
					sb.append """
			tko:${util.Text.camelCase(k)}
				a skos:Concept ;
"""
			
					if (v.containsKey("ann"))
					v.ann.each{k2,v2->
						sb.append """
				${nsMap[k2]}:$k2 \"\"\"${v2}\"\"\" ;
"""

						//println "\t$k2=$v2"
					}
					//println "${v.text}\n"
					sb.append """
				skos:definition \"\"\"${v.text}\"\"\" ;
				skos:hasTopConcept tko:$uri;
				.
"""
				}
			} catch (Exception e) {
				println e
			}
		}


		try {
			def model = ju.saveStringModel(""+sb, "ttl")
			println "model size=${model.size()}"
		} catch (Exception ex) {
			println """
$sb
$ex
"""
		}
//			ju.saveModelFile(model,
//				"$dest/${(file.name=~/(.*)\.json/)[0][1]}.ttl", "ttl")
		""+sb

		}


}
