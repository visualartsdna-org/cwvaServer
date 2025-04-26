package services

import org.markdown4j.Markdown4jProcessor
import support.util.GeminiInterface

class AIInterpretation {
	
	def handleQueryParams(m) {
		
		getHtml(m)
	}

	def getHtml(m) {
		def col = new GeminiInterface().submit(m)
		def text = col.candidates[0].content.parts[0].text
		
		def htmlMD = new Markdown4jProcessor().process(text)
		def html = """
<html>
<head>
<style>
.button {
  background-color: #e7e7e7; color: steelblue;
  border: none;
  padding: 12px 24px;
  text-align: center;
  text-decoration: none;
  display: inline-block;
  font-size: 14px;
  margin: 4px 2px;
  cursor: pointer;
}
</style>
</head>
<body>
<button class="button" onclick="history.back()">Go Back</button>
<br/>
${htmlMD}
</body>
</html>
"""
	}
}
