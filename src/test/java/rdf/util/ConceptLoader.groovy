package rdf.util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class ConceptLoader {

	@Test
	void test() {
		def s =makeTtl("G:/My Drive/Anotes/new 3.txt")
		new File("/stage/metadata/vocab/watercolorTextures.ttl").text = s
	}
	
	@Test
	void test1() {
		def s =makeTtl("G:/My Drive/Anotes/new 3.txt")
		println s
	}
	
	@Test
	void test0() {
		def m =load("G:/My Drive/Anotes/new 3.txt")
		m.each{k,v->
			println "${makeUri(k)}\n$k=$v\n"
		}
	}
	
	def makeTtl(s) {
		def sb = new StringBuilder()
		sb.append """
# created ${new Date()} 
@prefix schema: <https://schema.org/> .
@prefix the:   <http://visualartsdna.org/thesaurus/> .
@prefix dct:   <http://purl.org/dc/terms/> .
@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix owl:   <http://www.w3.org/2002/07/owl#> .
@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix xs:    <http://www.w3.org/2001/XMLSchema#> .

the:watercolorTextureTechnique
        a                  skos:Concept ;
        rdfs:label         "Texture Technique" ;
        skos:broader       the:watercolorTechnique ;
        skos:definition    "Watercolor techniques supporting development of texture in the painting" ;
        skos:inScheme      the:watercolorPainting  .

"""
		def m =load("G:/My Drive/Anotes/new 3.txt")
		m.each{k,v->
			sb.append """
the:${makeUri(k)}
        a                  skos:Concept ;
        skos:inScheme      the:watercolorPainting ;
        rdfs:label         "$k" ;
        skos:broader       the:watercolorTextureTechnique ;
        skos:definition    \"\"\"${m[k]}\"\"\" ;
        skos:inScheme      the:watercolorPainting .

"""
		}

		"$sb"
	}
	
	
	def makeUri(s) {
		util.Text.camelCase(s)
	}
	
	def load(fs) {
		def m=[:]
		def key
		new File(fs).eachLine{
			if (it.startsWith("#")) return
			else if (it.trim() == "") key=null
			else if (!key) {
				key = it
			}
			else {
				m[key]=it
			}
			
		}
		m
	}

}
