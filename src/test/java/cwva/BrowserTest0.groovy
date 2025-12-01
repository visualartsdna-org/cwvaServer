package cwva

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class BrowserTest0 {

	@Test
	void test() {
		def data = [
			[image:"http://192.168.1.71/images/Seoul.jpg",label:"Seoul",uri:"http://192.168.1.71/work/951467b2-d8c8-4222-b394-0c15f5bf5204",artist:"Rick Spates", site:"http://rickspates.art"],
			[image:"http://192.168.1.71/images/TensionT5Links.jpg",label:"Tension T5 Links",uri:"http://192.168.1.71/work/8e69fb3e-bd56-43d8-b8e3-6ced8d8fcfb4",artist:"rspates", site:"http://rspates.art"],
			[image:"http://192.168.1.71/images/BeeBalm.jpg",label:"Bee Balm",uri:"http://192.168.1.71/work/dce84a9a-1f2e-45fe-8b00-66b63b4d7587",artist:"Rick Spates", site:"http://rickspates.art"],
			[image:"http://192.168.1.71/images/Windmills.jpg",label:"Windmills",uri:"http://192.168.1.71/work/b9913142-70be-4593-84ef-919ce08f86b3",artist:"Rick Spates", site:"http://rickspates.art"],
			
			]
		def s = html(data)
		new File("/stage/tmp/junk.html").text = s
	}

	def html(data) {
		def html = """
<html>
<head>
</head>
<body>
hello

<table>
<tr>
""" 
		data.each{m->
			html += """
<td>
<table><tr><td>
<a href="${m.uri}">
<img src="${m.image}" width="250" />
</a>
</td></tr><tr><td><center>
<a href="${m.uri}">
${m.label}
</a>
</center>
</td></tr><tr><td><center>
<a href="${m.site}">
${m.artist}
</a>
</center>
</td></tr></table>
</td>
"""
		}
		html += """
</tr></table>
</body>
</html>

"""
		
	}
}
