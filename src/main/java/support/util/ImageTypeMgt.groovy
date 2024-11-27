package support.util

import static org.junit.jupiter.api.Assertions.*

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import org.junit.jupiter.api.Test
import javax.imageio.ImageIO
import java.nio.charset.StandardCharsets
import java.nio.file.*

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
class ImageTypeMgt {
	
	
	def handleUpload(m) {
		def iname = m.fileupload
		if (!iname) 
			return "No file selected, type change not performed."
		def dir = m.directory
		def n = iname.lastIndexOf(".")
		def ext = iname.substring(n)
		def name = iname.substring(0,n)
		def outExt = m.format.toLowerCase()
		def oname = "${name}.$outExt" 
		def ls = convertDriver(dir,dir,iname,oname)

		def s = ""
		ls.each{
			s += "$it\n"

		}
		s
	}

	// change file type
	// returns list of status strings
	// https://usage.imagemagick.org/resize/#scale
	def convertDriver(src,tgt,iname,oname) {
		def ls = []
		def initSize=0
		ls += "from $src/$iname"
		ls += "to $tgt/$oname"
		def cmd = "magick $src/$iname $tgt/$oname"
		def s = new util.Exec().exec cmd
		ls
	}

}
