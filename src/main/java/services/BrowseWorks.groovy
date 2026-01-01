package services

import static org.junit.Assert.*

import org.apache.jena.rdf.model.Model

import groovy.json.JsonSlurper
import rdf.JenaUtilities
import rdf.JenaUtils
import rdf.QuerySupport
import org.junit.Test
import rdf.Prefixes


class BrowseWorks {
	
	def ju = new JenaUtilities()
	def width = 336
	def page=20
	def dsize = [[cnt:0]]

	def getData(model, artist, order, offset, limit) {
		def dir = "C:/stage/server/cwvaContent/ttl/data"
		if (artist=="all") artist = ""
		def filter = artist ? "filter(?artist = '$artist')" : ""
		def sort = order=="Title" ? "asc(?label)" : "desc(?dt)"
		if (!offset) offset = 0
		//if (!limit) 
		limit = page
		
		dsize = ju.queryListMap1(model,Prefixes.forQuery,"""
select (count(*) as ?cnt) {
		?uri a vad:CreativeWork ;
			schema:image ?image ;
			rdfs:label ?label ;
			vad:workOnSite ?site ;
			schema:dateCreated ?dt ;
			vad:hasArtistProfile/vad:artist/rdfs:label ?artist ;
			.
		${filter}
}
""")
		
		def data = ju.queryListMap1(model,Prefixes.forQuery,"""
select ?image ?label ?uri ?artist ?site {
		?uri a vad:CreativeWork ;
			schema:image ?image ;
			rdfs:label ?label ;
			vad:workOnSite ?site ;
			schema:dateCreated ?dt ;
			vad:hasArtistProfile/vad:artist/rdfs:label ?artist ;
			.
		${filter}
} order by $sort
	offset $offset
	limit $limit
""")
		data
	}

	def isMobile
	def browse(host,model,mq) {
		
		isMobile = mq.isMobile == "true"
		def data = getData(model, mq.artist, mq.order, mq.offset, mq.limit)
		html(data, host, mq)
		
	}

	def html(data, host, mq) {

		def html = HtmlTemplate.head(host, "white")
		int pages=1
		if (isMobile) {
			html += """
<style>
.pagination {
  display: flex;
  justify-content: center;
  list-style: none; /* remove list bullets */
  padding: 0px;
  /* Allow items to wrap if they run out of space, although hiding them below is usually better */
  flex-wrap: wrap; 
}

.pagination li a {
  display: block; /* let links fill the list item */
  /* Increase padding to ensure a good touch target size (~44px minimum height/width) */
  padding: 12px 16px; 
  text-decoration: none;
  border: 1px solid gray;
  color: black;  
  margin: 0 4px;
  border-radius: 5px; /* add rounded borders */
  /* Make sure buttons don't shrink too much */
  flex-shrink: 0;
}
.pagination li a.active {
  background-color: cornflowerblue;
  color: white;
}
.pagination li a:hover:not(.active) {
  background-color: lightgray;
}

/* --- Mobile Responsiveness Additions --- */

/* 1. Add the Viewport Meta Tag to your HTML head (VERY IMPORTANT) */
/*    <meta name="viewport" content="width=device-width, initial-scale=1.0"> */

/* 2. Hide most page numbers on small screens (e.g., phones below 600px) */
@media only screen and (max-width: 600px) {
  .pagination li a {
    /* By default, hide all individual page numbers */
    display: none;
  }

  .pagination li a.active, /* ALWAYS show the current page */
  .pagination li.prev-next a, /* Ensure 'Previous'/'Next' buttons are always visible */
  .pagination li.start-end a { /* Optional: Show 'First'/'Last' page if needed */
    display: block; 
  }
}
.pagination li a.disabled {
  /* Change the color to grey and remove hover effects */
  color: lightgray;
  border-color: lightgray;
  cursor: not-allowed; /* Changes the mouse icon to a "stop" symbol */
  pointer-events: none; /* Prevents the link from being clickable even if tapped/clicked */
  background-color: white; /* Ensure it stays white/transparent */
}

/* Optional: Ensure disabled links don't have a hover effect */
.pagination li a.disabled:hover {
    background-color: white; 
}
</style>

"""
		} else {
			
		
		html += """
<style>
.pagination {
  display: flex;
  justify-content: center;
  list-style: none; /* remove list bullets */
  padding: 0px;
}

.pagination li a {
  display: block; /* let links fill the list item */
  padding: 8px 12px;
  text-decoration: none;
  border: 1px solid gray;
  color: black;  
  margin: 0 4px;
  border-radius: 5px; /* add rounded borders */
}
.pagination li a.active {
  background-color: cornflowerblue;
  color: white;
}
.pagination li a:hover:not(.active) {
  background-color: lightgray;
}
</style>
"""
		}
		
		//def page = 1
		
		html += """
<table style="width: 100%;">
        <colgroup>
            <col style="width: 30%;"> <!-- First column width -->
            <col style="width: 60%;"> <!-- Second column width -->
         </colgroup>

<tr><td>
"""
		
		html += """
<script>
function orderFunction() {
  document.getElementById("myForm").submit();
}
</script>
<h4>
<form id="myForm" action="/browseSort" method="get">
Order:
<input type="radio" id="date" name="order" onclick="orderFunction()" value="Date" ${mq.order=="Date" ? "checked" : ""}>
<label for="date">Date</label>
<input type="radio" id="title" name="order" onclick="orderFunction()" value="Title" ${mq.order=="Title" ? "checked" : ""}>
<label for="title">Title</label>
<input type="hidden" name="artist" value="${mq.artist}">
<input type="hidden" id="page" name="page" value="$pages">
</form>
"""
		html += """
<form id="auto-submit-form" action="/browseFilter" method="get">
  <label for="artist-select">Filter by artist:</label>
  <select name="artist" id="artist-select">
    <option value="all" ${mq.artist=="All" ? "selected" : ""}>All</option>
    <option value="Rick Spates" ${mq.artist=="Rick Spates" ? "selected" : ""}>Rick Spates</option>
    <option value="rspates" ${mq.artist=="rspates" ? "selected" : ""}>rspates</option>
  </select>
	<input type="hidden" name="order" value="${mq.order}">
	<input type="hidden" id="page" name="page" value="$pages">
</form>
</h4>

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
"""
		
		html += """


</td><td>

<p style="font-family: Arial, sans-serif; font-size: 16px;"><b>
Welcome to VisualArtsDNA—an online art gallery. 
Feel free to explore. </b>This site began as a project to understand 
how information connects to artwork. That effort grew 
into a structured information model, now an evolving ontology 
with vocabularies and linked concepts that document each piece. 
While the model lives in the background, the gallery up front invites 
you to enjoy the artwork directly.</p>

</td></tr></table>


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
      // We use innerHTML so the browser renders the tags
      cellWrapper.innerHTML = `
<table><tr><td>
<a href="${cwva.Server.rehost(m.uri)}">
<img src="${cwva.Server.rehost(m.image)}" width="${width}" />
</a>
</td></tr><tr><td><center>
<a href="${cwva.Server.rehost(m.uri)}">
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
"""
		if (isMobile) {
			def lp=[]
			// figure out all the page params first
			int l=0 // recalls last value of i, for offset last page
//			int pages=1 // count of the pages
			int k=0 // for limit last page
			int dataSize = dsize[0].cnt as int
			// i is the increment by page size of works
	
			for (int i= 0; i<dataSize; i+=page) {
				k = Math.min(i + page,dataSize)
				l = i
				def mp=[pg:pages,oset:i,lim:k]
				lp += mp
				pages++
				l=i
			}
			
			int pg = mq.page as int
	
				html += """
<ul class="pagination">
  <li class="prev-next">
"""
			if (pg==1) {
				html+= """
<a class="disabled" href="#">&laquo; Previous</a>
  </li>
"""
			} else {
				html+= """
<a href="$host/browseFilter?artist=${mq.artist}&order=${mq.order}&
offset=${lp[pg-2].oset}&
limit=${lp[pg-2].lim}&
page=${lp[pg-2].pg}">&laquo; Previous</a>
  </li>
"""
			}
					html += """
  <li><a class="active" href="$host/browseFilter?artist=${mq.artist}&order=${mq.order}&
offset=${lp[pg-1].oset}&
limit=${lp[pg-1].lim}&
page=${lp[pg-1].pg}">
$pg</a></li>
"""
			if (pg==pages-1) {
				html += """
   <li class="prev-next">
<a class="disabled" href="#">Next &raquo;</a>
   </li>
 </ul>
"""
			} else {
				html += """
   <li class="prev-next">
<a href="$host/browseFilter?artist=${mq.artist}&order=${mq.order}&
offset=${lp[pg].oset}&
limit=${lp[pg].lim}&
page=${lp[pg].pg}">Next &raquo;</a>
   </li>
 </ul>
"""
					}
		} else {  // not mobile
			html += """


<ul class="pagination">
  <li><a href="$host/browseFilter?artist=${mq.artist}&order=${mq.order}&offset=0&limit=$page&page=1">&laquo;</a></li>

"""
//		int pages=1
		int k=0
		int l=0
		int dataSize = dsize[0].cnt as int
		for (int i= 0; i<dataSize; i+=page) {
			k = Math.min(i + page,dataSize)
			l = i
			html += """
  <li><a ${mq.page == ""+pages ? "class=\"active\"": ""} href="$host/browseFilter?artist=${mq.artist}&order=${mq.order}&offset=${i}&limit=${k}&page=$pages">$pages</a></li>
"""
			pages++
		}

		html += """
  <li><a href="$host/browseFilter?artist=${mq.artist}&order=${mq.order}&offset=${l}&limit=${k}&page=${pages-1}">&raquo;</a></li>
</ul>
"""
		}
		
		html += """

</body>
</html>
"""
		html += HtmlTemplate.tail
		html
	}

}
