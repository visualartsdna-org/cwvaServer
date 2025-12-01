package images

import org.jcodec.api.FrameGrab
import org.jcodec.api.JCodecException
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Picture
import org.jcodec.scale.AWTUtil
import org.junit.Before
import org.junit.Test
import org.junit.After
import static org.junit.Assert.*

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Test class for extracting evenly spaced frames from MP4 files using JCodec.
 *
 * Maven Dependencies (add to pom.xml):
 * <dependencies>
 *     <dependency>
 *         <groupId>org.jcodec</groupId>
 *         <artifactId>jcodec</artifactId>
 *         <version>0.2.5</version>
 *     </dependency>
 *     <dependency>
 *         <groupId>org.jcodec</groupId>
 *         <artifactId>jcodec-javase</artifactId>
 *         <version>0.2.5</version>
 *     </dependency>
 *     <dependency>
 *         <groupId>junit</groupId>
 *         <artifactId>junit</artifactId>
 *         <version>4.13.2</version>
 *         <scope>test</scope>
 *     </dependency>
 * </dependencies>
 *
 * No external software installation required - pure Java solution!
 */
class MP4FrameExtractorTest {
	
	def dir = "C:/stage/tmp/DualT5"
	def mp4 = "$dir/Recording 2025-11-21 095913.mp4"
	private Path outputDir = Paths.get(dir)
	
	@Before
	void setUp() {
		// Create temporary output directory
		outputDir = Files.createTempDirectory(outputDir,"frame_extraction_test")
		println "Output directory: ${outputDir}"
	}
	
	//@After
	void tearDown() {
		// Clean up test files (comment out to keep files for inspection)
		if (outputDir && Files.exists(outputDir)) {
			outputDir.toFile().listFiles()?.each { it.delete() }
			Files.delete(outputDir)
		}
	}
	
	@Test
	void testExtractFramesFromMP4() {
		// Path to your test MP4 file
		String inputFile = mp4
		
		// Verify input file exists
		assertTrue("Input file must exist: ${inputFile}",
				   new File(inputFile).exists())
		
		// Extract 10 frames
		int frameCount = 10
		List<File> extractedFrames = extractFrames(inputFile, outputDir.toString(), frameCount)
		
		// Verify correct number of frames extracted
		assertEquals("Should extract ${frameCount} frames",
					 frameCount, extractedFrames.size())
		
		// Verify all files exist and have content
		extractedFrames.each { file ->
			assertTrue("Frame file should exist: ${file.name}", file.exists())
			assertTrue("Frame file should not be empty", file.length() > 0)
			println "Extracted: ${file.name} (${file.length()} bytes)"
		}
	}
	
	/**
	 * Extracts evenly spaced frames from an MP4 file
	 *
	 * @param inputPath Path to the input MP4 file
	 * @param outputPath Directory where frames will be saved
	 * @param numFrames Number of frames to extract (8-12 recommended)
	 * @return List of extracted frame files
	 */
	List<File> extractFrames(String inputPath, String outputPath, int numFrames) {
		assert numFrames >= 8 && numFrames <= 12,
			   "numFrames should be between 8 and 12"
		
		File inputFile = new File(inputPath)
		List<File> extractedFiles = []
		
		try {
			// Get total frame count
			int totalFrames = getTotalFrameCount(inputFile)
			println "Total frames in video: ${totalFrames}"
			
			// Calculate frame interval
			int frameInterval = (int) Math.floor(totalFrames / (numFrames + 1))
			println "Extracting every ${frameInterval}th frame"
			
			// Extract frames at calculated intervals
			(1..numFrames).each { frameNum ->
				int framePosition = frameInterval * frameNum
				String outputFile = "${outputPath}/frame_${String.format('%03d', frameNum)}.jpg"
				
				// Extract and save frame
				Picture picture = FrameGrab.getFrameFromFile(inputFile, framePosition)
				BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture)
				
				File frameFile = new File(outputFile)
				ImageIO.write(bufferedImage, "jpg", frameFile)
				
				extractedFiles << frameFile
				println "Extracted frame ${frameNum} (position: ${framePosition})"
			}
			
		} catch (JCodecException e) {
			throw new RuntimeException("Error extracting frames: ${e.message}", e)
		}
		
		return extractedFiles
	}
	
	/**
	 * Gets the total number of frames in the video
	 */
	private int getTotalFrameCount(File videoFile) throws JCodecException {
		int frameCount = 0
		def grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile))
		
		while (grab.getNativeFrame() != null) {
			frameCount++
		}
		
		return frameCount
	}
	
	//@Test
	void testExtractWithCustomOutputLocation() {
		String inputFile = mp4
		String customOutput = System.getProperty("java.io.tmpdir") + "/custom_frames"
		
		// Create custom output directory
		File customDir = new File(customOutput)
		customDir.mkdirs()
		
		try {
			List<File> frames = extractFrames(inputFile, customOutput, 8)
			
			assertEquals(8, frames.size())
			frames.each { file ->
				assertTrue(file.absolutePath.startsWith(customOutput))
			}
			
			println "Successfully extracted to custom location: ${customOutput}"
		} finally {
			// Cleanup
			customDir.listFiles()?.each { it.delete() }
			customDir.delete()
		}
	}
	
	//@Test
	void testExtractMaximumFrames() {
		String inputFile = "src/test/resources/sample.mp4"
		
		// Test with maximum recommended frames
		List<File> frames = extractFrames(inputFile, outputDir.toString(), 12)
		
		assertEquals(12, frames.size())
		
		// Verify sequential numbering
		frames.eachWithIndex { file, idx ->
			String expectedName = "frame_${String.format('%03d', idx + 1)}.jpg"
			assertEquals(expectedName, file.name)
		}
		
		println "Successfully extracted maximum 12 frames"
	}
}