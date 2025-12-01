package cwva

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import rdf.Prefixes
import services.HtmlTemplate

class BrowserTest {
	def ju = new JenaUtilities()
	def width = 300
	
	@Test
	void test() {
		def data = getData()
//		[
//			[image:"http://192.168.1.71/images/Seoul.jpg",label:"Seoul",uri:"http://192.168.1.71/work/951467b2-d8c8-4222-b394-0c15f5bf5204",artist:"Rick Spates", site:"http://rickspates.art"],
//			[image:"http://192.168.1.71/images/TensionT5Links.jpg",label:"Tension T5 Links",uri:"http://192.168.1.71/work/8e69fb3e-bd56-43d8-b8e3-6ced8d8fcfb4",artist:"rspates", site:"http://rspates.art"],
//			[image:"http://192.168.1.71/images/BeeBalm.jpg",label:"Bee Balm",uri:"http://192.168.1.71/work/dce84a9a-1f2e-45fe-8b00-66b63b4d7587",artist:"Rick Spates", site:"http://rickspates.art"],
//			[image:"http://192.168.1.71/images/Windmills.jpg",label:"Windmills",uri:"http://192.168.1.71/work/b9913142-70be-4593-84ef-919ce08f86b3",artist:"Rick Spates", site:"http://rickspates.art"],
//			]
		def s = html(data)
		new File("/stage/tmp/junk.html").text = s
	}
	
	def getData() {
		def order = "date"
		def offset = 0
		def limit = 200
		getData("all", order, offset, limit)
	}
	def getData(artist, order, offset, limit) {
		def dir = "C:/stage/server/cwvaContent/ttl/data"
		
		def filter = artist ? "filter(?artist = '$artist')" : ""
		def sort = order=="title" ? "asc(?label)" : "desc(?dt)"
		def mod = ju.loadFiles(dir)
		def data = ju.queryListMap1(mod,Prefixes.forQuery,"""
select ?image ?label ?uri ?artist ?site {
		#?uri a vad:CreativeWork ;
		?uri a ?type ;
			schema:image ?image ;
			rdfs:label ?label ;
			vad:workOnSite ?site ;
			schema:dateCreated ?dt ;
			vad:hasArtistProfile/vad:artist/rdfs:label ?artist ;
			.
		# temporary filter
		filter (?type in (vad:Watercolor, vad:BlenderComposition))
		${filter}
} order by $sort
	offset $offset
	limit $limit
""")
		data
	}

	def html(data) {
		def qm = [order:"Date"]
		def html = HtmlTemplate.head("http://192.168.1.71/", "white")
		
		html += """
<script>
function orderFunction() {
  document.getElementById("myForm").submit();
}
</script>
<h6>
<form id="myForm" action="/browseSort" method="get">
Order:
<input type="radio" id="date" name="order" onclick="orderFunction()" value="Date" ${qm.order=="Date" ? "checked" : ""}>
<label for="date">Date</label>
<input type="radio" id="title" name="order" onclick="orderFunction()" value="Title" ${qm.order=="Title" ? "checked" : ""}>
<label for="title">Title</label>
<!--
<br><label for="archived" align="right">Archived</label>
<input type="checkbox" id="archived" name="archived" onclick="myFunction()" value="Archived" ${qm.archived ? "checked" : ""}>
-->
</form>
"""
		html += """
<script>
const artistSelect = document.getElementById('artist-select');
const artistOutput = document.getElementById('artist-output');

// Add a 'change' event listener to the select element
artistSelect.addEventListener('change', () => {
  // Get the form and submit it
  const form = document.getElementById('auto-submit-form');
  form.submit();
});
</script>
<form id="auto-submit-form" action="/browseFilter" method="get">
  <label for="artist-select">Filter by artist:</label>
  <select name="artist" id="artist-select">
    <option value="all">All</option>
    <option value="Rick Spates">Rick Spates</option>
    <option value="rspates">rspates</option>
  </select>
</form>
</h6>

<p id="artist-output"></p>
"""
		
		html += """
<style>
  /* 1. The Outer Grid Container */
  #table-container {
    display: grid;
    /* Logic: Fill the row with as many columns as possible. 
       Constraint: Each column must be at least 150px wide.
    */
    grid-template-columns: repeat(auto-fill, minmax(${width}px, 1fr));
    gap: 20px; /* Space between grid items */
    padding: 10px;
  }

  /* 2. The Grid Item Wrapper */
  .grid-item {
    /* Optional styling to visualize the grid cell area */
    //background-color: #f9f9f9;
    //border: 1px dashed #ccc;
    padding: 10px;
    
    /* Centers your inner table inside the grid cell */
    display: flex;
    justify-content: center;
    align-items: center;
  }

  /* 3. Styling for YOUR inner table (Optional, for visibility) */
  .grid-item table {
    //border: 1px solid red; /* Red border to prove it's a table */
    background: white;
    width: 100%; /* Make the inner table fill the grid cell */
  }
</style>

<div id="table-container"></div>

<script>
  const M = ${data.size()}; // Number of items to generate
  const container = document.getElementById('table-container');

  function generateTable() {
    container.innerHTML = '';

    //for (let i = 1; i <= M; i++) {

""" 
		data.each{m->
			html += """
{
      const cellWrapper = document.createElement('div');
      // Create the grid item wrapper
      cellWrapper.className = 'grid-item';
      // INSERT YOUR HTML STRING HERE
      // We use innerHTML so the browser renders the tags
      cellWrapper.innerHTML = `
<table><tr><td>
<a href="${m.uri}">
<img src="${m.image}" width="${width}" />
</a>
</td></tr><tr><td><center>
<a href="${m.uri}">
${m.label}
</a>
</br>
<a href="${m.site}">
${m.artist}
</a>
</center>
</td></tr></table>
      `;
      container.appendChild(cellWrapper);
    }
"""
		}
		html += """


  }

  generateTable();
</script>

</body>
</html>
"""
		html += HtmlTemplate.tail
	}
}
