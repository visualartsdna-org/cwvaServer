package rdf

import org.apache.jena.rdf.model.InfModel
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory

class QuerySupport {

	def prefixes = Prefixes.forQuery
	def ju = new JenaUtilities()
	def mdl = ju.newModel()

	QuerySupport(){
		this(null)
	}
	QuerySupport(mdl){
		if (!mdl) this.mdl = cwva.Server.getInstance().dbm.rdfs
		else this.mdl = mdl
	}
	
	def queryCollections() {
		
		ju.queryListMap1(mdl, prefixes,
		"""select ?s ?l {
?s a skos:Collection .
	{?s skos:prefLabel ?l} union {?s rdfs:label ?l}
} order by ?l
""")
	}
	
	def queryCollection(list) {
		def sql = """select ?s ?l {
	{?s skos:prefLabel ?l} union {?s rdfs:label ?l}
	filter (?s in (
"""
		int i=0
		list.each{
			if (i++) sql += ","
			sql += "$it"
		}
			
sql += """
))} order by ?l
"""
		ju.queryListMap1(mdl, prefixes, sql)
	}
	
	def query(ns,guid) {
		def mm = [:]
		
		mm ["$ns:$guid"] = ju.queryDescribe(mdl, prefixes, """
describe ${ns}:${guid}
""")
		if (mm["$ns:$guid"].size()==0) return mm
		mm ["label"] = ju.queryListMap1(mdl, prefixes, """
select ?label { 
			{ ${ns}:${guid} rdfs:label ?label }
			union
			{ ${ns}:${guid} skos:prefLabel ?label }
}
""")[0]["label"]
		mm ["Tags"] =
		ju.queryExecConstruct(mdl, prefixes, """
# print the concepts, labels and definitions for the work or collection
# containing the work
construct {
[
 rdfs:label ?l ;
 skos:description ?d ;
 the:tag ?c
]
} {
	select distinct ?c ?l ?d
	{
		bind (${ns}:${guid} as ?s)
		{
		?s the:tag ?c .
		?c a skos:Concept ;
			skos:definition ?d .
		{?c rdfs:label ?l} union {?c skos:prefLabel ?l} 
		} union {
		?col skos:member ?s .
		?col the:tag ?c .
		?c a skos:Concept ;
		skos:definition ?d.
        {?c rdfs:label ?l} union {?c skos:prefLabel ?l} 
		}
	} order by ?l
}
""")
		mm
	}
	
	def queryRegistry() {
		
		ju.queryListMap1(mdl, prefixes, """
select ?label ?image ?qrcode ?year ?desc
			{ ?s rdfs:label ?label ;
				schema:image ?image ;
				vad:qrcode ?qrcode ;
				vad:media	'Watercolor' ;
				vad:hasArtistProfile work:ebab5e0c-cc32-4928-b326-1ddb4dd62c22 ;
				schema:description ?desc ;
				schema:dateCreated ?date
				bind (year(?date) as ?year)
				} order by ?label #Limit 3
""")
	}
	
	// queryRegistry doesn't get everything
	// this is the complement
	def queryRegistryOther() {
		
		ju.queryListMap1(mdl, prefixes, """
select ?label ?image ?qrcode ?year ?desc
			{ ?s rdfs:label ?label ;
				schema:image ?image ;
				vad:qrcode ?qrcode ;
				vad:media	?med ;
				vad:hasArtistProfile work:ebab5e0c-cc32-4928-b326-1ddb4dd62c22 ;
				schema:description ?desc ;
				schema:dateCreated ?date
				bind (year(?date) as ?year)
				filter(?med != 'Watercolor')
				} order by ?label #Limit 3
""")
	}
	
	def queryCertAuth(ns,guid) {
		def mm = [:]
		
		mm ["$ns:$guid"] = ju.queryDescribe(mdl, prefixes, """
describe ${ns}:${guid}
""")
		mm ["label"] = ju.queryListMap1(mdl, prefixes, """
select ?label { ${ns}:${guid} rdfs:label ?label }
""")[0]["label"]
//		mm ["Profile"] =
//		ju.queryDescribe(mdl, prefixes, """
//# print the profile
//describe ?c {
//		bind (${ns}:${guid} as ?s)
//		?s vad:hasArtistProfile ?c .
//}
//""")
//		mm ["Artist"] =
//		ju.queryDescribe(mdl, prefixes, """
//# print the profile
//describe ?a {
//		bind (${ns}:${guid} as ?s)
//		?s vad:hasArtistProfile ?c .
//		?c vad:artist ?a .
//}
//""")
		mm
	}
	
	def queryCertAuth0(ns,guid) {
		def mm = [:]
		
		mm ["$ns:$guid"] = ju.queryDescribe(mdl, prefixes, """
describe ${ns}:${guid}
""")
		mm ["label"] = ju.queryListMap1(mdl, prefixes, """
select ?label { ${ns}:${guid} rdfs:label ?label }
""")[0]["label"]
		mm ["Profile"] =
		ju.queryDescribe(mdl, prefixes, """
# print the profile
describe ?c {
		bind (${ns}:${guid} as ?s)
		?s vad:hasArtistProfile ?c .
}
""")
		mm ["Artist"] =
		ju.queryDescribe(mdl, prefixes, """
# print the profile
describe ?a {
		bind (${ns}:${guid} as ?s)
		?s vad:hasArtistProfile ?c .
		?c vad:artist ?a .
}
""")
		mm
	}
	
	// query support methods
	def getOneInstanceModel(ns,guid) {
		getOneInstanceModel("$ns:$guid")
	}
	def getOneInstanceModel(work) {

		ju.queryDescribe(mdl, prefixes, """
describe ${work}
"""
				)
	}

	def queryOnePropertyFromInstance(inst, prop) {
		
		if (inst.startsWith("http://")){
			inst = "<$inst>"
		}
		def lm = ju.queryListMap1(mdl, prefixes,
		"""select ?o {
$inst $prop ?o 
}
""")
		lm[0]["o"]
	}

	def queryBackgrounds() {
		
		def lm = ju.queryListMap1(mdl, prefixes,
"""
select ?s ?l {
?s a the:Background ;
	rdfs:label ?l
}"""
)
		lm
	}
	

}
