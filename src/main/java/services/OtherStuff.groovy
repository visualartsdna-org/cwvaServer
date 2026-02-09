package services

import org.junit.Test
import util.Gcp
import groovy.json.JsonSlurper
import rdf.JenaUtils
import rdf.QuerySupport

class OtherStuff {
	
	@Test
	public void test() {
		def ttl = "C:/stage/planned/node/ttl/art.ttl"
		def s = new OtherStuff().get()
		println s
		new File("C:/test/vadna/junk.html").text = s
	}
	
	def cfg=[:]
	
	OtherStuff(){
		this.cfg = cwva.Server.getInstance().cfg
	}

	def grafTblArrangement = "</tr><tr></tr><tr>" // stacked
	def graphTables = """
"""

	def get(host) {
		
	def grafTblArrangement = "</tr><tr></tr><tr>" // stacked
	
	def sb = new StringBuilder()
	
	sb.append HtmlTemplate.head(cfg.host)
	sb.append """
<style>
/* 
   VisualArtsDNA Utility Page Styles
   Aesthetic: Refined gallery/museum editorial with warm accents
*/

:root {
  --color-bg: #faf9f7;
  --color-surface: #ffffff;
  --color-text: #2d2926;
  --color-text-muted: #6b6560;
  --color-accent: #b8860b;
  --color-accent-hover: #996f0a;
  --color-border: #e8e4df;
  --color-border-light: #f0ece7;
  --shadow-soft: 0 2px 8px rgba(45, 41, 38, 0.06);
  --shadow-hover: 0 4px 16px rgba(45, 41, 38, 0.1);
  --radius: 8px;
  --transition: 0.2s ease;
}

body {
  background-color: var(--color-bg);
}

/* Content area typography - scoped to not affect nav */
.page-header,
.utility-container {
  font-family: 'Krub', sans-serif;
  font-size: 16px;
  line-height: 1.6;
  color: var(--color-text);
}

/* Page Header */
.page-header {
  text-align: center;
  padding: 2.5rem 1rem 2rem;
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 0.5rem;
  letter-spacing: -0.02em;
}

.page-header .subtitle {
  color: var(--color-text-muted);
  font-size: 1rem;
  margin: 0;
}

/* Main Content Container */
.utility-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 1.5rem 3rem;
}

/* Section Cards */
.section-card {
  background: var(--color-surface);
  border-radius: var(--radius);
  box-shadow: var(--shadow-soft);
  padding: 1.75rem 2rem;
  margin-bottom: 1.5rem;
  border: 1px solid var(--color-border-light);
  transition: box-shadow var(--transition);
}

.section-card:hover {
  box-shadow: var(--shadow-hover);
}

.section-card h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid var(--color-accent);
  display: inline-block;
}

.section-card p {
  color: var(--color-text-muted);
  margin: 0 0 1rem;
  font-size: 0.95rem;
}

/* Quick Links Grid */
.quick-links {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.quick-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  background: var(--color-bg);
  border-radius: var(--radius);
  text-decoration: none;
  color: var(--color-text);
  font-weight: 500;
  transition: all var(--transition);
  border: 1px solid var(--color-border);
}

.quick-link:hover {
  background: var(--color-accent);
  color: white;
  border-color: var(--color-accent);
  transform: translateY(-2px);
}

.quick-link svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

/* Form Styles */
.graph-form {
  display: grid;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: var(--color-text);
  font-size: 0.9rem;
}

.form-group select {
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-family: inherit;
  font-size: 0.95rem;
  background: var(--color-surface);
  color: var(--color-text);
  cursor: pointer;
  transition: border-color var(--transition);
}

.form-group select:focus {
  outline: none;
  border-color: var(--color-accent);
}

.form-group select option {
  padding: 0.5rem;
}

.form-hint {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  margin-top: 0.25rem;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.checkbox-group input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: var(--color-accent);
  cursor: pointer;
}

.checkbox-group label {
  font-size: 0.9rem;
  color: var(--color-text);
  cursor: pointer;
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 0.5rem;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.65rem 1.5rem;
  border: none;
  border-radius: var(--radius);
  font-family: inherit;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition);
  text-decoration: none;
}

.btn-primary {
  background: var(--color-accent);
  color: white;
}

.btn-primary:hover {
  background: var(--color-accent-hover);
  transform: translateY(-1px);
}

.btn-secondary {
  background: var(--color-bg);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.btn-secondary:hover {
  background: var(--color-border-light);
  color: var(--color-accent);
}

/* Collections List */
.collections-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 0.75rem;
}

.collection-link {
  display: block;
  padding: 0.75rem 1rem;
  background: var(--color-bg);
  border-radius: 6px;
  text-decoration: none;
  color: var(--color-text);
  font-size: 0.9rem;
  transition: all var(--transition);
  border: 1px solid transparent;
}

.collection-link:hover {
  background: var(--color-surface);
  border-color: var(--color-accent);
  color: var(--color-accent);
  padding-left: 1.25rem;
}

/* Data Graphics Grid */
.graphics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-top: 1rem;
}

.graphics-panel {
  background: var(--color-bg);
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.graphics-panel-header {
  padding: 0.875rem 1.25rem;
  background: var(--color-text);
  color: white;
}

.graphics-panel-header a {
  color: white;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.95rem;
}

.graphics-panel-header a:hover {
  text-decoration: underline;
}

.graphics-panel-body {
  padding: 0.5rem 0;
}

.graphics-panel-body a {
  display: block;
  padding: 0.6rem 1.25rem;
  color: var(--color-text);
  text-decoration: none;
  font-size: 0.9rem;
  transition: all var(--transition);
}

.graphics-panel-body a:hover {
  background: var(--color-surface);
  color: var(--color-accent);
  padding-left: 1.5rem;
}

/* Two Column Layout for Forms */
.two-column {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 1.5rem;
}

@media (max-width: 900px) {
  .two-column {
    grid-template-columns: 1fr;
  }
}

/* Responsive adjustments */
@media (max-width: 600px) {
  .page-header {
    padding: 1.5rem 1rem;
  }
  
  .section-card {
    padding: 1.25rem 1.5rem;
  }
  
  .quick-links {
    grid-template-columns: 1fr;
  }
  
  .collections-grid {
    grid-template-columns: 1fr;
  }
  
  .graphics-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<!-- Page Header -->
<header class="page-header">
  <h1>Explore</h1>
  <p class="subtitle">Ontologies, thesauri, collections, and data visualizations</p>
</header>

<div class="utility-container">

  <!-- Quick Access Utilities -->
  <section class="section-card">
    <h2>Quick Access</h2>
    <div class="quick-links">
      <a href="${cfg.host}/sparql" class="quick-link">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M8 9l3 3-3 3m5 0h3M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
        </svg>
        SPARQL Browser
      </a>
      <a href="${cfg.host}/metricTables" class="quick-link">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
        </svg>
        Metrics Dashboard
      </a>
    </div>
  </section>

  <!-- Graph Explorers - Two Column -->
  <div class="two-column">
  
    <!-- Ontology Graph -->
    <section class="section-card">
      <h2>Ontology Graph</h2>
      <p>Visualize the structure and relationships within concept schemes.</p>
      <form action="/model.graph2" method="get" class="graph-form">
        <div class="form-group">
          <label for="ontologySelect">Select concept schemes:</label>
          <select name="vocabGraph" id="ontologySelect" multiple size="6">
"""
	int i=0
	def lc = new QuerySupport().queryConceptSchemes("the:forOntology")
	lc.each{
			sb.append """            <option value="${it.s}" ${i++==0?"selected":""}>${it.l}</option>
"""
		}
		
		sb.append """          </select>
          <span class="form-hint">Hold Ctrl (Cmd on Mac) to select multiple</span>
        </div>
        <div class="checkbox-group">
          <input type="checkbox" id="ontologyDefn" name="defn" value="defn">
          <label for="ontologyDefn">Include comments</label>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary">Generate Graph</button>
          <a href="${cfg.host}/model.graph" class="btn btn-secondary">View Complete Graph</a>
        </div>
      </form>
    </section>

    <!-- Thesaurus Graph -->
    <section class="section-card">
      <h2>Thesaurus Graph</h2>
      <p>Explore vocabulary hierarchies and term relationships.</p>
      <form action="/vocab.graph2" method="get" class="graph-form">
        <div class="form-group">
          <label for="thesaurusSelect">Select concept schemes:</label>
          <select name="vocabGraph" id="thesaurusSelect" multiple size="6">
"""
	i=0
	lc = new QuerySupport().queryConceptSchemes("the:forThesaurus")
	lc.each{
			sb.append """            <option value="${it.s}" ${i++==0?"selected":""}>${it.l}</option>
"""
		}
		
		sb.append """          </select>
          <span class="form-hint">Hold Ctrl (Cmd on Mac) to select multiple</span>
        </div>
        <div class="checkbox-group">
          <input type="checkbox" id="thesaurusDefn" name="defn" value="defn">
          <label for="thesaurusDefn">Include definitions</label>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary">Generate Graph</button>
          <a href="${cfg.host}/vocab.graph" class="btn btn-secondary">View Complete Graph</a>
        </div>
      </form>
    </section>
    
  </div>

  <!-- Concept Collections -->
  <section class="section-card">
    <h2>Concept Collections</h2>
    <p>Browse curated collections of related concepts and artworks.</p>
    <div class="collections-grid">
"""
	
	lc = new QuerySupport().queryCollections()
	lc.each{
		sb.append """      <a href="${it.s.replaceAll("http://visualartsdna.org",host)}" class="collection-link">${it.l}</a>
"""
	}
		sb.append """    </div>
  </section>

  <!-- Data Graphics -->
  <section class="section-card">
    <h2>Data Visualizations</h2>
    <p>Interactive graphics powered by D3.js and L-systems.</p>
    <div class="graphics-grid">
      
      <div class="graphics-panel">
        <div class="graphics-panel-header">
          <a href="https://d3js.org/" target="_blank" rel="noopener">D3.js Graphics &#8599;</a>
        </div>
        <div class="graphics-panel-body">
          <a href="${cfg.host}/d3.wcDrawBasic">Drawings &amp; Watercolors &#8212; Basic</a>
          <a href="${cfg.host}/d3.wcBasic">Watercolors &#8212; Basic</a>
          <a href="${cfg.host}/d3.wcPhysical">Watercolors &#8212; Physical</a>
          <a href="${cfg.host}/d3.wcNFT">Watercolors &#8212; NFT</a>
          <a href="${cfg.host}/d3.drawBasic">Drawings &#8212; Basic</a>
          <a href="${cfg.host}/d3.drawPhysical">Drawings &#8212; Physical</a>
          <a href="${cfg.host}/d3.drawNFT">Drawings &#8212; NFT</a>
        </div>
      </div>
      
      <div class="graphics-panel">
        <div class="graphics-panel-header">
          <a href="https://github.com/rspates/lsys" target="_blank" rel="noopener">L-System Graphics &#8599;</a>
        </div>
        <div class="graphics-panel-body">
          <a href="${cfg.host}/lsys.wcDrawBasic">Drawings &amp; Watercolors &#8212; Basic</a>
          <a href="${cfg.host}/lsys.wcBasic">Watercolors &#8212; Basic</a>
          <a href="${cfg.host}/lsys.wcPhysical">Watercolors &#8212; Physical</a>
          <a href="${cfg.host}/lsys.wcNFT">Watercolors &#8212; NFT</a>
          <a href="${cfg.host}/lsys.drawBasic">Drawings &#8212; Basic</a>
          <a href="${cfg.host}/lsys.drawPhysical">Drawings &#8212; Physical</a>
          <a href="${cfg.host}/lsys.drawNFT">Drawings &#8212; NFT</a>
        </div>
      </div>
      
    </div>
  </section>

</div>
""" 
	sb.append HtmlTemplate.tail
	
	""+sb
	}
	
	def getMetrics() {
		
		def src = "stats"
		def f = "chart.html"
		try {
			def url = Gcp.gcpLs(src,f)
			if (url) Gcp.gcpCp(url,"${cfg.dir}/$f")
		} catch (RuntimeException re) {
			System.err.println ("$f not found, $re")
			throw new FileNotFoundException("$f not found")
		}
		def f2 = new File("${cfg.dir}/$f")
		
		if (!f2.exists()) {
			throw new FileNotFoundException("$f not found")
		}
		f2.text
	}
			
		
}
