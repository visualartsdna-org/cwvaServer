package services

import groovy.json.JsonOutput
import rdf.QuerySupport

class VocabTree {
	
	def qs
	def host
	def isMobile
	def odict = []
	
	def process( mq, host,rdfs) {

		isMobile = mq.isMobile == "true"
		def sb = new StringBuilder()
		if (!qs) qs = new QuerySupport(rdfs)
		this.host = host
		
		def dl = buildDict().sort{a,b->
			a.l <=> b.l
		}
		def dictionaryTree = convertToDictionary(dl)
		def jsonOutput = JsonOutput.toJson(dictionaryTree)
		//def prettyJson = JsonOutput.prettyPrint(jsonOutput)
		
	
		sb.append HtmlTemplate.head(host) //, "#757575")
		//sb.append revisedHtmlTemplateHead(host, "#FFFFFF") 
		printHtml(sb,mq.work,mq.selectBkgnd, jsonOutput)
		sb.append HtmlTemplate.tail
		"$sb"
		
	}
	
	
	/**
	 * Recursively builds a tree node for the given URI
	 * @param uri The URI of the current node
	 * @param resultsMap Map of URI to result row
	 * @param childrenMap Map of parent URI to list of child URIs
	 * @return A map representing the node in dictionary format
	 */
	def buildNode(uri, resultsMap, childrenMap) {
		def result = resultsMap[uri]
		def label = result.l
		def definition = result.d
		
		def childUris = childrenMap[uri] ?: []
		def children = childUris.collect { childUri ->
			buildNode(childUri, resultsMap, childrenMap)
		}
		
		return [(label): [
			uri: cwva.Server.rehost(uri),              // This line adds the uri
			definition: definition,
			children: children
		]]
	}
	
	/**
	 * Converts query results to dictionary tree format
	 * @param queryResults List of maps with s, b, l, d keys
	 * @return List of root nodes in dictionary format
	 */
	def convertToDictionary(queryResults) {
		// Create lookup maps
		def resultsMap = queryResults.collectEntries { [(it.s): it] }
		
		// Build parent-to-children mapping
		def childrenMap = [:].withDefault { [] }
		queryResults.each { row ->
			if (row.b) {
				childrenMap[row.b] << row.s
			}
		}
		
		// Find root nodes (nodes without parent 'b' property)
		def rootUris = queryResults.findAll { !it.b }.collect { it.s }
		
		// Build tree recursively from each root
		return rootUris.collect { rootUri ->
			buildNode(rootUri, resultsMap, childrenMap)
		}
	}
	
	def buildDict() {
		
		qs.queryGeneric("""
select ?s ?b ?l ?d {
?s a skos:Concept ;
	rdfs:label ?l ;
	skos:definition ?d ;
    .
	optional {?s skos:broader ?b ;}

	} order by ?l
""")
	}

	def printHtml(sb,work,bkgVal, jsonOutput) {
		
		// model-viewer
		sb.append """
<!-- TREE MARKUP -->
<div class="dictionary-tree">
  <div class="toolbar">
    <button id="toggleAllBtn">Expand All</button>
    <select id="conceptSelect" class="concept-select">
      <option value="" class="placeholder" selected>concepts</option>
    </select>
  </div>
  <ul id="treeRoot" class="tree"></ul>
</div>


<style>
  .dictionary-tree,
  .dictionary-tree * {
    box-sizing: border-box;
  }

  .dictionary-tree {
    margin: 0 !important;
    padding: 0.75rem 0.75rem 1.5rem 0.75rem;
    background: #eee;
    font-family: system-ui, -apple-system, "Segoe UI", sans-serif;
    font-size: 14px;
    line-height: 1.25;
  }

  .dictionary-tree ul,
  .dictionary-tree li {
    margin: 0 !important;
    padding: 0;
    list-style: none;
    background: transparent !important;
    float: none !important;
  }

  .dictionary-tree .toolbar {
    margin-bottom: 0.5rem;
    display: flex;
    gap: 0.5rem;
    align-items: center;
  }

  .dictionary-tree button {
    font-size: 0.8rem;
    padding: 0.25rem 0.55rem;
  }

  .dictionary-tree .concept-select {
    font-size: 0.8rem;
    padding: 0.2rem 0.4rem;
  }

  .dictionary-tree ul.tree {
    width: 100%;
  }

  .dictionary-tree .tree-item {
    display: flex;
    gap: 0.3rem;
    align-items: flex-start;
    padding: 2px 0;
    width: 100%;
  }

  /* --- CSS TRIANGLES --- */
  .dictionary-tree .toggle {
    width: 1.2rem;
    height: 1.2rem;
    cursor: pointer;
    flex: 0 0 auto;
    position: relative;
  }
  /* collapsed = pointing right */
  .dictionary-tree .tree-item.collapsed > .toggle::before {
    content: "";
    position: absolute;
    top: 50%;
    left: 2px;
    transform: translateY(-50%);
    width: 0;
    height: 0;
    border-style: solid;
    border-width: 5px 0 5px 7px;
    border-color: transparent transparent transparent #333;
  }
  /* expanded = pointing down */
  .dictionary-tree .tree-item:not(.collapsed) > .toggle::before {
    content: "";
    position: absolute;
    top: 40%;
    left: 2px;
    width: 0;
    height: 0;
    border-style: solid;
    border-width: 7px 5px 0 5px;
    border-color: #333 transparent transparent transparent;
  }
  /* no-children */
/* force no arrow on leaf nodes */
	.dictionary-tree .tree-item > .toggle.invisible::before {
	  content: "" !important;
	  border: none !important;
	  width: 0;
	  height: 0;
	}

  .dictionary-tree .item-body {
    flex: 1 1 auto;
    display: flex;
    flex-direction: column;
    gap: 0.15rem;
  }

  .dictionary-tree .node-content {
    display: flex;
    align-items: baseline;
    gap: 0.35rem;
    flex-wrap: wrap;
    width: 100%;
  }

  .dictionary-tree .node-title a {
    text-decoration: none;
    color: steelblue;
    font-weight: 600;
    white-space: nowrap;
  }

  .dictionary-tree .node-definition {
    flex: 1 1 auto;
  }

  .dictionary-tree ul.children {
    margin: 0.05rem 0 0 0;
    padding-left: 0.9rem;
    border-left: 1px solid #d9d9d9;
  }

  .dictionary-tree .collapsed > .item-body > ul.children {
    display: none;
  }

  .dictionary-tree .highlight {
    background: #fff7c2;
    border-radius: 3px;
    padding: 0 2px;
  }
</style>
<script>
document.addEventListener('DOMContentLoaded', function () {
  const orderedData = """
		
		sb.append jsonOutput
		sb.append ";"
		
		sb.append """


  const treeRoot = document.getElementById('treeRoot');
  const selectEl = document.getElementById('conceptSelect');
  const toggleAllBtn = document.getElementById('toggleAllBtn');

  // index of term -> {li, content}
  const termIndex = new Map();
  let expandedAll = false;

  function buildNode(key, dataObj) {
    const li = document.createElement('li');
    const hasChildren = Array.isArray(dataObj.children) && dataObj.children.length > 0;
    li.classList.add('tree-item');
    if (hasChildren) li.classList.add('collapsed');

    const toggle = document.createElement('span');
    toggle.className = 'toggle';
    if (!hasChildren) toggle.classList.add('invisible');
    li.appendChild(toggle);

    const body = document.createElement('div');
    body.className = 'item-body';

    const content = document.createElement('div');
    content.className = 'node-content';

    const title = document.createElement('span');
    title.className = 'node-title';
    const a = document.createElement('a');
    a.href = dataObj.uri || '#';
    a.textContent = key;
    title.appendChild(a);

    const colon = document.createElement('span');
    colon.textContent = ':';

    const def = document.createElement('span');
    def.className = 'node-definition';
    def.textContent = dataObj.definition || '';

    content.append(title, colon, def);
    body.appendChild(content);

    // index for dropdown
    termIndex.set(key, { li, content });

    if (hasChildren) {
      const ul = document.createElement('ul');
      ul.className = 'children';
      dataObj.children.forEach(childObj => {
        const childKey = Object.keys(childObj)[0];
        ul.appendChild(buildNode(childKey, childObj[childKey]));
      });
      body.appendChild(ul);
    }

    li.appendChild(body);

    if (hasChildren) {
      const toggleHandler = () => {
        const collapsed = li.classList.toggle('collapsed');
        // no text to update now, triangles are via CSS
      };
      toggle.addEventListener('click', toggleHandler);
      content.addEventListener('click', e => {
        if (e.target.tagName.toLowerCase() === 'a') return;
        toggleHandler();
      });
    }

    return li;
  }

  function buildTree(container, dataArr) {
    dataArr.forEach(obj => {
      const key = Object.keys(obj)[0];
      container.appendChild(buildNode(key, obj[key]));
    });
  }

  function setAll(expand = true) {
    document.querySelectorAll('.dictionary-tree .tree-item').forEach(item => {
      const hasChildren = item.querySelector(':scope > .item-body > ul.children');
      if (!hasChildren) return;
      item.classList.toggle('collapsed', !expand);
      // no need to change text; CSS handles arrow
    });
  }

  function expandTo(li) {
    let current = li;
    while (current && current !== treeRoot) {
      if (current.classList && current.classList.contains('tree-item')) {
        current.classList.remove('collapsed');
      }
      // climb DOM to the nearest LI parent
      current = current.parentElement;
      while (current && current !== treeRoot && current.tagName !== 'LI') {
        current = current.parentElement;
      }
    }
  }

  function clearHighlight() {
    document
      .querySelectorAll('.dictionary-tree .highlight')
      .forEach(el => el.classList.remove('highlight'));
  }

  // build UI
  buildTree(treeRoot, orderedData);

  // populate dropdown (sorted)
  const sortedKeys = Array.from(termIndex.keys()).sort((a, b) =>
    a.localeCompare(b, undefined, { sensitivity: 'base' })
  );
  sortedKeys.forEach(key => {
    const opt = document.createElement('option');
    opt.value = key;
    opt.textContent = key;
    selectEl.appendChild(opt);
  });

  // toggle button
  toggleAllBtn.addEventListener('click', () => {
    expandedAll = !expandedAll;
    setAll(expandedAll);
    toggleAllBtn.textContent = expandedAll ? 'Collapse All' : 'Expand All';
  });

  // dropdown search
  selectEl.addEventListener('change', () => {
    const term = selectEl.value;
    if (!term) return;
    const entry = termIndex.get(term);
    if (!entry) return;

    clearHighlight();
    expandTo(entry.li);
    entry.content.classList.add('highlight');
    entry.li.scrollIntoView({ behavior: 'smooth', block: 'center' });
  });
});

  </script>
"""
	}
	
}
