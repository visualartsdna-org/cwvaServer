package services

import rdf.QuerySupport

class ModelViewer {
	
	
	def qs
	def host
	def isMobile
	
	def process( mq, host,rdfs) {

		isMobile = mq.isMobile == "true"
		def sb = new StringBuilder()
		if (!qs) qs = new QuerySupport(rdfs)
		this.host = host
		sb.append HtmlTemplate.head(host)
		printHtml(sb,mq.work,mq.site)
		sb.append HtmlTemplate.tail
		"$sb"
		
	}
	
	def parseUrl(path) {
		def uri = path.substring("work:".length())
		"http://visualartsdna.org/work/$uri"
	}
	

	def printHtml(sb,work,site) {
		
		// model-viewer
		sb.append """
<!--required-->
<style> 
.mvDiv {
  border: 1px solid;
  margin-left: 0;
  padding: 7px; 
  width: ${isMobile ? "368" : "600"}px;
  height: 500px;
  resize: both;
  overflow: auto;
}
model-viewer {
  position: relative; 
  width: 100%;
  height: 100%;
}
.logo-overlay {
  /* This places the overlay on top of the model-viewer */
  position: absolute; 
  top: 90px;
  left: 10px;
  z-index: 100; /* Ensure it's above the 3D scene */
}

.logo-overlay p {
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.9);
  font-family: sans-serif;
  font-size: 12px;
}
.icon-text {
  font-size: 0.8rem; /* Relative to the root element's font size */
}
</style>
<script type="module" src="https://ajax.googleapis.com/ajax/libs/model-viewer/4.0.0/model-viewer.min.js"></script>
"""
	

		def label = qs.queryOnePropertyFromInstance(work, "rdfs:label")
		def descr = qs.queryOnePropertyFromInstance(work, "schema:description")
		def model = qs.queryOnePropertyFromInstance(work, "vad:image3d")
		def bkgnd = qs.queryOnePropertyFromInstance(work, "vad:background")
		def bkgndImage = qs.queryOnePropertyFromInstance(bkgnd, "schema:image")
					
		sb.append """${do3d(rehost(model),rehost(bkgndImage),site)}"""
		
	def uri = work.substring("http://".length())
	sb.append """
<table><tr><td>
	<a href="${rehost(parseUrl(work))}">$label</a>. $descr.
	</td></tr><tr><td>
	Back to <a href="http://$site">$site</a>.
	</td></tr></table>
"""
	}
	
	def do3d(fs,bkgnd,site) {
		
		def bg = """
	environment-image=$bkgnd
	skybox-image=$bkgnd
"""
		def icons = """
	<p class="icon-text">
	<img style='display:inline;' src="images/left-click.png" width="20px" height="20px">drag
	<img style='display:inline;' src="images/right-click.png" width="20px" height="20px">pan
	<img style='display:inline;' src="images/scroll.png" width="20px" height="20px">zoom
	</p>
"""
	
		
		// removed AR mode from model-viewer: ar ar-modes="webxr scene-viewer quick-look"
		// to get rid of AR button in model-viewer on IOS
		// TODO: need .glb to .usdz convert
		// ios-src="/images/female-t3-prism3.usdz"
		"""<div class="mvDiv">
    <model-viewer src="$fs"  
	camera-controls tone-mapping="neutral" shadow-intensity="0"
	${bkgnd ? bg : ""}  auto-rotate
	style="flex-grow: 1; height: 100%; background-color: lightgray;">
     </model-viewer>
		<div class="logo-overlay">
		<p>$site</p>
		</div>
     </div>
		${!isMobile ? icons : ""}
"""
	}

	def rehost(r) {
		if (r instanceof List) {
			def r2 = []
			r.each{
				r2 += it.replaceAll("http://visualartsdna.org",host)
			}
			return r2
		} else
		r.replaceAll("http://visualartsdna.org",host)
	}
	

}
