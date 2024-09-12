package support.util

import static org.junit.jupiter.api.Assertions.*

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import org.junit.jupiter.api.Test
import javax.imageio.ImageIO
import java.nio.charset.StandardCharsets

/*
0.25 is a good scale for takeout useage

scale	last	ratio

IMG_7392CompSketch.jpg, 15mb
1.00	3.2mb	5:1		// why did it scale at 1:1?
0.75	1.91mb	8:1
0.50	1.03	15:1
0.25	0.34mb	47:1
0.10	63kb	246:1

IMG_7393CompSketch.jpg, 12.3mb
1.00	1.2mb	6:1		// why did it scale at 1:1?
0.75	1.2mb	10:1
0.50	0.65mb	19:1
0.25	0.21mb	58:1
0.10	0.04mb	305:1
1.25	2.8mb	4:1
1.50	3.6mb	3:1
2.00	5.4mb	2:1
3.00	10.4mb	1:1
4.00	14.6mb	0.8:1
*/
class ImageGraphMgtTest {
	
	
	// Use this to scale a file down
	// to a smaller size, ~5:1
	@Test
	void test() {
		def src = "G:/My Drive/art/projects/galapagos/sealion"
		def iname = "IMG_7392CompSketch.jpg"
		def scale = 1.0
		def tgt = "/temp/test"
		def ls = scaleDriver(src,tgt,iname,iname,scale)
		ls.each{
			println it
		}
	}
	
	def handleUpload(query) {
		def dir = "/temp/scale"
		def al = query.split(/&/)
		def m=[:]
		al.each {
			def av = it.split(/=/)
			if (av.size()==2)
				m[av[0]]=java.net.URLDecoder.decode(av[1], StandardCharsets.UTF_8.name())
			 }
//		m.each {k,v->
//			println "$k = $v"
//		}
		def iname = m.fileupload
		if (!iname) 
			return "No file selected, scaling not performed."
		def n = iname.lastIndexOf(".")
		def ext = iname.substring(n)
		def name = iname.substring(0,n)
		if (ext != ".jpg") throw new RuntimeException("only supports .jpg files")
		if (!m.scale) 
			return "No scale selected, scaling not performed."
		def overwrite = (m.overwrite) as boolean
		def scale = m.scale as double
		def oname = overwrite ? iname : "${name}_sc${scale}$ext" 
		def ls = scaleDriver(dir,dir,iname,oname,scale,overwrite)
		def s = ""
		ls.each{
			s += "$it\n"

		}
		s
	}

	// Scale a file down
	// returns list of status strings
	def scaleDriver(src,tgt,iname,oname,scale,overwrite) {
		def ls = []
		def initSize=0
			ls += "from $src/$iname"
			ls += "to $tgt/$oname"
			initSize = new File("$src/$iname").length()
			BufferedImage bi = ImageIO.read(new File("$src/$iname"))
			Graphics2D ig = bi.createGraphics()
			bi = ImageGraphicMgt.transform(bi,scale)
			//bi = ImageMgt.toARGB(bi)
			ls +="write=" +  ImageIO.write(ImageGraphicMgt.toRGB(bi), "JPEG", new File("$tgt/$oname"))
			def lastSize=0
			ls += "init = ${initSize}"
			ls += "last = ${lastSize = new File("$tgt/$oname").length()}"
			ls += "scale = ${(initSize/lastSize) as int} : 1"
			ls += "overwrite = $overwrite"
			ls
	}

//	@Test
	void test1() {
		def dir = "G:/My Drive/art/projects/galapagos/booby"
		def k = "IMG_7393CompSketch.jpg"
		def scale = 0.5
		def size = 0
		def tgt = "/temp/test"
		for (int i=90; i>0 && size < 10000 * 1024; i-=10) {
			scale = i/100
			println "$dir/$k"
			println "Current: ${new File("$dir/$k").length()}"
			BufferedImage bi = ImageIO.read(new File("$dir/$k"))
			Graphics2D ig = bi.createGraphics()
			bi = ImageGraphicMgt.transform(bi,scale)
			//bi = ImageMgt.toARGB(bi)
			println ImageIO.write(ImageGraphicMgt.toRGB(bi), "JPEG", new File("$tgt/$k"))
			println "scale: $scale"
			println "$tgt/$k"
			println "New: ${new File("$tgt/$k").length()}"
		}
	}
	
	@Test
	void testScaleDown() {
		def dir = "G:/My Drive/art/projects/galapagos/sealion"
		def k = "IMG_7392CompSketch.jpg"
		def scale = 1.0
		def initSize=0
		def tgt = "/temp/test"
			println k
			println "from $dir"
			println "to $tgt"
			println "init = ${initSize = new File("$dir/$k").length()}"
			BufferedImage bi = ImageIO.read(new File("$dir/$k"))
			Graphics2D ig = bi.createGraphics()
			bi = ImageGraphicMgt.transform(bi,scale)
			//bi = ImageMgt.toARGB(bi)
			println "write=" +  ImageIO.write(ImageGraphicMgt.toRGB(bi), "JPEG", new File("$tgt/$k"))
			def lastSize=0
			println "last = ${lastSize = new File("$tgt/$k").length()}"
			println "scale = ${initSize/lastSize} : 1"
			
	}

	@Test
	void test0() {
		def dir = "G:/My Drive/art/projects/waterLily"
		def k = "IMG_7231Ink.jpg"
		def scale = 0.5
		def tgt = "/temp/test"
			println k
			BufferedImage bi = ImageIO.read(new File("$dir/$k"))
			Graphics2D ig = bi.createGraphics()
			bi = ImageGraphicMgt.transform(bi,scale)
			//bi = ImageMgt.toARGB(bi)
			println ImageIO.write(ImageGraphicMgt.toRGB(bi), "JPEG", new File("$tgt/$k"))
			
	}

}
