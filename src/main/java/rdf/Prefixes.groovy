package rdf

class Prefixes {
	
	static def forQuery = """
prefix vad: <http://visualartsdna.org/2021/07/16/model#> 
prefix work:	<http://visualartsdna.org/work/> 
prefix the:   <http://visualartsdna.org/thesaurus/> 
prefix xs: <http://www.w3.org/2001/XMLSchema#> 
prefix xsd: <http://www.w3.org/2001/XMLSchema#> 
prefix skos: <http://www.w3.org/2004/02/skos/core#> 
prefix dct: <http://purl.org/dc/terms/> 
prefix foaf: <http://xmlns.com/foaf/0.1/> 
prefix schema: <https://schema.org/> 
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
prefix owl: <http://www.w3.org/2002/07/owl#>
prefix tko:   <http://visualartsdna.org/takeout/>
"""
	
	static def forFile = """
@prefix vad: <http://visualartsdna.org/2021/07/16/model#> .
@prefix work:	<http://visualartsdna.org/work/> .
@prefix the:   <http://visualartsdna.org/thesaurus/> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix dct: <http://purl.org/dc/terms/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix schema: <https://schema.org/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix tko:   <http://visualartsdna.org/takeout/> .
"""
}
