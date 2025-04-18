package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test
import rdf.JenaUtils

class ReleaseNotesLoader {

	@Test
	void test() {
		def ju = new JenaUtils()
		def m = ju.loadFiles("/stage/tmp/release.ttl")
		def json = ju.saveModelString(m,"JSON-LD")
		def c = new JsonSlurper().parseText(json)
		def s =loadToTtl(c,"C:/Users/ricks/iCloudDrive/Anotes/cwa2.4.5.txt")
		
		println s
	}
	
	@Test
	void test2() {
		def ju = new JenaUtils()
		def m = ju.loadFiles("/stage/tmp/release.ttl")
		def json = ju.saveModelString(m,"JSON-LD")
		def c = new JsonSlurper().parseText(json)
		
		c["@graph"].each{ println it }
		
		c["@graph"].find{
			it.definition == "move support.Guid to util.Guid"
		}.each{
			println "here--$it"
		}
	}
	
	@Test
	void test1() {
		def s =loadToTtl("C:/Users/ricks/iCloudDrive/Anotes/cwa2.4.5.txt")
		new File("/stage/tmp/release.ttl").text = s
	}
	
	def loadToTtl(c,fs) {
		def sb = new StringBuilder()
		int cnt = 0
		c["@graph"].each{  // find highest position
			if (it.broader != "the:releaseNotes")
			cnt = Math.max(cnt,it["schema:position"] as int)
		}
		cnt++
		
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

"""
		def key
		def version = -1
		def category
		def catShort
		new File(fs).eachLine{
			//println it
			if (it.startsWith("#")) return
			else if (it =~ /^[0-9]+\.[0-9]+\.[0-9]+$/) {
				version = it
			}
			else if (it  =~ /^_ .*$/) { // [A-Za-z0-9 ,.;\:\"'?\(\)]+
				; //m[it.substring(2)] = "todo"
			}
			else if (it  =~ /^x .*$/) {
				def date = new SimpleDateFormat("yyyy-MM-dd").format(new Date())

				def found = c["@graph"].find{k->
					(k.definition == it.substring(2)
					&& k.broader == "the:release_$version")
				}
				
				/**
				 * Issues with "position" getting last used number
				 * from existing entries
				 * within correct version
				 */
				if (found) {
				sb.append """
	[
		skos:definition \"\"\"${found.definition}\"\"\" ;
		skos:prefLabel "${found.prefLabel}" ;
		a skos:Concept ;
		skos:broader ${found.broader} ;
		schema:position ${found["schema:position"]} ;
		schema:status "${found.status}" ;
		schema:datePublished ${found.datePublished} ;
	] .
"""
				} else {
		
				sb.append """
	[
		skos:definition \"\"\"${it.substring(2)}\"\"\" ;
		skos:prefLabel "$version${category?"_$catShort":""}_${cnt}" ;
		a skos:Concept ;
		skos:broader the:release_$version ;
		schema:position ${cnt++} ;
		schema:status "completed" ;
		schema:datePublished "$date"^^xs:date ;
	] .
"""
				}
			}			
			else if (it  =~ /^[\w ]+$/) {
				category = "$it"
				catShort = util.Text.camelCase(it)
			}			
		}
		sb.append"""
	the:release_$version
		skos:definition "A release of code and metadata" ;
		skos:prefLabel "Release Notes for version $version" ;
		a skos:Concept ;
		skos:broader the:releaseNotes ;
		skos:inConceptScheme the:systemMaintenance ;
		.

"""
		"$sb"
	}

	def loadToTtl(fs) {
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

"""
		def cnt = 1
		def key
		def version = -1
		def category
		def catShort
		new File(fs).eachLine{
			//println it
			if (it.startsWith("#")) return
			else if (it =~ /^[0-9]+\.[0-9]+\.[0-9]+$/) {
				version = it
			}
			else if (it  =~ /^_ .*$/) { // [A-Za-z0-9 ,.;\:\"'?\(\)]+
				; //m[it.substring(2)] = "todo"
			}
			else if (it  =~ /^x .*$/) {
				def date = new SimpleDateFormat("yyyy-MM-dd").format(new Date())
				
				sb.append """
	[
		skos:definition \"\"\"${it.substring(2)}\"\"\" ;
		skos:prefLabel "$version${category?"_$catShort":""}_${cnt}" ;
		a skos:Concept ;
		skos:broader the:release_$version ;
		schema:position ${cnt++} ;
		schema:status "completed" ;
		schema:datePublished "$date"^^xs:date ;
	] .
"""
			}			
			else if (it  =~ /^[\w ]+$/) {
				category = "$it"
				catShort = util.Text.camelCase(it)
			}			
		}
		sb.append"""
	the:release_$version
		skos:definition "A release of code and metadata" ;
		skos:prefLabel "Release Notes for version $version" ;
		a skos:Concept ;
		skos:broader the:releaseNotes ;
		skos:inConceptScheme the:systemMaintenance ;
		.

"""
		"$sb"
	}

	@Test
	void test0() {
		def m =load("C:/Users/ricks/iCloudDrive/Anotes/cwa2.4.5.txt")
		println "done\n"
		m.each{k,v->
			if (v=="done")
				println "$k"
		}
		println "\ntodo\n"
		m.each{k,v->
			if (v=="todo")
				println "$k"
		}
		println "\ncategory\n"
		m.each{k,v->
			if (v=="category")
				println "$k"
		}
	}
	
	def load(fs) {
		def m=[:]
		def key
		def version = -1
		new File(fs).eachLine{
			println it
			if (it.startsWith("#")) return
			else if (it =~ /^[0-9]+\.[0-9]+\.[0-9]+$/) {
				version = it
			}
			else if (it  =~ /^_ .*$/) { // [A-Za-z0-9 ,.;\:\"'?\(\)]+
				m[it.substring(2)] = "todo"
			}
			else if (it  =~ /^x .*$/) {
				m[it.substring(2)] = "done"
			}			
			else if (it  =~ /^[\w ]+$/) {
				m[it] = "category"
			}			
		}
		m
	}

	def makeUri(s) {
		util.Text.camelCase(s)
	}
	

}
