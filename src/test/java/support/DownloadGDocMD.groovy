package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.ServiceAccountCredentials

import java.util.regex.Pattern


class DownloadGDocMD {
	
	def ju = new JenaUtilities()

	@Test
	void test() {
		def src = "/stage/server/cwvaContent/ttl/data"
		def tgt = "/stage/conceptQueue"
		def model = ju.loadFiles(src)
		def lm = ju.queryListMap1(model,rdf.Prefixes.forQuery,"""
		select ?sd ?l {
		 ?s a the:AI ;
			rdfs:label ?l ;
			the:topic the:Criticism , the:Digital, the:Sculpture;
			the:document ?sd ;
			.
			}
""")
		int i=0
		lm.each{
			String docUrl       = it.sd
			String outputFile   = "$tgt/${it.l}_${i++}.md"
			try {
				getDoc(docUrl, outputFile)
			} catch (Exception ex) {
				println "${it.l}, $ex"
			}
			sleep 5 * 1000
		}
	}
	
	@Test
	void test0() {
		
		// locate a jar for a class!!
//		println com.google.common.io.ByteStreams.class.getProtectionDomain().getCodeSource().getLocation()
		
		String docUrl       = "	https://docs.google.com/document/d/17wxI6Gxva7a6tmJ901nASAZ8_WU8scjVmYm-ypFVxBo/edit?usp=sharing"
		String outputFile   = 'C:/stage/tmp/conceptTest/output.md'
		getDoc(docUrl, outputFile)
		
	}
	
	def getDoc(docUrl, outputFile) {
		
		String documentId = extractDocId(docUrl)
		//println "Document ID: ${documentId}"
		
		// ---------------------------------------------------------------------------
		// Build the Drive API client
		// ---------------------------------------------------------------------------
		def JSON_FACTORY   = GsonFactory.getDefaultInstance()
		def HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
		def APP_NAME       = "GoogleDocToMarkdown"
		
		Drive driveService
		
			// --- API Key authentication (public docs only) ---
			driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
				.setApplicationName(APP_NAME)
				.build()
		
		// ---------------------------------------------------------------------------
		// Fetch document metadata (title) via Drive API
		// ---------------------------------------------------------------------------
		//println "Fetching document metadata..."
		String apiKey = System.getProperty("gdApiKey")
		
		def fileGet = driveService.files().get(documentId).setFields("name, mimeType")
		if (apiKey) { fileGet.setKey(apiKey) }
		
		def fileMeta = fileGet.execute()
		String docTitle = fileMeta.getName()
		String mimeType = fileMeta.getMimeType()
		println "Title: ${docTitle}"
		println "MIME type: ${mimeType}"
		
		if (mimeType != 'application/vnd.google-apps.document') {
			System.err.println("WARNING: File is not a Google Doc (${mimeType}). Export may fail.")
		}
		
		// ---------------------------------------------------------------------------
		// Export the document as HTML via Drive API
		// ---------------------------------------------------------------------------
		println "Exporting as HTML..."
		
		def exportRequest = driveService.files().export(documentId, 'text/html')
		if (apiKey) { exportRequest.setKey(apiKey) }
		
		def outputStream = new ByteArrayOutputStream()
		exportRequest.executeMediaAndDownloadTo(outputStream)
		
		String html = outputStream.toString('UTF-8')
		println "HTML export received: ${html.length()} characters"
		
		def converter = new HtmlToMarkdownConverter(docTitle)
		String markdown = converter.convert(html)
		
		// IMAGE cleanup
		// Remove inline images: ![alt](data:image/...) and ![alt](https://...)
		markdown = markdown.replaceAll(/!\[[^\]]*\]\([^\)]+\)/, '')
		
		// Remove any leftover blank lines from stripped images
		markdown = markdown.replaceAll(/\n{3,}/, '\n\n')
		
		// Remove inline images: ![alt](...)
		markdown = markdown.replaceAll(/!\[[^\]]*\]\([^\)]+\)/, '')
		
		// Remove reference-style image definitions: [imageN]: <data:...>
		markdown = markdown.replaceAll(/(?m)^\[image\d+\]:\s*<[^>]+>\s*$/, '')
		
		// Remove reference-style image usages: ![alt][imageN]
		markdown = markdown.replaceAll(/!\[[^\]]*\]\[[^\]]+\]/, '')
		
		// Clean up leftover blank lines
		markdown = markdown.replaceAll(/\n{3,}/, '\n\n')

		
		// ---------------------------------------------------------------------------
		// Write to output file
		// ---------------------------------------------------------------------------
		new File(outputFile).text = markdown
		println "Markdown saved to: ${outputFile}"
		println "---"
		//println markdown
		
		
	}
	
	// ---------------------------------------------------------------------------
	// Extract the Document ID from the shared URL
	// ---------------------------------------------------------------------------
	String extractDocId(String url) {
		// Handles URLs like:
		//   https://docs.google.com/document/d/DOCUMENT_ID/edit?usp=sharing
		//   https://docs.google.com/document/d/DOCUMENT_ID/
		//   https://docs.google.com/document/d/DOCUMENT_ID
		def matcher = url =~ /\/document\/d\/([a-zA-Z0-9_-]+)/
		if (matcher.find()) {
			return matcher.group(1)
		}
		// Maybe the user passed a raw document ID
		if (url ==~ /^[a-zA-Z0-9_-]+$/) {
			return url
		}
		throw new IllegalArgumentException(
			"Could not extract a document ID from: ${url}\n" +
			"Expected a URL like https://docs.google.com/document/d/<ID>/edit"
		)
	}
	

	// ---------------------------------------------------------------------------
	// Convert HTML to Markdown
	// ---------------------------------------------------------------------------
	
	class HtmlToMarkdownConverter {
	
		String docTitle
	
		HtmlToMarkdownConverter(String title) {
			this.docTitle = title
		}
	
		String convert(String html) {
			// Extract only the <body> content
			String body = extractBody(html)
	
			// Process block-level elements first
			String md = body
	
			// --- Headings ---
			// Google exports headings as <h1> through <h6> with inline styles.
			// We map doc headings down one level so the doc title can be #.
			(1..6).each { level ->
				String prefix = '#' * (level + 1)  // h1 -> ##, h2 -> ###, etc.
				md = md.replaceAll(
					/(?is)<h${level}[^>]*>(.*?)<\/h${level}>/,
					"\n${prefix} \$1\n\n"
				)
			}
	
			// --- Tables ---
			md = convertTables(md)
	
			// --- Lists ---
			md = convertLists(md)
	
			// --- Horizontal rules ---
			md = md.replaceAll(/(?i)<hr[^>]*\/?>/, '\n---\n')
	
			// --- Line breaks and paragraphs ---
			md = md.replaceAll(/(?i)<br[^>]*\/?>/, '\n')
			md = md.replaceAll(/(?is)<p[^>]*>(.*?)<\/p>/, '\$1\n\n')
			md = md.replaceAll(/(?is)<div[^>]*>(.*?)<\/div>/, '\$1\n')
	
			// --- Inline formatting ---
			// Bold
			md = md.replaceAll(/(?is)<b[^>]*>(.*?)<\/b>/, '**$1**')
			md = md.replaceAll(/(?is)<strong[^>]*>(.*?)<\/strong>/, '**$1**')
	
			// Italic
			md = md.replaceAll(/(?is)<i[^>]*>(.*?)<\/i>/, '*$1*')
			md = md.replaceAll(/(?is)<em[^>]*>(.*?)<\/em>/, '*$1*')
	
			// Strikethrough
			md = md.replaceAll(/(?is)<s[^>]*>(.*?)<\/s>/, '~~$1~~')
			md = md.replaceAll(/(?is)<strike[^>]*>(.*?)<\/strike>/, '~~$1~~')
			md = md.replaceAll(/(?is)<del[^>]*>(.*?)<\/del>/, '~~$1~~')
	
			// Underline (no native Markdown — use HTML passthrough)
			md = md.replaceAll(/(?is)<u[^>]*>(.*?)<\/u>/, '<u>$1</u>')
	
			// Superscript / Subscript (keep as HTML)
			md = md.replaceAll(/(?is)<sup[^>]*>(.*?)<\/sup>/, '<sup>$1</sup>')
			md = md.replaceAll(/(?is)<sub[^>]*>(.*?)<\/sub>/, '<sub>$1</sub>')
	
			// Code (inline)
			md = md.replaceAll(/(?is)<code[^>]*>(.*?)<\/code>/, '`$1`')
	
			// Preformatted / code blocks
			md = md.replaceAll(/(?is)<pre[^>]*>(.*?)<\/pre>/, '\n```\n$1\n```\n')
	
			// Links
			md = md.replaceAll(
				/(?is)<a[^>]*href\s*=\s*"([^"]*)"[^>]*>(.*?)<\/a>/,
				'[$2]($1)'
			)
	
			// Images — match alt before src or src before alt
			md = md.replaceAll(
				/(?is)<img[^>]*src\s*=\s*"([^"]*)"[^>]*alt\s*=\s*"([^"]*)"[^>]*\/?>/,
				'![$2]($1)'
			)
			md = md.replaceAll(
				/(?is)<img[^>]*alt\s*=\s*"([^"]*)"[^>]*src\s*=\s*"([^"]*)"[^>]*\/?>/,
				'![$1]($2)'
			)
			md = md.replaceAll(
				/(?is)<img[^>]*src\s*=\s*"([^"]*)"[^>]*\/?>/,
				'![image]($1)'
			)
	
			// Blockquotes
			md = md.replaceAll(/(?is)<blockquote[^>]*>(.*?)<\/blockquote>/) { match, content ->
				content.toString().split('\n').collect { "> ${it}" }.join('\n') + '\n'
			}
	
			// --- Cleanup ---
			// Strip Google's inline style spans and all remaining HTML tags
			md = md.replaceAll(/<[^>]+>/, '')
	
			// Decode common HTML entities
			md = decodeEntities(md)
	
			// Collapse excessive blank lines (3+ → 2)
			md = md.replaceAll(/\n{3,}/, '\n\n')
	
			// Remove empty bold/italic markers left behind
			md = md.replaceAll(/\*\*\s*\*\*/, '')
			md = md.replaceAll(/\*\s*\*/, '')
	
			// Trim leading/trailing whitespace per line
			md = md.split('\n').collect { it.stripTrailing() }.join('\n')
	
			// Build final output with title as H1
			String result = "# ${docTitle}\n\n${md}"
			return result.trim() + '\n'
		}
	
		/**
		 * Extract content between <body> tags.
		 */
		private String extractBody(String html) {
			def matcher = html =~ /(?is)<body[^>]*>(.*)<\/body>/
			if (matcher.find()) {
				return matcher.group(1)
			}
			return html
		}
	
		/**
		 * Convert HTML tables to Markdown pipe tables.
		 */
		private String convertTables(String md) {
			md = md.replaceAll(/(?is)<table[^>]*>(.*?)<\/table>/) { match, tableHtml ->
				convertSingleTable(tableHtml.toString())
			}
			return md
		}
	
		private String convertSingleTable(String tableHtml) {
			def sb = new StringBuilder('\n')
			def rows = []
	
			// Extract rows
			def rowMatcher = tableHtml =~ /(?is)<tr[^>]*>(.*?)<\/tr>/
			while (rowMatcher.find()) {
				String rowHtml = rowMatcher.group(1)
				def cells = []
	
				// Match <th> and <td> cells
				def cellMatcher = rowHtml =~ /(?is)<(?:td|th)[^>]*>(.*?)<\/(?:td|th)>/
				while (cellMatcher.find()) {
					String cellContent = cellMatcher.group(1)
						.replaceAll(/<[^>]+>/, '')     // strip inner HTML
						.replaceAll(/\n/, ' ')          // flatten newlines
						.trim()
					cells << cellContent
				}
				if (cells) { rows << cells }
			}
	
			if (!rows) return ''
	
			// Determine column count from widest row
			int colCount = rows*.size().max()
	
			// Render header row
			def headerRow = rows[0]
			sb.append('|')
			(0..<colCount).each { i ->
				sb.append(" ${i < headerRow.size() ? headerRow[i] : ''} |")
			}
			sb.append('\n')
	
			// Separator row
			sb.append('|')
			(0..<colCount).each { sb.append(' --- |') }
			sb.append('\n')
	
			// Data rows
			rows.drop(1).each { row ->
				sb.append('|')
				(0..<colCount).each { i ->
					sb.append(" ${i < row.size() ? row[i] : ''} |")
				}
				sb.append('\n')
			}
			sb.append('\n')
			return sb.toString()
		}
	
		/**
		 * Convert HTML ordered and unordered lists to Markdown.
		 * Handles nested lists by applying the conversion recursively.
		 */
		private String convertLists(String md) {
			// Process innermost lists first, then work outward
			String previous = ''
			while (previous != md) {
				previous = md
				md = md.replaceAll(/(?is)<ul[^>]*>(.*?)<\/ul>/) { match, listHtml ->
					convertListItems(listHtml.toString(), false, 0)
				}
				md = md.replaceAll(/(?is)<ol[^>]*>(.*?)<\/ol>/) { match, listHtml ->
					convertListItems(listHtml.toString(), true, 0)
				}
			}
			return md
		}
	
		private String convertListItems(String listHtml, boolean ordered, int depth) {
			def sb = new StringBuilder('\n')
			String indent = '  ' * depth
			int index = 1
			def itemMatcher = listHtml =~ /(?is)<li[^>]*>(.*?)<\/li>/
			while (itemMatcher.find()) {
				String itemText = itemMatcher.group(1)
					.replaceAll(/<[^>]+>/, '')  // strip remaining tags
					.trim()
				if (ordered) {
					sb.append("${indent}${index}. ${itemText}\n")
					index++
				} else {
					sb.append("${indent}- ${itemText}\n")
				}
			}
			sb.append('\n')
			return sb.toString()
		}
	
		/**
		 * Decode common HTML entities to their Unicode characters.
		 */
		private String decodeEntities(String text) {
			text = text.replace('&amp;',   '&')
			text = text.replace('&lt;',    '<')
			text = text.replace('&gt;',    '>')
			text = text.replace('&quot;',  '"')
			text = text.replace('&#39;',   "'")
			text = text.replace('&apos;',  "'")
			text = text.replace('&nbsp;',  ' ')
			text = text.replace('&ndash;', '–')
			text = text.replace('&mdash;', '—')
			text = text.replace('&lsquo;', '\u2018')
			text = text.replace('&rsquo;', '\u2019')
			text = text.replace('&ldquo;', '\u201C')
			text = text.replace('&rdquo;', '\u201D')
			text = text.replace('&hellip;', '…')
			text = text.replace('&trade;', '™')
			text = text.replace('&copy;',  '©')
			text = text.replace('&reg;',   '®')
			text = text.replace('&bull;',  '•')
			text = text.replace('&middot;','·')
			text = text.replace('&deg;',   '°')
			text = text.replace('&frac12;','½')
			text = text.replace('&frac14;','¼')
			text = text.replace('&frac34;','¾')
			text = text.replace('&times;', '×')
			text = text.replace('&divide;','÷')
			// Numeric decimal entities  (e.g. &#8211;)
			text = text.replaceAll(/&#(\d+);/) { match, code ->
				String.valueOf((char) Integer.parseInt(code))
			}
			// Numeric hex entities  (e.g. &#x2014;)
			text = text.replaceAll(/&#x([0-9a-fA-F]+);/) { match, hex ->
				String.valueOf((char) Integer.parseInt(hex, 16))
			}
			return text
		}
	}
	
}
