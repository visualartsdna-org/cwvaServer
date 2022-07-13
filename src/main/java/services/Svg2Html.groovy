package services

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import groovy.xml.XmlSlurper
import groovy.xml.XmlUtil

class Svg2Html {

	@Test
	void test0() {
		def out = "junk.html"
		def svg = "test02.svg"
		new File(svg).eachLine {
			if (it.startsWith("<svg")) {
				
			}
		}
		println s
	}

	@Test
	void test1() {
		def out = "junk.html"
		def svg = "test02.svg"
		def parser=new XmlSlurper()
		parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
		parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		
		def rootNode = parser.parse(svg)
		
		println rootNode.name()
		println rootNode.@width
		println rootNode.@height
		println rootNode.@viewBox
	}

	@Test
	void test2() {
		def out = "junk.html"
		def svg = "test02.svg"
		def parser=new XmlSlurper()
		parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
		parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		
		def rootNode = parser.parse(svg)
		
		println """
<${rootNode.name()} id="model" xmlns="http://www.w3.org/2000/svg" 
style="display: inline; width: inherit; min-width: inherit; max-width: inherit; height: inherit; min-height: inherit; max-height: inherit; "
width="${rootNode.@width}" height="${rootNode.@height}"
viewBox="${rootNode.@viewBox}"
version="1.1">
"""
	}
	
	def htmlEpilog = """
    </div>
    <button id="enable">enable</button>
    <button id="disable">disable</button>

    <script>
      window.onload = function() {
        window.zoomTiger = svgPanZoom('#model', {
          zoomEnabled: true,
          controlIconsEnabled: true,
          fit: true,
          center: true,
        });
        document.getElementById('enable').addEventListener('click', function() {
          window.zoomTiger.enableControlIcons();
        })
        document.getElementById('disable').addEventListener('click', function() {
          window.zoomTiger.disableControlIcons();
        })
      };
    </script>
  </body>
</html>
"""

	@Test
	void test3() {
		def out = "junk.html"
		def svg = "test02.svg"
		def parser=new XmlSlurper()
		parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
		parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		
		def rootNode = parser.parse(svg)
		
		println """
${htmlProlog}
<${rootNode.name()} id="model" xmlns="http://www.w3.org/2000/svg" 
style="display: inline; width: inherit; min-width: inherit; max-width: inherit; height: inherit; min-height: inherit; max-height: inherit; "
width="${rootNode.@width}" height="${rootNode.@height}"
viewBox="${rootNode.@viewBox}"
version="1.1">
...
${htmlEpilog}
"""
	}

	@Test
	void test() {
		def out = "C:/temp/svg/test100.html"
		def svg = "C:/temp/svg/test100.svg"
		convert(out,svg)
	}
	
	def convert(out,svg, title) {
	def htmlProlog = """
<html>
  <head>
    <script src="../dist/svg-pan-zoom.js"></script>
  </head>
  <body>
    <h1>$title</h1>
    <div id="container" style="width: 1500px; height: 1000px; border:1px solid black; ">
"""
		def parser=new XmlSlurper()
		parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
		parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		
		def rootNode = parser.parse(svg)
		
		def s= """
${htmlProlog}
<${rootNode.name()} id="model" xmlns="http://www.w3.org/2000/svg" 
style="display: inline; width: inherit; min-width: inherit; max-width: inherit; height: inherit; min-height: inherit; max-height: inherit; "
width="${rootNode.@width}" height="${rootNode.@height}"
viewBox="${rootNode.@viewBox}"
version="1.1">
${XmlUtil.serialize(rootNode.g[0])}
${htmlEpilog}
"""
		
		new File(out).text = s .replaceAll("tag0:","")
	}

}
