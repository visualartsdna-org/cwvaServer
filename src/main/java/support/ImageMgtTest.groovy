package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Graphics2D
import java.awt.Color
import java.awt.Font

class ImageMgtTest {
	
	/**
	 * Use this test to
	 * stamp image files to replace
	 * existing image files in the archive
	 */
	@Test
	void testStamp() {
		
		def ifile = "IMG_5534.JPG"
		def title = "Nutty"
		def guid="519353e0-1c18-40ce-9574-d46a3267dabb"
		def dir = "C:/stage/data"
		def signature="left"
		def factor=0.64
		int size=125 // default 125
		def ns = "http://visualartsdna.org/work"
		def sigfile = util.FileUtil.loadImage(
			"/temp/images",
			"rsart.jpg")

		println ImageMgt.makeStampedFile(guid,ifile,title,dir,ns,sigfile,factor,size,signature)
	}
	
	@Test
	void testStamp1() {
		
		def ifile = "C:/temp/images/deckview.jpg"
		def title = "decview with stamp"
		def guid="5351f503-c8fe-45e2-8758-5db5b7778160"

		println ImageMgt.makeStampedFile(guid,ifile,title)
	}
	
	@Test
	void testStamp0() {
		def factor=0.64
		def dir = "/stage/temp"
		def ifile = "C:/temp/images/deckview.jpg"
		def title = "decview with stamp"
		
		int size=125 // default 125
		def ns = "http://visualartsdna.org/work"
		def guid="5351f503-c8fe-45e2-8758-5db5b7778160"
		def sigfile = "C:/temp/git/nals3d/rsart.jpg"

		println ImageMgt.makeStampedFile(guid,dir,ifile,title,ns,sigfile,factor,size)
	}
	

	def ifile = "C:/temp/images/deckview.jpg"
	def i2file = "C:/temp/git/nals3d/testQrc.jpg"
	def i3file = "C:/temp/git/nals3d/rsart.jpg"
	def ofile = "C:/temp/git/nals3d/test3.jpg"
	
	@Test
	void testOverlay() {
		ImageMgt.overlay(ifile,i2file,i3file,ofile)
	}
	
	
	@Test
	void testScale() {
		def dir = "C:/temp/git/nals3d"
		def ifile = "$dir/rickspates.art.jpg"
		def sbi = ImageIO.read(new File(ifile));
		def h = sbi.getHeight()
		def w = sbi.getWidth()
		def factor=0.64
		def bi = ImageMgt.scale(sbi, BufferedImage.TYPE_INT_RGB, (w*factor) as int, (h*factor) as int, factor, factor)
		File img = new File("$dir/testQrcRS.jpg")
		ImageIO.write(bi, "JPEG", img);
	}

	@Test
	void test1() {
		def dir = "C:/temp/git/nals3d"
		def ifile = "$dir/qrc_5351f503-c8fe-45e2-8758-5db5b7778160.jpg"
		def sbi = ImageIO.read(new File(ifile));
		def h = sbi.getHeight()
		def w = sbi.getWidth()
		def factor=0.64
		def bi = ImageMgt.scale(sbi, BufferedImage.TYPE_INT_RGB, (w*factor) as int, (h*factor) as int, factor, factor)
		File img = new File("$dir/testQrc.jpg")
		ImageIO.write(bi, "JPEG", img);
	}


	@Test
	void test0() {
		def ifile = "C:/test/archivo/reviewed/SemanticSensorNetworkOntology.jpg"
		def sbi = ImageIO.read(new File(ifile));
		def h = sbi.getHeight()
		def w = sbi.getWidth()
		def bi = ImageMgt.scale(sbi, BufferedImage.TYPE_INT_RGB, w/4 as int, h/4 as int, 0.25, 0.25)
		File img = new File("test.jpg")
		ImageIO.write(bi, "JPEG", img);
	}

	@Test
	public void testQRC() {
		int size=125 // default 125
		def ns = "http://visualartsdna.org/work"
		def dir = "C:/stage/rspates/select"
		def guids=[
 "da2fe60f-d299-4e87-9f7e-c07d8882165f",
		  ].each{guid->
			ImageMgt.qrcode(guid,dir,ns,size)
		}
	}
	
	@Test
	public void testQRCSig() {
		int size=125 // default 125
		def dir = "C:/temp/git/nals3d"
		ImageMgt.qrcodeSig("rickspates.art",dir,size)
	}

}
