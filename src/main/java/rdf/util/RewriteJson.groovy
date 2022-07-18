package rdf.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test
import rdf.JenaUtils
import util.Rson

class RewriteJson {

	def jsonDir = "json"
	def ttlDir = "ttl"

	// TODO: @Release of takout to ttl
	// For processing takeout json to ttl
	// extract new takeout folder
	// point base var to the folder and run
	// collect "published" ttl in the base/ttl folder
	@Test
	void test() {
		def base = "C:/temp/generatedFiles"
		setup(base)
		process(base)
		
		def src = "$base/$jsonDir"
		def dest = "$base/$ttlDir"
		def type = "vad:NotaBene" 	// Google Keep notes 
		def prefix = "tko"			// via Takeout
		process(src,dest,type,prefix)

	}

	def setup(base) {
		def dir = new File("$base/$jsonDir")
		if (!dir.isDirectory()) {
			dir.mkdirs()
		}
		dir = new File("$base/$ttlDir")
		if (!dir.isDirectory()) {
			dir.mkdirs()
		}
	}

	def process(base) {
		new File(base).eachFile {fn->

			if (!fn.name.endsWith(".json")) return
			def json = "$base/${fn.name}"
			def m = Rson.load(json)
			def col = getCol(m)

			// check for publish label
			def publish = col.tags.find{
				it == "publish"
			}
			if (publish) {
				m += col
				//m.each { println it }
				new File("$base/$jsonDir/${fn.name}").text = JsonOutput.prettyPrint(
						new JsonOutput().toJson(m)
						)
			}
		}
	}
	

	def process(src,dest,type,prefix) {
		def js = new JsonSlurper()
		def ju = new JenaUtils()
		new File(src).eachFile {file->
			
			if (!(file.name.endsWith(".json"))) return

			println "$file"
			def model = ju.newModel()
			def c = Rson.load(file.absolutePath)//js.parse(file)
			if (c instanceof Map
				&& type) {
				c["rdf:type"] = type == "" ? "unknown" : type
			} else if (c instanceof List
				&& type) {
				c = ["$prefix:data":c]
				c["rdf:type"] = type == "" ? "unknown" : type
			}
				def sb = new StringBuilder()
				sb.append  """
@prefix tko: <http://visualartsdna.org/takeout#> .
@prefix vad: <http://visualartsdna.org/2021/07/16/model#> .
@prefix work:	<http://visualartsdna.org/work/> .
@prefix z0:	<http://visualartsdna.org/system/> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix dc: <http://purl.org/dc/elements/1.1/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix schema: <https://schema.org/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

"""
				try {
					JsonRdfUtil.jsonToTtl(["$prefix":[c]], sb, "$prefix:")
					def s = (""+sb).replaceAll(/([^\\])\\([^tbnrf\\'"])/,/$1\\\\$2/)

					model = ju.saveStringModel(s, "ttl")
				} catch (Exception ex) {
					println """
$file
$sb
$ex
"""
				}
			ju.saveModelFile(model,
				"$dest/${(file.name=~/(.*)\.json/)[0][1]}.ttl", "ttl")

		}
	}



	// for the annotation map design
	def getCol(m) {
		def col = [:]

		def text = ""
		m.textContent.eachLine{

			try {
				if (it.trim().startsWith("[")) {
					def ann = (it.trim() =~ /^\[(.*)\]/)[0][1]
					//println "\t$ann"
					def fs = ann.split("=")
					if (fs[0]=="z0:json") {
						def jc = new JsonSlurper().parseText(fs[1])
						if (!(jc instanceof Map)) {
							col["z0:json"]=jc
						} else {
							col += jc
						}
					}
					else if (!fs[0].contains(":")){
						col["schema:${fs[0]}"]=fs[1]
					}
					else col[fs[0]]=fs[1]
				}
				else if (!it.startsWith("@"))
					text += "$it\n"
			} catch (Exception e){
				println "Annotation error on ${it.trim()}\n$e"
			}
		}
		// redistribute annotation map
		if (col.annotationMap instanceof Map)
			try {
				col.annotationMap.each { k,v->
					def key = k.contains(":") ? k : "schema:$k"
					col[key] = v
				}
				col.remove("annotationMap")
			} catch (Exception e) {
				println "Annotation Map error on ${col.annotationMap}\n$e"
			}

		// text
		col.filteredText = text

		// labels
		col.tags = []
		m.labels.each{
			col.tags += it.name
		}
		col
	}

	@Test
	void test0() {
		def json = "C:/temp/tratsi/Takeout/Keep/Covid.json"
		def m = Rson.load(json)
		println "\n${m.title}"
		def col = [:]

		def text = ""
		m.textContent.eachLine{

			try {
				if (it.trim().startsWith("[")) {
					def ann = (it.trim() =~ /^\[(.*)\]/)[0][1]
					//println "\t$ann"
					def fs = ann.split("=")
					if (fs[0]=="z0:json") {
						def jc = new JsonSlurper().parseText(fs[1])
						if (!(jc instanceof Map)) {
							col["z0:json"]=jc
						} else {
							col += jc
						}
					}
					else col[fs[0]]=fs[1]
				}
				else if (!it.startsWith("@"))
					text += "$it\n"
			} catch (Exception e){
				println "Error on ${it.trim()}\n$e"
			}
		}
		println "$text"
		col.each{println "\t$it"}
		int i=0
		m.labels.each{
			if (i++>0) print ", "
			print "${it.name}"
		}
		println "\n---"

	}

}
