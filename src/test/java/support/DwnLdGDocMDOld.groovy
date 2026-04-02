package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.ServiceAccountCredentials

import java.util.regex.Pattern


class DwnLdGDocMDOld {

	@Test
	void test() {
		
		def docUrl = "https://docs.google.com/document/d/19yCwEWGdTY6sMCpBabJo09iSrmH6pjBPYezcOMAucuA/edit?tab=t.0"
		//String docUrl       = "https://docs.google.com/document/d/17wxI6Gxva7a6tmJ901nASAZ8_WU8scjVmYm-ypFVxBo/edit?usp=sharing"
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
//		if (!(docTitle =~ /(?i)\b(?:criticism|critique)\b/)) {
//			println "found no crit file: $docTitle"
//			throw new Exception("found no crit file: $docTitle")
//		}
		println "MIME type: ${mimeType}"
		
		if (mimeType != 'application/vnd.google-apps.document') {
			System.err.println("WARNING: File is not a Google Doc (${mimeType}). Export may fail.")
		}
		
		// ---------------------------------------------------------------------------
		// Export the document as Markdown via Drive API
		// ---------------------------------------------------------------------------
		println "Exporting as Markdown (text/markdown)..."
		
		String markdown
		
		try {
		    def exportRequest = driveService.files().export(documentId, 'text/markdown')
		    if (apiKey) { exportRequest.setKey(apiKey) }
		
		    def outputStream = new ByteArrayOutputStream()
		    exportRequest.executeMediaAndDownloadTo(outputStream)
		    markdown = outputStream.toString('UTF-8')
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
			
		    println "Markdown export received: ${markdown.length()} characters"
		
		} catch (Exception e) {
		    // If text/markdown is not supported, fall back to plain text
		    System.err.println("Markdown export failed (${e.message}), falling back to text/plain...")
		
		    def exportRequest = driveService.files().export(documentId, 'text/plain')
		    if (apiKey) { exportRequest.setKey(apiKey) }
		
		    def outputStream = new ByteArrayOutputStream()
		    exportRequest.executeMediaAndDownloadTo(outputStream)
		    String plainText = outputStream.toString('UTF-8')
		    println "Plain text export received: ${plainText.length()} characters"
		
		    // Wrap plain text with the document title as an H1
		    markdown = "# ${docTitle}\n\n${plainText}"
			
		}
		
		// ---------------------------------------------------------------------------
		// Write to output file
		// ---------------------------------------------------------------------------
		new File(outputFile).setText(markdown, 'UTF-8')
		println "Markdown saved to: ${outputFile}"
		println "---"
//		println markdown
		
		
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
		
}
