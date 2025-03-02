package support

import java.awt.image.BufferedImage
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import javax.imageio.ImageIO

import org.junit.jupiter.api.Test

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import net.glxn.qrgen.javase.*
import net.glxn.qrgen.core.image.ImageType

/**
 * Supports:
 * Image overlay
 * Image scaling
 * QRCode generation
 * 
 * @author ricks
 *
 */
class ImageMgt {
	
	/**
	 * 
	 * @param sbi, original buffered image
	 * @param imageType, typically BufferedImage.TYPE_INT_RGB for jpeg
	 * @param dWidth, the new image width
	 * @param dHeight, the new image height
	 * @param fWidth, image scaled width
	 * @param fHeight, image scaled height
	 * @return
	 */
	public static BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) {
		BufferedImage dbi = null;
		if(sbi != null) {
		  dbi = new BufferedImage(dWidth, dHeight, imageType);
		  java.awt.Graphics2D g = dbi.createGraphics();
		  AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
		  g.drawRenderedImage(sbi, at);
		}
		return dbi;
	  }

	  
	  /**
	   * NOT WORKING
	   * @param ifile, the image
	   * @param i2file, the work qrcode
	   * @param i3file, the signature qrcode
	   * @param ofile, the output file
	   * @return
	   */
	  def static overlay(ifile,i2file,i3file,ofile) {
		  BufferedImage overlay = ImageIO.read(new File(i2file));
		  overlay(ifile,overlay,i3file,ofile)
	  }
	  /**
	   * 
	   * @param ifile, the image
	   * @param i2file, the work qrcode
	   * @param i3file, the signature qrcode
	   * @param ofile, the output file
	   * @return
	   */
	  def static overlay(ifile,BufferedImage overlay,i3file,ofile,signature) {
		  
		  int grcSize=80
		  
		  // load source images
		  BufferedImage image = ImageIO.read(new File(ifile));
		  BufferedImage overlay2 = ImageIO.read(
			  i3file instanceof File ? i3file : new File(i3file));
		  
		  // create the new image, canvas size is the max. of both image sizes
		  int w = Math.max(image.getWidth(), overlay.getWidth());
		  int h = Math.max(image.getHeight(), overlay.getHeight());
		  BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		  
		  // paint both images, preserving the alpha channels
		  Graphics2D g = combined.getGraphics();
		  g.drawImage(image, 0, 0, null);
		  
		  if (signature=="left") {
			  g.drawImage(overlay,
				  (grcSize * 0.5) as int,
				  (image.getHeight() - grcSize * 1.5 as int),
				  null);
			  g.setPaint(Color.white)
			  
			  g.drawImage(overlay2,
				  (grcSize * 1.5) as int,
				  (image.getHeight() - grcSize * 1.5) as int,
				  null);
	
		  } else if (signature=="right") {
			  g.drawImage(overlay,
				  (image.getWidth() - grcSize * 1.5) as int,
				  (image.getHeight() - grcSize * 1.5 as int),
				  null);
			  g.setPaint(Color.white)
			  
			  g.drawImage(overlay2,
				  (image.getWidth() - grcSize * 2.5) as int,
				  (image.getHeight() - grcSize * 1.5) as int,
				  null);
		  }
  // a way to apply a font-based signature
  //		g.setPaint(Color.white)
  //
  //		g.setFont (new Font ("Segoe Script", Font.ITALIC, 45))
  //		g.drawString("rickspates.art",
  //              image.getWidth() - 400,
  //              image.getHeight() - 35
  //			  )
		  
		  g.dispose();
		  
		  ImageIO.write(combined, "jpeg", new File(ofile));
	  }
  
	  
	def static qrcode(guid,dir) {
		int size=125 // default 125
		def ns = "http://visualartsdna.org/work"
		qrcode(guid,dir,ns,size)
	}
	
	/**
	 * 
	 * @param guid, basis of the QRCode
	 * @param dir, where to write the file
	 * @param ns, with the quid forms the QRCode
	 * @param size, side of the QRCode square
	 * @return
	 */
	def static qrcode(guid,dir,ns,size) {
		def qrcName = "qrc_${guid}.jpg"
		def qrcFile = new File("$dir/$qrcName")
		if (!qrcFile.exists()) {
			File file = net.glxn.qrgen.javase.QRCode.from("$ns/$guid").to(ImageType.JPG).withSize(size,size).file()
			def newFile = new File("$dir/$qrcName")
			newFile << file.bytes
		}
		"$dir/$qrcName"
	}

	def static qrcodeSig(ns,dir,size) {
		def qrcName = "${ns}.jpg"
		def qrcFile = new File("$dir/$qrcName")
		if (!qrcFile.exists()) {
			File file = net.glxn.qrgen.javase.QRCode.from("http://$ns").to(ImageType.JPG).withSize(size,size).file()
			def newFile = new File("$dir/$qrcName")
			newFile << file.bytes
		}
		"$dir/$qrcName"
	}

	def static makeStampedFile(guid,ifile,title,dir,signature) {
		def sigfile = util.FileUtil.loadImage(
			cwva.Server.getInstance().cfg.images,
			"rsart.jpg")

		def ns = "http://visualartsdna.org/work"
		def factor=0.64
		int size=125 // default 125
		makeStampedFile(guid,ifile,title,dir,ns,sigfile,factor,size,signature)
	}
		
	def static makeStampedFile(guid,ifile,title) {
		def dir = "/stage/temp"
		makeStampedFile(guid,ifile,title,dir,"right")
	}
		
	def static makeStampedFile(guid,ifile,title,dir,ns,sigfile,factor,size,signature) {
		def fname = "${util.Text.camelCase(title)}.jpg"
		def ofile = "$dir/$fname"
		def qrcFile = qrcode(guid,dir,ns,size)
		
		BufferedImage bi1 = ImageIO.read(new File(qrcFile));
		
		def h = bi1.getHeight()
		def w = bi1.getWidth()
		def bi2 = ImageMgt.scale(bi1, BufferedImage.TYPE_INT_RGB, (w*factor) as int, (h*factor) as int, factor, factor)

		ImageMgt.overlay("$dir/$ifile",bi2,sigfile,ofile,
			signature?:"right") // default for rdf digital
		fname
	}
}
