package function

import cwva.ServletBase
import util.FileUtil

class ArtistSite extends ServletBase {
	
	def cfg
	
	ArtistSite(cfg){
		this.cfg = cfg
	}
	
	def serve(path,query,response,request){
		def file = "index.html"
		
		if (path != cfg.path) {
			file = (path =~ /.*\/(.*)$/)[0][1]
		}
	
		if (request) {
			def stream = request.getInputStream()
			def data = stream.readAllBytes()
			new File("${cfg.cache}/lsys.jpg").bytes = data
		} 
		else
		switch(file) {
			
			case ~/.*\.jpg$/:
				def f = FileUtil.loadImage(cfg.dir,file)
				sendJpegFile(response,f)
				break

			case ~/.*\.gif/:
				def f = FileUtil.loadImage(cfg.dir,file)
				sendGifFile(response,f)
				break

			case ~/.*\.ico/:
				def f = FileUtil.loadImage(cfg.dir,file)
				sendIconFile(response,f)
				break

			case ~/.*\.html$/:
				sendHtmlFile(response,"${cfg.dir}/$file")
				break

			default:
				logOut "unrecognized command $path, ${query?:""}"
				break
		}
	}
	
	def logOut(s) {
		Server.getInstance().logOut(s)
	}
	
}
