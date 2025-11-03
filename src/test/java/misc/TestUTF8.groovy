package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.StandardCharsets


class TestUTF8 {

    // Helper method to create a temporary test file
    private File createTestFile(String content, String encoding) {
        def tempFile = File.createTempFile("test", ".txt")
        tempFile.deleteOnExit()
        tempFile.withWriter(encoding) { writer ->
            writer.write(content)
        }
        return tempFile
    }

    // Helper method to create a temporary test file
    private File loadFile() {
		new File("/stage/tmp/tensegrityVocab00.ttl")
    }

    /**
     * Core logic to check if a file is valid UTF-8.
     * It reads the file into a byte array and attempts to decode it.
     * If the decoding fails due to an invalid byte sequence, the file is not valid UTF-8.
     * @param file The file to check.
     * @return true if the file is valid UTF-8, false otherwise.
     */
    private boolean isValidUtf8(File file) {
        try {
            def bytes = Files.readAllBytes(file.toPath())
            // Attempt to decode the bytes using a strict UTF-8 decoder
            StandardCharsets.UTF_8.newDecoder()
                                 .decode(java.nio.ByteBuffer.wrap(bytes))
            return true
        } catch (java.nio.charset.MalformedInputException e) {
            // This exception is thrown if an invalid byte sequence is found
            println "Non-UTF-8 sequence found in file: ${file.name} at position: ${e.getInputLength()}"
            return false
        } catch (Exception e) {
            // Catch other potential I/O or decoding errors
            println "An error occurred while checking file: ${e.getMessage()}"
            return false
        }
    }

 private List<Character> findNonAsciiCharacters(File file) {
        def nonAsciiChars = []
        
        // Read the file content as a String. We use a non-strict encoding like 'UTF-8' 
        // to read the file first, then check the character values.
        def content = new String(Files.readAllBytes(file.toPath()), "UTF-8")

        content.eachWithIndex { c ,index ->
            // ASCII characters have a value between 0 and 127 (inclusive).
            // Groovy/Java characters are 16-bit, so we cast to int to check the value.
            if ((int)c > 127) {
                // Found a non-ASCII character
                nonAsciiChars.add(c)
                println "Non-ASCII character found: '${c}' (Unicode value: U+${Integer.toHexString((int)c).toUpperCase()}) at index ${index}"
            }
        }
        return nonAsciiChars.unique() // Return a list of unique non-ASCII characters
    }
	
private List<Character> findNonAsciiCharactersFromLines(File file) {
		def nonAsciiChars = []
		
		// Read the file content as a String. We use a non-strict encoding like 'UTF-8'
		// to read the file first, then check the character values.
		file.eachLine{content->
		//def content = new String(Files.readAllBytes(file.toPath()), "UTF-8")

		content.eachWithIndex { c ,index ->
			// ASCII characters have a value between 0 and 127 (inclusive).
			// Groovy/Java characters are 16-bit, so we cast to int to check the value.
			if ((int)c > 127) {
				// Found a non-ASCII character
				nonAsciiChars.add(c)
				println "Non-ASCII character found: '${c}' (Unicode value: U+${Integer.toHexString((int)c).toUpperCase()}) at index ${index}"
				println "\t$content"
			}
		}
		}
		return nonAsciiChars.unique() // Return a list of unique non-ASCII characters
	}

	    // ------------------- TEST CASES -------------------

	@Test
	void testTtlFileAscii() {
		def asciiFile = loadFile()
		
		def result = findNonAsciiCharactersFromLines( asciiFile)
		
		println "Testing valid ascii file..."
		assertTrue(result.isEmpty(), "The file contains no non-ASCII characters.")
	}
	@Test
	void testTtlFile() {
		def utf8File = loadFile()
		
		println "Testing valid UTF-8 file..."
		assertTrue(isValidUtf8(utf8File), "The file should be reported as valid UTF-8.")

	}
    @Test
    void testValidUtf8File() {
        def content = "This is a valid UTF-8 string with a snowman: \u2603"
        def utf8File = createTestFile(content, "UTF-8")
        
        println "Testing valid UTF-8 file..."
        assertTrue(isValidUtf8(utf8File), "The file should be reported as valid UTF-8.")
    }

//    @Test
//    void testInvalidUtf8File() {
//        // A simple way to generate an invalid sequence is to create a file
//        // with a different encoding (like ISO-8859-1) containing characters
//        // that are not valid in basic UTF-8 context.
//        // A common non-UTF-8 sequence is the byte 0xBF, which is invalid as a 
//        // leading or single byte in UTF-8 unless it's part of a multi-byte sequence.
//        
//        def invalidBytes = new byte[] {}
//            (byte)0x48, (byte)0x65, (byte)0x6c, (byte)0x6c, (byte)0x6f, (byte)0x20, (byte)0xbf  // Invalid starting byte for UTF-8
//			
////            (byte)0x48, // 'H'
////            (byte)0x65, // 'e'
////            (byte)0x6c, // 'l'
////            (byte)0x6c, // 'l'
////            (byte)0x6f, // 'o'
////            (byte)0x20, // ' '
////            (byte)0xbf  // Invalid starting byte for UTF-8
//        }
//        
//        def invalidFile = File.createTempFile("invalid", ".txt")
//        invalidFile.deleteOnExit()
//        invalidFile.withOutputStream { os ->
//            os.write(invalidBytes)
//        }
//        
//        println "Testing invalid UTF-8 file..."
//        assertFalse(isValidUtf8(invalidFile), "The file should be reported as invalid UTF-8.")
//    }
}
