package support

import org.markdown4j.Markdown4jProcessor
import support.util.GeminiInterface

class MarkdownToHtml {


	def handleQueryParams(m) {

		def text = new File("${m.directory}/${m.fileupload}").text
		getHtml(text)
	}

	def handleBlob(filePart) {
		try {
			if (filePart != null) {
				InputStream inputStream = filePart.inputStream

				def text = inputStream.text 
				getHtml(text)
			} else {
				throw new Exception("Part 'markdownFile' not found")
			}
		} catch (Exception e) {
			throw new RuntimeException( e.message)
		}
	}

	def getHtml(text) {

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
