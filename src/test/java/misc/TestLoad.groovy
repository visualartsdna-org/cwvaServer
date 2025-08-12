package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
	import org.apache.jena.rdf.model.ModelFactory
	import org.apache.jena.rdf.model.Model
	import org.apache.jena.riot.Lang
	import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RiotException
import java.nio.charset.CharacterCodingException
	import java.io.InputStream
	import java.io.StringReader
	import java.nio.charset.StandardCharsets

class TestLoad {
	
	// an ISO88591 handler isn't necessary
	// utf-8 loads it just fine!
	@Test
	void test3() {
		// ISO88591 test
		def bytISO88591 = """
		@prefix ex: <http://example.org/> .
		ex:data ex:hasValue \"\"\"UTF-16 characters: ñ, é, special characters: éàçü The sentence "**Ça va bien, merci!**" contains characters like **Ç** and **à** that are part of the ISO-8859-1 (Latin 1) character set.\"\"\" .
		""".getBytes(StandardCharsets.ISO_8859_1)
		//String iso88591String = new String(strISO88591, StandardCharsets.ISO_8859_1);
		
		
		
		def utf16InputStream = new ByteArrayInputStream(bytISO88591)
		
		def jenaModel = loadJenaModelWithEncodingFallback(utf16InputStream)
		
		if (jenaModel) {
		    println "\n--- Model Loaded. Printing Statement ---"
		    def statement = jenaModel.listStatements().next()
		    println "Subject: ${statement.subject}"
		    println "Predicate: ${statement.predicate}"
		    println "Object: ${statement.object}"
		    println "--- End of Model Output ---"
		}		
	}

	@Test
	void test2() {
		def utf16Test = """
		@prefix ex: <http://example.org/> .
		ex:data ex:hasValue "UTF-16 characters: ñ, é".
		""".getBytes('UTF-16')
		
		def utf16InputStream = new ByteArrayInputStream(utf16Test)
		
		def jenaModel = loadJenaModelWithEncodingFallback(utf16InputStream)
		
		if (jenaModel) {
		    println "\n--- Model Loaded. Printing Statement ---"
		    def statement = jenaModel.listStatements().next()
		    println "Subject: ${statement.subject}"
		    println "Predicate: ${statement.predicate}"
		    println "Object: ${statement.object}"
		    println "--- End of Model Output ---"
		}		
	}

	@Test
	void test() {
	// --- Example Usage ---
		def ttl2 = """
@prefix dct: <http://purl.org/dc/terms/> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix schema: <https://schema.org/> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix the:   <http://visualartsdna.org/thesaurus/> .
@prefix tko:   <http://visualartsdna.org/takeout/> .
@prefix vad: <http://visualartsdna.org/model/> .
@prefix work:	<http://visualartsdna.org/work/> .
@prefix xs: <http://www.w3.org/2001/XMLSchema#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

the:ElephantEarCriticism20250804075002
	    a                skos:Concept ;
        tko:created      "2025-08-04"^^xsd:date ;
        tko:edited       "2025-08-04T07:50:02"^^xsd:dateTime ;
        the:tag          work:1e122016-0f17-4134-9f4c-37c9bef6946b ;
		skos:broader	the:Criticism  ;
		the:source 	the:ChatGPT ;
        rdfs:label       "Criticism of Elephant Ear" ;
        vad:prompt  \"\"\"Write an artistic criticism of this 15 x 11 inch watercolor called Elephant Ear, with the description, Elephant ear seen in early morning with the sunlight from the left filtering through the translucent leaves.\"\"\" ;
        vad:response  \"\"\"In Elephant Ear, the artist uses the delicate medium of watercolor to capture a luminous, almost breath-held moment in nature. The modest scale—15 by 11 inches—invites an intimate viewing experience. It draws the observer closer, encouraging quiet attention, much like the early morning light that inspires the piece.

Watercolor, inherently fluid and translucent, is a fitting choice for a subject defined by light filtering through soft foliage. The artist uses this medium with control and sensitivity. The luminous lime greens of the elephant ear leaves seem to glow from within—an effect made possible by skillful layering and an understanding of how watercolor pigments interact with paper and water. The shadows, rendered in deep ultramarine and indigo tones, suggest both the physical structure of the leaves and the directional play of light without resorting to harsh outlines.

The contrast between the vibrant leaves and the richly textured background is especially effective in watercolor. Where many might allow the background to fade or wash out, this artist embraces the expressive potential of wet-on-wet techniques and dry brushwork to suggest the irregular surface of a forest floor or damp earth. Splatters, pooling, and layering are used not as accidents but as visual tools, grounding the ethereal leaves in a believable physical environment.

The scale of the work contributes to its intimacy. At 15" x 11", the piece avoids the overwhelming grandeur of large-format botanical illustration, opting instead for a human-scaled encounter—like noticing something beautiful at your feet on a quiet morning walk. The viewer is drawn into a moment of quiet revelation, framed in humble dimensions but filled with atmospheric weight.

Compositionally, the angled, overlapping leaves provide dynamic movement against the more amorphous background. This diagonal orientation pulls the eye from the lower right to the upper left, echoing the source of the light itself. Such a choice not only guides the viewer’s gaze but also enhances the sense of a natural unfolding—leaves growing toward the light, or light discovering the leaves.

As a watercolor, Elephant Ear is not merely a representation of a plant but a meditation on light, form, and temporality. The medium’s transparency echoes the subject’s translucency, and the painting’s restrained scale and luminous palette honor the quiet beauty of a fleeting encounter with nature. The work succeeds both as an observational study and as a poetic impression—controlled yet free, grounded yet glowing.

\"\"\" ;
		.
""".getBytes('Windows-1252')
	def testTurtleData = """
@prefix dc: <http://purl.org/dc/elements/1.1/> .
@prefix ex: <http://example.org/> .
ex:document dc:title "This is a document with an em dash—and a curly quote’." .
""".getBytes('Windows-1252') // Simulate an InputStream from a Windows-1252 source.
	
	def inputStream = new ByteArrayInputStream(ttl2)
	
	def jenaModel = loadJenaModelWithEncodingFallback(inputStream)
	
	if (jenaModel) {
		println "\n--- Model Loaded. Printing Statement ---"
		def statement = jenaModel.listStatements().next()
		println "Subject: ${statement.subject}"
		println "Predicate: ${statement.predicate}"
		println "Object: ${statement.object}"
		println "--- End of Model Output ---"
	}
	
	}
	

/**
 * Loads an Apache Jena Model from an InputStream with a robust encoding fallback.
 *
 * This method attempts to load the stream as UTF-8. If it fails, it falls back
 * to Windows-1252, and if that also fails, it attempts UTF-16. This provides
 * a comprehensive way to handle different source encodings.
 *
 * @param inputStream The InputStream containing the RDF data.
 * @return A Jena Model loaded with the correctly decoded data, or null if loading fails.
 */
	// an ISO88591 handler isn't necessary
	// utf-8 loads it just fine!
def loadJenaModelWithEncodingFallback1(InputStream inputStream) {
    if (!inputStream) {
        println "Input stream is null. Cannot load model."
        return null
    }
        // Read the entire stream into a byte array
        byte[] bytes = inputStream.bytes
		println new String(bytes,  "UTF-16")

    try {

        // Attempt 1: UTF-8 (most common)
        println "Attempting to load model with UTF-8 encoding..."
        def decodedString = new String(bytes, 'UTF-8')
        def model = ModelFactory.createDefaultModel()
        model.read(new StringReader(decodedString), null, "TURTLE")
        println "Model loaded successfully with UTF-8 encoding."
        return model

    } catch (CharacterCodingException|RiotException e) {
        // This catch block handles the case where the input stream is NOT valid UTF-8.
        println "UTF-8 decoding failed. Falling back to Windows-1252..."
        try {
            // Attempt 2: Windows-1252 (common for legacy files)
            def decodedString = new String(bytes, 'Windows-1252')
            
            def model = ModelFactory.createDefaultModel()
            model.read(new StringReader(decodedString), null, "TURTLE")
            println "Model loaded successfully with Windows-1252 fallback."
            return model

        } catch (CharacterCodingException|RiotException ex) {
            // This catch block handles the case where the input is neither UTF-8 nor Windows-1252.
            println "Windows-1252 decoding failed. Falling back to UTF-16..."
            try {
                // Attempt 3: UTF-16 (another common encoding)
                def decodedString = new String(bytes, 'UTF-16')
                
                def model = ModelFactory.createDefaultModel()
                model.read(new StringReader(decodedString), null, "TURTLE")
                println "Model loaded successfully with UTF-16 fallback."
                return model

	        } catch (CharacterCodingException|RiotException ex2) {
	            // This catch block handles the case where the input is neither UTF-8 nor Windows-1252.
	            println "UTF-16 decoding failed. Falling back to ISO-8859-1..."
	            try {
	                // Attempt 4: ISO-8859-1 (another common encoding)
	                def decodedString = new String(bytes, StandardCharsets.ISO_8859_1)
	                
	                def model = ModelFactory.createDefaultModel()
	                model.read(new StringReader(decodedString), null, "TURTLE")
	                println "Model loaded successfully with UTF-16 fallback."
	                return model
	
	            } catch (Exception finalEx) {
	                println "Loading with all fallbacks failed."
	                finalEx.printStackTrace()
	            }
            } catch (Exception ex2) {
                println "An unexpected error occurred during UTF-16 fallback."
                ex2.printStackTrace()
            }
        } catch (Exception ex) {
            println "An unexpected error occurred during Windows-1252 fallback."
            ex.printStackTrace()
        }
    } catch (Exception e) {
        println "An unexpected error occurred during model loading."
        e.printStackTrace()
    }
    return null
}
/**
 * Loads an Apache Jena Model from an InputStream with a robust encoding fallback.
 *
 * This method attempts to load the stream as UTF-8. If it fails, it falls back
 * to Windows-1252, and if that also fails, it attempts UTF-16. This provides
 * a comprehensive way to handle different source encodings.
 *
 * @param inputStream The InputStream containing the RDF data.
 * @return A Jena Model loaded with the correctly decoded data, or null if loading fails.
 */
def loadJenaModelWithEncodingFallback(InputStream inputStream) {
	if (!inputStream) {
		println "Input stream is null. Cannot load model."
		return null
	}
		// Read the entire stream into a byte array
		byte[] bytes = inputStream.bytes
		println new String(bytes,  "UTF-16")

	try {

		// Attempt 1: UTF-8 (most common)
		println "Attempting to load model with UTF-8 encoding..."
		def decodedString = new String(bytes, 'UTF-8')
		def model = ModelFactory.createDefaultModel()
		model.read(new StringReader(decodedString), null, "TURTLE")
		println "Model loaded successfully with UTF-8 encoding."
		return model

	} catch (CharacterCodingException|RiotException e) {
		// This catch block handles the case where the input stream is NOT valid UTF-8.
		println "UTF-8 decoding failed. Falling back to Windows-1252..."
		try {
			// Attempt 2: Windows-1252 (common for legacy files)
			def decodedString = new String(bytes, 'Windows-1252')
			
			def model = ModelFactory.createDefaultModel()
			model.read(new StringReader(decodedString), null, "TURTLE")
			println "Model loaded successfully with Windows-1252 fallback."
			return model

		} catch (CharacterCodingException|RiotException ex) {
			// This catch block handles the case where the input is neither UTF-8 nor Windows-1252.
			println "Windows-1252 decoding failed. Falling back to UTF-16..."
			try {
				// Attempt 3: UTF-16 (another common encoding)
				def decodedString = new String(bytes, 'UTF-16')
				
				def model = ModelFactory.createDefaultModel()
				model.read(new StringReader(decodedString), null, "TURTLE")
				println "Model loaded successfully with UTF-16 fallback."
				return model

			} catch (Exception finalEx) {
				println "Loading with all fallbacks failed."
				finalEx.printStackTrace()
			}
		} catch (Exception ex) {
			println "An unexpected error occurred during Windows-1252 fallback."
			ex.printStackTrace()
		}
	} catch (Exception e) {
		println "An unexpected error occurred during model loading."
		e.printStackTrace()
	}
	return null
}

}
