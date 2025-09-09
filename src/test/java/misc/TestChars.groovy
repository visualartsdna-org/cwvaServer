package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import java.nio.charset.Charset
import java.nio.charset.CharacterCodingException
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

class TestChars {
	
	
	def s = """
        n Elephant Ear, the artist uses the delicate medium of watercolor to capture a luminous, almost breath-held moment in nature. The modest scale—15 by 11 inches—invites an intimate viewing experience. It draws the observer closer, encouraging quiet attention, much like the early morning light that inspires the piece.

Watercolor, inherently fluid and translucent, is a fitting choice for a subject defined by light filtering through soft foliage. The artist uses this medium with control and sensitivity. The luminous lime greens of the elephant ear leaves seem to glow from within—an effect made possible by skillful layering and an understanding of how watercolor pigments interact with paper and water. The shadows, rendered in deep ultramarine and indigo tones, suggest both the physical structure of the leaves and the directional play of light without resorting to harsh outlines.

The contrast between the vibrant leaves and the richly textured background is especially effective in watercolor. Where many might allow the background to fade or wash out, this artist embraces the expressive potential of wet-on-wet techniques and dry brushwork to suggest the irregular surface of a forest floor or damp earth. Splatters, pooling, and layering are used not as accidents but as visual tools, grounding the ethereal leaves in a believable physical environment.

The scale of the work contributes to its intimacy. At 15" x 11", the piece avoids the overwhelming grandeur of large-format botanical illustration, opting instead for a human-scaled encounter—like noticing something beautiful at your feet on a quiet morning walk. The viewer is drawn into a moment of quiet revelation, framed in humble dimensions but filled with atmospheric weight.

Compositionally, the angled, overlapping leaves provide dynamic movement against the more amorphous background. This diagonal orientation pulls the eye from the lower right to the upper left, echoing the source of the light itself. Such a choice not only guides the viewer’s gaze but also enhances the sense of a natural unfolding—leaves growing toward the light, or light discovering the leaves.

As a watercolor, Elephant Ear is not merely a representation of a plant but a meditation on light, form, and temporality. The medium’s transparency echoes the subject’s translucency, and the painting’s restrained scale and luminous palette honor the quiet beauty of a fleeting encounter with nature. The work succeeds both as an observational study and as a poetic impression—controlled yet free, grounded yet glowing.

"""

	
	@Test
	void test4() {
		// --- Example Usage ---
		
		// Example 1: Valid UTF-8 string
		def validString = "Hello, world! "
		def validBytes = validString.getBytes('UTF-8')
		println "Testing valid UTF-8 bytes: ${isValidUtf8(validBytes)}"
		
		// Example 2: Invalid UTF-8 string
		// This byte array contains the hex value 0x92, which is not a valid
		// starting byte for a multi-byte UTF-8 character.
		def invalidBytes = [0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x2C, 0x20, 0x92] as byte[]
		println "Testing invalid UTF-8 bytes: ${isValidUtf8(invalidBytes)}"
		
	}
	
/**
 * Checks if a byte array contains valid UTF-8 characters by using a strict decoder.
 *
 * This method creates a CharsetDecoder and explicitly sets its
 * onMalformedInput action to REPORT, which forces it to throw a
 * CharacterCodingException on any invalid byte sequences.
 *
 * @param bytes The byte array to be validated.
 * @return true if the bytes represent a valid UTF-8 sequence, false otherwise.
 */
def isValidUtf8(byte[] bytes) {
    if (bytes == null) {
        return false
    }

    try {
        // Get a UTF-8 charset decoder
        Charset utf8 = Charset.forName('UTF-8')
        def decoder = utf8.newDecoder()

        // Set the decoder to be strict, so it will throw an exception
        // on any malformed input.
        decoder.onMalformedInput(CodingErrorAction.REPORT)

        // Attempt to decode the byte buffer.
        decoder.decode(ByteBuffer.wrap(bytes))

        return true
    } catch (CharacterCodingException e) {
        // If a CharacterCodingException is caught, the bytes were not valid UTF-8.
        println "Malformed UTF-8 sequence detected: ${e.message}"
        return false
    }
}
	

	
	@Test
	void test3() {
		//def fs = "C:/stage/tmp/ElephantEarCriticism20250804075002.ttl"
		def fs = "C:/stage/metadata/tags/ElephantEarCriticism20250804075002.ttl"
		try {
			def data = new JenaUtilities().loadFiles(fs)
				println "ok"
			} catch (org.apache.jena.atlas.RuntimeIOException rioe) {
				println "$rioe"
				//println "${rioe.detailMessage}"
				if ((""+rioe).contains("MalformedInputException"))
					println "yes"
			} catch (org.apache.jena.riot.RiotException re) {
				throw new RuntimeException("$fs, $re")
			} catch (Exception e) {
				throw new RuntimeException("$fs, $e")
			}

	}
	@Test
	void test() {
// Example usage that simulates reading the raw bytes from a file.
// The byte array below contains the raw hex values for ' and —.
def fileBytes = [0x92, 0x97] as byte[]

// Decode the raw bytes directly as a Windows-1252 string
def correctlyDecodedString = decodeWindows1252(fileBytes)

println "Correctly Decoded String: ${correctlyDecodedString}"
	}

	@Test
	void test2() {
// Decode the raw bytes directly as a Windows-1252 string
def correctlyDecodedString = decodeWindows1252(s.getBytes())

println "Correctly Decoded String: ${correctlyDecodedString}"
	}
	
/**
 * Decodes a byte array from Windows-1252 to a Groovy String.
 *
 * This method is the correct way to handle a file that was saved with
 * Windows-1252 encoding but needs to be interpreted as a String.
 *
 * @param windows1252Bytes The raw byte array of the file content.
 * @return A new String with characters correctly decoded from Windows-1252.
 */
def decodeWindows1252(byte[] windows1252Bytes) {
    if (windows1252Bytes == null) {
        return null
    }
    
    return new String(windows1252Bytes, 'Windows-1252')
}


/**
 * Converts a Groovy String (which is internally UTF-16) into a
 * UTF-8 encoded byte array.
 *
 * @param inputString The Groovy String to convert.
 * @return A byte array containing the UTF-8 encoded representation of the string.
 */
def stringToUtf8Bytes(String inputString) {
	if (inputString == null) {
		return null
	}

	// The getBytes() method is the standard way to encode a String
	// into a byte array using a specified character set.
	return new String(inputString.getBytes('UTF-8'))
}

@Test
void test5() {
// Example usage that simulates reading the raw bytes from a file.
// The byte array below contains the raw hex values for ' and —.
def fileBytes = [0x92, 0x97] as byte[]

// Decode the raw bytes directly as a Windows-1252 string
def correctlyDecodedString = stringToUtf8Bytes(s)

println "Correctly Decoded String: ${correctlyDecodedString}"
}

@Test
void test9() {
	def s = "this is a quote: \"\"\"now is the \"time\"\"\"\""
	
	def b = s ==~ /.*""""$/
	println b
}
@Test
void test9a() {
	def s = "this is a quote: \"now is the time\""
	
	println "${fixQuote(s)}|"
	
}

def fixQuote(s) {
	
	if (s ==~ /.*"$/)
		s = s += " "
	s
}
	
}
