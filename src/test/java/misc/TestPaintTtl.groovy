package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class TestPaintTtl {

	def paints = """
Indian Red	DD
Burnt Sienna	DD
Alizarin Crimson	DD
Vermilion	DD
Naples Yellow	DD
Yellow Ochre Dark	DD
Yellow Ochre Light	DD
Raw Sienna	DD
Cadmium Yellow	DD
Burnt Umber	DD
Raw Umber	DD
Chrome Green	DD
Gerre Verte	SEN
Viridian	DD
Cerulean	SEN
Manganese	DD
Azure	SEN
Otremarine	DD
Cobalt Blue	DD
Indigo	SCC
Mars Violet	DD
Violet	DD
Cobalt Turquoise	LCS
Azure Blue	LCS
Cerulean Blue	LCS
Manganese Blue	LCS
Indigo Blue	LCS
Cobalt Green Deep	LCS
Cobalt Green Light	LCS
Phthalo Green	LCS
Virdian	LCS
Lemon Yellow	LCS
Lead Tin Yellow Dark	LCS
Naples Yellow Dark	LCS
Naples Yellow Light	LCS
Realgar	LCS
Cadmium Yellow Orange	LCS
Potters Pink	LCS
"""
	
def mfrMap=[
	SEN:"the:Sennelier",
	DD:"the:DavidDavis",
	LCS:"the:LCornelissenSon",
	SCC:"the:SchminckeCompany",
	]

	
	@Test
	void test() {
		paints.eachLine{
			if (it.trim() == "") return
			def fs = it.split("\t")
			def p = fs[0]
			def m = fs[1]
			def mfr = "maker"
			
			println """
the:${camelCase(p)}$m
	a	skos:Concept ;
	skos:broader the:WatercolorPaint ;
	rdfs:label	"$p" ;
	skos:definition	"A paint combining a pigment idenfied by label and brand with a binder of the identified material." ;
	schema:material	the:HomemadeWatercolorBinder ;
	schema:image	<http://visualartsdna.org/images/${camelCase(p)}${m}.jpg> ;
	schema:brand	the:${mfrMap[m]} ;
.
"""
		}
	}
	def camelCase(s) {
		s.replaceAll( /( )([A-Za-z0-9])/, {
			it[2].toUpperCase()
			} )
	}
	
	@Test
	void test2() {
		def l=[]
		paints.eachLine{
			if (it.trim() == "") return
			def fs = it.split("\t")
			def p = fs[0]
			def m = fs[1]
			def mfr = "maker"
			
			l += "the:${camelCase(p)}$m"
		}
		l.sort().each{
			println """		<li><input type="checkbox">$it</li>"""
		}
	}

		

}
