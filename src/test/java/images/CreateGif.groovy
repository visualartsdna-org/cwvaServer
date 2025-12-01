package images

import static org.junit.jupiter.api.Assertions.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import org.junit.jupiter.api.Test
import com.madgag.gif.fmsware.AnimatedGifEncoder

class CreateGif {

	@Test
	void test() {
		def dir="C:/stage/tmp/DualT5"
		def output = "$dir/test.gif"
		def imgDir = "$dir/frame_extraction_test9699881003909250175"
		makeGif( imgDir, output)
	}

	def makeGif( imagesDir, outputFS) {
		AnimatedGifEncoder enc = new AnimatedGifEncoder()
		def images = []
		def imgs = new File(imagesDir).listFiles()
		imgs.each{
			def bi = ImageIO.read(it)
			images += bi
		}
		enc.start(outputFS);
		enc.setDelay(2000); // 1 frame per second
		images.each{
			enc.addFrame(it)
		}
		enc.finish();
	}
	
	// Scale a file down
	// returns list of status strings
	// https://usage.imagemagick.org/resize/#scale
	def scale(src,tgt,iname,oname,scale,overwrite) {
		def ls = []
		def initSize=0
		ls += "from $src/$iname"
		ls += "to $tgt/$oname"
		initSize = new File("$src/$iname").length()
		def cmd = "magick $src/$iname -resize ${scale * 100}% $tgt/$oname"
		def s = new util.Exec().exec cmd
		ls += "init = ${initSize}"
		def opath = Paths.get("$tgt/$oname")
		def lastSize = Files.size(opath)
		ls += "last = ${lastSize}"
		ls += "scale = ${(initSize/lastSize) as int} : 1"
		ls += "overwrite = $overwrite"
		ls
	}


}
