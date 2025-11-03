package services

import java.text.SimpleDateFormat

class HtmlTemplate {
	// http://clipart-library.com/clipart/dna-cliparts_4.htm
	static def head(host) {
		head(host, "#FFFFFF")
	}
	static def head(host, bgColor) {
		"""
<html>
<head>
<!-- Google tag (gtag.js) -->
<script async src="https://www.googletagmanager.com/gtag/js?id=G-0GRY55G849"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', 'G-0GRY55G849');
</script>
<link rel="icon" href="$host/images/dblHelix.png">
<!-- <title>About: http://visualartsdna.org/work/6360c068-de32-4917-80a3-0dfd8b0175c9</title> -->
<title>VisualArtsDNA</title>
<link href='https://fonts.googleapis.com/css?family=Krub' rel='stylesheet'>
<style>
<!-- -->
    body {
        background-color: $bgColor
    }


/* scope EVERYTHING to .top-nav */
.top-nav {
  background-color: #eee;
  font-family: 'Krub', sans-serif;
  /* optional: make it full width + fixed height */
  /* padding: 0 .5rem; */
}

.top-nav__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;           /* modern way instead of float */
  gap: 1.5rem;             /* space between items */
  align-items: center;
  height: 3.2rem;
}

.top-nav__item {
  /* we don't need float anymore */
}

.top-nav__item a {
  display: block;
  font-size: 1.4rem;       /* similar visual size to your old 22px on body */
  color: steelblue;
  text-decoration: none;
  padding: 0.5rem 0;       /* vertical centering */
}

.top-nav__item a:hover {
  color: navy;
  text-decoration: underline;
}

a:link {
  color: steelblue;
  background-color: transparent;
  text-decoration: none;
}
a:visited {
  color: cornflowerblue;
  background-color: transparent;
  text-decoration: none;
}
a:hover {
  color: navy;
  background-color: transparent;
  text-decoration: underline;
}
a:active {
  color: blue;
  background-color: transparent;
  text-decoration: underline;
}
body {
    font-family: 'Krub';font-size: 22px;
}
tr:nth-child(even) {background-color: #f8f8f8;}
</style>
	<meta charset="UTF-8">
	<meta name="description" content="An ontology for the visual arts. VisualArtsDNA organizes the details of the visual arts creative process into an information model expressed in OWL.">
	<meta name="keywords" content="RDF,OWL,painting,sculpture,drawing,printmaking,ontology,model">
	<!--<meta name="author" content="Rick Spates">-->
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>VisualArtsDNA</title> <!-- #60 char max -->
	<meta name="og:title" property="og:title" content="VisualArtsDNA">
	<!--<meta name="robots" content="Noindex, Nofollow, Noarchive">-->
</head>
<body>
<small>
<nav class="top-nav">
  <ul class="top-nav__list">
  <li><a class="top-nav__item" href="$host">VisualArtsDNA</a></li>
  <li><a class="top-nav__item"href="$host/browse">Browse</a></li>
  <li><a class="top-nav__item"href="$host/vocabTree">Thesaurus</a></li>
  <li><a class="top-nav__item"href="$host/otherStuff">More</a></li>
  </ul>
</nav>
</small>
"""
//		${getSparql(host)}
		
//	<!--  <li><a href="#contact">Contact</a></li>
//	<li><a href="#about">About</a></li>
//	-->
	}
	
	static def getSparql(host) {
		cwva.Server.getInstance().cfg.sparql ? "<li><a href=\"$host/sparql\">SPARQL</a></li>" : ""
	}
	
	static def title(uriLong,uriShort) {
	def aboutTitle = """
<h3 id="title">
About:
<a href="$uriLong">$uriShort</a> 
</h3>
"""
	}
		
	static def tableHead(header1) {
		def table="""
<div><table >
<tbody>
<tr height="50">
<th class="col-xs-3">$header1</th>
</tr>
"""
	}
	
	static def tableHead(header1,header2) {
		def table="""
<div><table >
<tbody>
<tr height="50">
<th class="col-xs-3">$header1</th>
<th class="col-xs-3">$header2</th>
</tr>
"""
	}
	
	static def getYear() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy");
		df.format(new Date());
	}
	
	// <a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/4.0/80x15.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/4.0/">Creative Commons Attribution-ShareAlike 4.0 International License</a>.
	// Copyright &copy; 2020 visualartsdna.com. All Rights Reserved.
	static def tableTail = """</tbody></table></div>
"""
	
	static def tail = """
<center><font size="2" color="#666666">
<br/><hr/><br/>
<a href="mailto:visualartsdna@gmail.com"/>visualartsdna@gmail.com</a><br/>
Copyright &copy; ${getYear()} visualartsdna.org. All Rights Reserved.
<br/>
v ${cwva.Server.version}
</font></center>
</body></html>
"""

}
