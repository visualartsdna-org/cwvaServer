package support

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D

class ImageMgt2 extends ImageMgt {
	
	/**
	 *
	 * @param ifile, the image
	 * @param i2file, the work qrcode
	 * @param i3file, the signature qrcode
	 * @param ofile, the output file
	 * @return
	 */
	def static overlay(ifile,ofile,signature) {
		
		int grcSize=80
		
		// load source images
		BufferedImage image = ImageIO.read(new File(ifile));
		
		// create the new image, canvas size is the max. of both image sizes
		int w = image.getWidth() // Math.max(image.getWidth(), overlay.getWidth());
		int h = image.getHeight() //Math.max(image.getHeight(), overlay.getHeight());
		BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		
		// paint both images, preserving the alpha channels
		Graphics2D g = combined.getGraphics();
		g.drawImage(image, 0, 0, null);
		
		
// can you detect RGB values in area of signature and 
// pick black or white appropriately
		
		def sigWidth = 180
		def sigHeight = 40
		def hproportion = 0.98
		def wproportion = 0.9
		
// a way to apply a font-based signature
		def doWhite = detectPixValue(image,hproportion,wproportion)
		g.setPaint(doWhite ? Color.lightGray : Color.darkGray)
		
		// font size is a proportion of height
		def size = (Math.max(h,w) / 1028 * 8) as int
		size = Math.min(size,35)
		size = Math.max(size,10)
		
		g.setFont (new Font ("Arial", Font.PLAIN, size))
		g.drawString(signature,
              w  * wproportion,
              h  * hproportion
//              image.getWidth() - sigWidth,
//              image.getHeight() - sigHeight
			  )
		
		g.dispose();
		
		ImageIO.write(combined, "jpeg", new File(ofile));
	}

	// bool return avg pix value > 800000
	static def detectPixValue(bi,hproportion,wproportion) {
		def sum = 0
		def h=bi.getHeight()
		def w=bi.getWidth()
		
		def h0 = bi.getHeight() * hproportion
		def w0 = bi.getWidth() * wproportion
		
		def c=0
		for (int i=w0;i<w;i++) {
			for (int j=h0;j<h;j++) {
				def rgb = bi.getRGB(i,j) & 0xFFFFFF
				sum += (rgb < 0x800000) ? 1 : -1
			}
		}
//			def rgb = it & 0xFFFFFF
//			int alpha = (rgb >> 24) & 0xFF;
//			int red =   (rgb >> 16) & 0xFF;
//			int green = (rgb >>  8) & 0xFF;
//			int blue =  (rgb      ) & 0xFF;
			
		sum > 0
	}
	
	// bool return avg pix value > 800000
//	static def detectPixValue(bi,sw,sh) {
//		def l = []
//		def h=bi.getHeight()
//		def w=bi.getWidth()
//		
//		def h0 = bi.getHeight() - sh
//		def w0 = bi.getWidth() - sw
//		
//		def c=0
//		for (int i=w0;i<w;i++) {
//			for (int j=h0;j<h;j++) {
//				l += bi.getRGB(i,j)
//				c++
//			}
//		}
//		def sum = 0
//		l.each{
//			def rgb = it & 0xFFFFFF
////			int alpha = (rgb >> 24) & 0xFF;
////			int red =   (rgb >> 16) & 0xFF;
////			int green = (rgb >>  8) & 0xFF;
////			int blue =  (rgb      ) & 0xFF;
//			
//			sum += (rgb < 0x800000) ? 1 : -1
//		}
//		sum > 0
//	}
	
	def static makeStampedFile(guid,ifile,title,dir,signature) {

		def ns = "http://visualartsdna.org/work"
		def factor=0.64
		int size=125 // default 125
		makeStampedFile(guid,ifile,title,dir,ns,factor,size,signature)
	}
		
	def static makeStampedFile(guid,ifile,title) {
		def dir = "/stage/temp"
		makeStampedFile(guid,ifile,title,dir,"qrc right")
	}
		
	// factor,size not used
	def static makeStampedFile(guid,ifile,title,dir,ns,factor,size,signature) {
		def fname = "${util.Text.camelCase(title)}.jpg"
		def ofile = "$dir/$fname"
		
		if (false) {  // 
			
			def qrcFile = qrcode(guid,dir,ns,size)
			BufferedImage bi1 = ImageIO.read(new File(qrcFile));
			def h = bi1.getHeight()
			def w = bi1.getWidth()
			def bi2 = ImageMgt.scale(bi1, BufferedImage.TYPE_INT_RGB, (w*factor) as int, (h*factor) as int, factor, factor)
		} 
			ImageMgt2.overlay("$dir/$ifile", ofile, signature)
		fname
	}

}
