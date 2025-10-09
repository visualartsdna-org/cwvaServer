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
		printHtml(sb,mq.work,mq.site,mq.selectBkgnd)
		sb.append HtmlTemplate.tail
		"$sb"
		
	}
	
	def parseUrl(path) {
		def uri = path.substring("work:".length())
		"http://visualartsdna.org/work/$uri"
	}
	

	def printHtml(sb,work,site,bkgVal) {
		
		// model-viewer
		sb.append """
<!--required-->
   <script src="https://cdn.tailwindcss.com"></script>
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
<script>
function setAction(x) {
	document.getElementById("myForm9").submit();
}
</script>
"""
	
		def siteName = removeProtocol(site)
		def label = qs.queryOnePropertyFromInstance(work, "rdfs:label")
		def descr = qs.queryOnePropertyFromInstance(work, "schema:description")
		def model = qs.queryOnePropertyFromInstance(work, "vad:image3d")
		
		def bkgnd = qs.queryOnePropertyFromInstance(work, "vad:background")
		def bkgndImage = qs.queryOnePropertyFromInstance(bkgnd, "schema:image")
		def bkgndLM = qs.queryBackgrounds()
		
		def optionStr = ""
		bkgndLM.sort{a,b->
			a.l <=> b.l
		}.each{m->
			optionStr += """<option value="${m.s}" ${m.s == (bkgVal ? bkgVal : bkgnd) ? "selected" : ""}>${m.l}</option>"""
		} 
		if (bkgVal)
		bkgndImage = qs.queryOnePropertyFromInstance(bkgVal, "schema:image")
		
					
		sb.append """${do3d(rehost(model),rehost(bkgndImage),site, optionStr, work, siteName)}"""
		
	def uri = work.substring("http://".length())
	sb.append """
<table><tr><td>
	<a href="${rehost(parseUrl(work))}">$label</a>. $descr
	</td></tr><tr><td>
	Visit <a href="$site">$siteName</a>.
	</td></tr></table>
"""
	}
	
	def do3d(fs,bkgnd,site, optionStr, work, siteName) {
		
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
    <model-viewer 
			id="myModelViewer"
			src="$fs"  
	camera-controls tone-mapping="neutral" shadow-intensity="0"
	${bkgnd ? bg : ""}  auto-rotate
	style="flex-grow: 1; height: 100%; background-color: lightgray;">
     </model-viewer>
		<div class="logo-overlay">
		<p>$siteName</p>
		</div>
     </div>
<table><tr><td>
<form id="myForm9" action="/modelviewer.bkgnd" method="get">
<select name="selectBkgnd" id="selectBkgnd" onchange="setAction('selectBkgnd')">
$optionStr
</option>
</select>
<!--<input type = "submit" name = "submit" value = "Select" />--></td>
<input type="hidden" id="site" name="site" value="$site">
<input type="hidden" id="work" name="work" value="$work">
</form>
</td><td>
        <!-- Toggle Button -->
        <button 
            id="toggleButton"
            onclick="toggleRotation()"
            class="mt-8 px-3 py-1 bg-teal-600 text-white text-sm font-normal rounded-full shadow-lg hover:bg-teal-700 transition duration-300 ease-in-out transform hover:scale-105 active:bg-teal-800"
        >
            Rotation On
        </button>

</td><td style="width:25%">
		${!isMobile ? icons : ""}
</td></tr></table>

    <script>
        // Get references to the elements
        const modelViewer = document.getElementById('myModelViewer');
        const toggleButton = document.getElementById('toggleButton');

        /**
         * Toggles the 'auto-rotate' attribute on the model-viewer component.
         * Also updates the button text to reflect the new state.
         */
        function toggleRotation() {
            // Check if the auto-rotate attribute is currently present
            const isRotating = modelViewer.hasAttribute('auto-rotate');

            if (isRotating) {
                // If rotating, remove the attribute to stop rotation
                modelViewer.removeAttribute('auto-rotate');
                toggleButton.textContent = "Rotation Off";
                toggleButton.classList.replace('bg-teal-600', 'bg-slate-400');
                toggleButton.classList.replace('hover:bg-teal-700', 'hover:bg-slate-700');
                toggleButton.classList.replace('active:bg-teal-800', 'active:bg-slate-800');
            } else {
                // If not rotating, add the attribute to start rotation
                modelViewer.setAttribute('auto-rotate', '');
                toggleButton.textContent = "Rotation On";
                toggleButton.classList.replace('bg-slate-400', 'bg-teal-600');
                toggleButton.classList.replace('hover:bg-slate-700', 'hover:bg-teal-700');
                toggleButton.classList.replace('active:bg-slate-800', 'active:bg-teal-800');
            }
            
            console.log('Rotation Toggled. Current state:', !isRotating ? 'Rotating' : 'Stopped');
        }

        // Initialize the button text based on the initial state in the HTML
        // This is optional but good for robustness
        window.onload = () => {
             if (modelViewer.hasAttribute('auto-rotate')) {
                toggleButton.textContent = "Rotation On";
             } else {
                toggleButton.textContent = "Rotation Off";
                // Since the model-viewer starts with 'auto-rotate', this block might not run initially,
                // but it's here for completeness if the HTML attribute changes.
             }
        };

    </script>

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
	
	def removeProtocol(site) {
		site.replaceAll("http://","").replaceAll("https://","").replaceAll("/","")
	}
	
}
