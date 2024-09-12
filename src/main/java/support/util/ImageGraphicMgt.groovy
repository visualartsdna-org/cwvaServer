package support.util
import javax.imageio.ImageIO

// Borrowed from nals3d ImageMgt
//
import org.apache.commons.io.IOUtils
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.AlphaComposite

class ImageGraphicMgt {
	
	static Random random = new Random()
	
	
/**
 * Change the old pixel color to the
 * new pixel color in the buffered image
 * @param bi
 * @param oldPix
 * @param newPix
 * @return
 */
	static def flipPix(bi,oldPix,newPix) {
		
		def h=bi.getHeight()
		def w=bi.getWidth()
//		def ctms = System.currentTimeMillis()
		for (int i=0;i<w;i++) {
			for (int j=0;j<h;j++) {
				int c = bi.getRGB(i,j)
				if (c == oldPix.getRGB()) {
					bi.setRGB(i, j, newPix.getRGB())
				}
			}
		}
//		def ctms2 = System.currentTimeMillis()
//		println "${ctms2-ctms} ms"

	}
	/**
	 * 
	 * @param base base image buffered image
	 * @param color the opaque color
	 * @param c a closure of the method to call
	 * @param args the params to the method
	 * @return
	 */
	static def mask(base, color, Closure c,Map args) {
		
		int w = base.getWidth()
		int h = base.getHeight()
		BufferedImage bi = new BufferedImage(w , h, BufferedImage.TYPE_INT_ARGB)
		Graphics2D ig = bi.createGraphics()
		ig.setPaint(color)
		ig.fillRect(0, 0, w, h)
		ig.setPaint(new Color(0,0,0,0))

		c(ig,args)
		Graphics2D igBase = base.createGraphics()
		igBase.drawImage(bi, 0, 0, null)
		bi
	}
	
	static def drawPolygon(Graphics2D ig,args) {
		polygon(ig,ig.&drawPolygon,args)
	}
	
	static def fillPolygon(Graphics2D ig,args) {
		polygon(ig,ig.&fillPolygon,args)
	}
	
	/**
	 * 
	 * @param ig
	 * @param c
	 * @param args a map of values for {n, x, y}
	 * @return
	 */
	static def polygon(ig,c,args) {
		
		def xa=[] , ya=[]
		(1..args.n).each{xa += it*random.nextInt(args.x)}
		(1..args.n).each{ya += it*random.nextInt(args.y)}
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0F);
		ig.setComposite(ac);
		c(xa as int[],ya as int[],args.n)
	}
		
	/**
	 * 
	 * @param before
	 * @param mask
	 * @return
	 */
	static def mask(bi,mask) {
		Graphics2D ig = bi.createGraphics()
		def bool = ig.drawImage(mask, 0, 0, null)
		if (!bool) throw new RuntimeException("Failed to draw mask")
		bi
	}
	
	/**
	 * Create a copy of a type of image
	 * in another type from an image
	 * @param before
	 * @return
	 */
	static def toImageType(before,type) {
		int w = before.getWidth()
		int h = before.getHeight()
		BufferedImage bi = new BufferedImage(w , h, type)
		Graphics2D ig = bi.createGraphics();
		ig.drawImage(before, 0, 0, null)
		bi
	}
	
	/**
	 * Get an ARGB image
	 * from an RGB image
	 * @param before
	 * @return
	 */
	static def toRGB(before) {
		toImageType(before,BufferedImage.TYPE_INT_RGB)
	}
	
	/**
	 * Get an RGB image
	 * from an ARGB image
	 * @param before
	 * @return
	 */
	static def toARGB(before) {
		toImageType(before,BufferedImage.TYPE_INT_ARGB)
	}

	/**
	 * transform to scale
	 * @param before
	 * @param scale
	 * @return
	 */
	static def transform(before,scale) {
		int w = before.getWidth()
		int h = before.getHeight()
		BufferedImage after = new BufferedImage((w*scale) as int , (h*scale) as int,
			BufferedImage.TYPE_INT_ARGB)
		AffineTransform at = new AffineTransform()
		at.scale(scale, scale)
		AffineTransformOp scaleOp =new AffineTransformOp(at,
			AffineTransformOp.TYPE_BILINEAR)
		scaleOp.filter(before, after)
	}

	/**
	 * transform to rotate
	 * @param before
	 * @param deg
	 * @param anchorx
	 * @param anchory
	 * @return
	 */
	static def transform(before,deg,anchorx,anchory) {
		def scale = 1.0
		int w = before.getWidth()
		int h = before.getHeight()
		BufferedImage after = new BufferedImage((w*scale) as int , (h*scale) as int,
			BufferedImage.TYPE_INT_ARGB)
		AffineTransform at = new AffineTransform()
		at.rotate(Math.toRadians(deg),anchorx,anchory)
		AffineTransformOp scaleOp =new AffineTransformOp(at,
			AffineTransformOp.TYPE_BILINEAR)
		scaleOp.filter(before, after)
	}

}
