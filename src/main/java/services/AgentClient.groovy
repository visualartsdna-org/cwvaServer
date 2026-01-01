package services

class AgentClient {
	
	
	def process( mq, agentUrl, host) {
		def sb = new StringBuilder()
		
		sb.append HtmlTemplate.head(host) //, "#757575")
		printHtml(sb,mq, agentUrl, host)
		sb.append HtmlTemplate.tail
		"$sb"

	}
	
	def sampleQuestions = [
		"how many paintings",
		"artworks from Korea",
		"cold press paintings",
		"what is gesso",
		"compare cold press vs hot press",
		"paintings from the last 3 years",
]
	
	def printHtml(sb,mq, agentUrl, host) {
		
		sb.append """
    <title>Watercolor Art Query</title>
    <style>
        /* === CUSTOMIZABLE STYLES === */
        :root {
            --primary-color: #4a6fa5;
            --primary-hover: #3d5d8a;
            --background: #f5f7fa;
            --card-background: #ffffff;
            --text-color: #333;
            --text-muted: #666;
            --border-color: #ddd;
            --success-color: #28a745;
            --error-color: #dc3545;
        }

        * {
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
            background: var(--background);
            color: var(--text-color);
            margin: 0;
            padding: 20px;
            line-height: 1.6;
        }

        .container {
            max-width: 900px;
            margin: 0 auto;
        }

        header {
            text-align: center;
            margin-bottom: 30px;
        }

        header h1 {
            color: var(--primary-color);
            margin-bottom: 5px;
        }

        header p {
            color: var(--text-muted);
            margin: 0;
        }

        /* Query Input Section */
        .query-section {
            background: var(--card-background);
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            margin-bottom: 20px;
        }

        .input-group {
            display: flex;
            gap: 12px;
        }

        #queryInput {
            flex: 1;
            padding: 14px 18px;
            font-size: 16px;
            border: 2px solid var(--border-color);
            border-radius: 8px;
            outline: none;
            transition: border-color 0.2s;
        }

        #queryInput:focus {
            border-color: var(--primary-color);
        }

        #queryInput::placeholder {
            color: #aaa;
        }

        button {
            padding: 14px 28px;
            font-size: 16px;
            font-weight: 600;
            background: var(--primary-color);
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            transition: background 0.2s;
        }

        button:hover {
            background: var(--primary-hover);
        }

        button:disabled {
            background: #aaa;
            cursor: not-allowed;
        }

        /* Example Queries */
        .examples {
            margin-top: 16px;
            padding-top: 16px;
            border-top: 1px solid var(--border-color);
        }

        .examples-label {
            font-size: 13px;
            color: var(--text-muted);
            margin-bottom: 8px;
        }

        .example-chips {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }

        .example-chip {
            padding: 6px 12px;
            font-size: 13px;
            background: var(--background);
            border: 1px solid var(--border-color);
            border-radius: 20px;
            cursor: pointer;
            transition: all 0.2s;
        }

        .example-chip:hover {
            background: var(--primary-color);
            color: white;
            border-color: var(--primary-color);
        }

        /* Response Section */
        .response-section {
            background: var(--card-background);
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            display: none;
        }

        .response-section.visible {
            display: block;
        }

        .response-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
        }

        .response-header h3 {
            margin: 0;
            color: var(--primary-color);
        }

        .response-meta {
            font-size: 13px;
            color: var(--text-muted);
        }

        .response-text {
            font-size: 17px;
            line-height: 1.7;
            margin-bottom: 20px;
            padding: 16px;
            background: var(--background);
            border-radius: 8px;
        }

        /* Artwork anchor links in responses */
        .response-text a.artwork-link {
            color: var(--primary-color);
            text-decoration: none;
            border-bottom: 1px dotted var(--primary-color);
            transition: all 0.2s;
        }

        .response-text a.artwork-link:hover {
            color: var(--primary-hover);
            border-bottom-style: solid;
        }

        /* External reference links (Wikipedia, etc.) */
        .response-text a.external-link {
            color: #6b7280;
            text-decoration: none;
            border-bottom: 1px dotted #6b7280;
        }

        .response-text a.external-link::after {
            content: " ^";
            font-size: 0.75em;
        }

        .response-text a.external-link:hover {
            color: var(--primary-color);
        }

        /* Collapsible Details */
        .details-toggle {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 10px 0;
            font-size: 14px;
            color: var(--text-muted);
            cursor: pointer;
            user-select: none;
        }

        .details-toggle:hover {
            color: var(--primary-color);
        }

        .details-toggle .arrow {
            transition: transform 0.2s;
        }

        .details-toggle.open .arrow {
            transform: rotate(90deg);
        }

        .details-content {
            display: none;
            margin-top: 12px;
        }

        .details-content.visible {
            display: block;
        }

        .detail-block {
            margin-bottom: 16px;
        }

        .detail-label {
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            color: var(--text-muted);
            margin-bottom: 6px;
        }

        .sparql-display {
            font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
            font-size: 13px;
            background: #1e1e1e;
            color: #d4d4d4;
            padding: 16px;
            border-radius: 8px;
            overflow-x: auto;
            white-space: pre-wrap;
        }

        .data-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }

        .data-table th,
        .data-table td {
            padding: 10px 12px;
            text-align: left;
            border-bottom: 1px solid var(--border-color);
        }

        .data-table th {
            background: var(--background);
            font-weight: 600;
            color: var(--text-muted);
        }

        .data-table tr:hover td {
            background: var(--background);
        }

        /* Loading State */
        .loading {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px;
            color: var(--text-muted);
        }

        .spinner {
            width: 24px;
            height: 24px;
            border: 3px solid var(--border-color);
            border-top-color: var(--primary-color);
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
            margin-right: 12px;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        /* Error State */
        .error {
            color: var(--error-color);
            padding: 16px;
            background: #fff5f5;
            border-radius: 8px;
            border: 1px solid #ffcccc;
        }

        /* Settings Panel */
        .settings {
            margin-top: 30px;
            padding: 20px;
            background: var(--card-background);
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }

        .settings h4 {
            margin: 0 0 16px 0;
            color: var(--text-muted);
            font-size: 14px;
            text-transform: uppercase;
        }

        .setting-row {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 12px;
        }

        .setting-row label {
            min-width: 120px;
            font-size: 14px;
            color: var(--text-muted);
        }

        .setting-row input {
            flex: 1;
            padding: 8px 12px;
            font-size: 14px;
            border: 1px solid var(--border-color);
            border-radius: 6px;
        }

        /* Responsive */
        @media (max-width: 600px) {
            .input-group {
                flex-direction: column;
            }

            button {
                width: 100%;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- Header - Customize your title here -->
        <header>
            <h1>Visual Art Query</h1>
            <p>Ask questions about the artwork collection in natural language</p>
        </header>

        <!-- Query Input -->
        <div class="query-section">
            <div class="input-group">
                <input 
                    type="text" 
                    id="queryInput" 
                    placeholder="Ask a question... (e.g., 'how many paintings from Korea')"
                    autocomplete="off"
                >
                <button id="submitBtn" onclick="submitQuery()">Ask</button>
            </div>

            <!-- Example Queries - Customize these -->
            <div class="examples">
                <div class="examples-label">Try an example:</div>
                <div class="example-chips">
"""
		sampleQuestions.each{
			sb.append """<span class="example-chip" onclick="setQuery('$it')">$it</span>\n"""
		}

sb.append """					
                </div>
            </div>
        </div>

        <!-- Response Area -->
        <div class="response-section" id="responseSection">
            <div id="responseContent"></div>
        </div>
    </div>
			<h5>
			<a href="/html/AgentAbout.html">About Visual Art Query</a>
			</h5>
    <script>
        // === CONFIGURATION ===
        // Change this to your agent's URL
        const DEFAULT_AGENT_URL = '${host}/agent';

        // === HELPER FUNCTIONS ===
        function getAgentUrl() {
            //return document.getElementById('agentUrl').value || DEFAULT_AGENT_URL;
            return DEFAULT_AGENT_URL;
        }

        function setQuery(text) {
            document.getElementById('queryInput').value = text;
            document.getElementById('queryInput').focus();
        }

        function showLoading() {
            const section = document.getElementById('responseSection');
            const content = document.getElementById('responseContent');
            section.classList.add('visible');
            content.innerHTML = `
                <div class="loading">
                    <div class="spinner"></div>
                    <span>Thinking...</span>
                </div>
            `;
        }

        function showError(message) {
            const section = document.getElementById('responseSection');
            const content = document.getElementById('responseContent');
            section.classList.add('visible');
            content.innerHTML = `<div class="error">X \${message}</div>`;
        }

        function showResponse(data) {
            const section = document.getElementById('responseSection');
            const content = document.getElementById('responseContent');
            section.classList.add('visible');

            // Check if query was screened out
            if (data.screened) {
                content.innerHTML = `
                    <div class="response-header">
                        <h3>Response</h3>
                    </div>
                    <div class="response-text" style="border-left: 3px solid var(--primary-color);">
                        \${escapeHtml(data.response)}
                    </div>
                `;
                return;
            }

            // Build response HTML
            // Use sanitizeResponseHtml to allow safe anchor tags while escaping other HTML
            let html = `
                <div class="response-header">
                    <h3>Response</h3>
                    <span class="response-meta">
                        \${data.row_count} result\${data.row_count !== 1 ? 's' : ''} 
                        • \${data.elapsed_seconds?.toFixed(2) || '?'}s
                    </span>
                </div>
                <div class="response-text">\${sanitizeResponseHtml(data.response)}</div>
            `;

            // Add collapsible details
            html += `
                <div class="details-toggle" onclick="toggleDetails(this)">
                    <span class="arrow">></span>
                    <span>Show technical details</span>
                </div>
                <div class="details-content">
            `;

            // SPARQL query
            if (data.sparql) {
                html += `
                    <div class="detail-block">
                        <div class="detail-label">Generated SPARQL</div>
                        <div class="sparql-display">\${escapeHtml(data.sparql)}</div>
                    </div>
                `;
            }

            // Data table (if results exist)
            if (data.data && data.data.length > 0) {
                html += `
                    <div class="detail-block">
                        <div class="detail-label">Data (\${data.data.length} rows)</div>
                        \${buildDataTable(data.data)}
                    </div>
                `;
            }

            html += `</div>`;

            content.innerHTML = html;
        }

        function buildDataTable(data) {
            if (!data || data.length === 0) return '<p>No data</p>';

            const columns = Object.keys(data[0]);
            const maxRows = 10; // Limit displayed rows

            let html = '<table class="data-table"><thead><tr>';
            columns.forEach(col => {
                html += `<th>\${escapeHtml(col)}</th>`;
            });
            html += '</tr></thead><tbody>';

            data.slice(0, maxRows).forEach(row => {
                html += '<tr>';
                columns.forEach(col => {
                    let value = row[col] || '';
                    // Truncate long values
                    if (value.length > 50) {
                        value = value.substring(0, 50) + '...';
                    }
                    html += `<td>\${escapeHtml(value)}</td>`;
                });
                html += '</tr>';
            });

            html += '</tbody></table>';

            if (data.length > maxRows) {
                html += `<p style="color: var(--text-muted); font-size: 13px; margin-top: 8px;">
                    Showing \${maxRows} of \${data.length} rows
                </p>`;
            }

            return html;
        }

        function toggleDetails(element) {
            element.classList.toggle('open');
            const content = element.nextElementSibling;
            content.classList.toggle('visible');
        }

        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        function sanitizeResponseHtml(text) {
            /**
             * Allow safe anchor tags from the agent while escaping everything else.
             * Only allows:
             * - <a href="..." target="_blank" class="artwork-link">...</a>
             * - <a href="..." target="_blank" class="external-link">...</a>
             * 
             * URLs must be http:// or https:// (no javascript:)
             */
            if (!text) return '';
            
            // Extract and preserve safe anchor tags before escaping
            const anchors = [];
            const placeholder = '___SAFE_ANCHOR_';
            
            // Pattern to match safe anchor tags (actual HTML, not escaped)
            const anchorPattern = /<a\\s+href="(https?:\\/\\/[^"]+)"\\s+target="_blank"\\s+class="(artwork-link|external-link)">([^<]+)<\\/a>/gi;
            
            // Replace anchors with placeholders
            let processed = text.replace(anchorPattern, (match, url, cssClass, linkText) => {
                // Validate URL is safe
                if (!url.match(/^https?:\\/\\//i)) {
                    return linkText; // Return just the text if URL is suspicious
                }
                const index = anchors.length;
                anchors.push({ url, cssClass, linkText });
                return placeholder + index + '___';
            });
            
            // Escape everything else
            processed = escapeHtml(processed);
            
            // Restore the safe anchors
            anchors.forEach((anchor, index) => {
                const safeAnchor = `<a href="\${anchor.url}" target="_blank" class="\${anchor.cssClass}">\${escapeHtml(anchor.linkText)}</a>`;
                processed = processed.replace(placeholder + index + '___', safeAnchor);
            });
            
            return processed;
        }

        // === MAIN QUERY FUNCTION ===
        async function submitQuery() {
            const input = document.getElementById('queryInput');
            const button = document.getElementById('submitBtn');
            const query = input.value.trim();

            if (!query) {
                input.focus();
                return;
            }

            // Disable button during request
            button.disabled = true;
            showLoading();

            try {
                const response = await fetch(`\${getAgentUrl()}/query`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ query: query })
                });

				if (response.status === 429) {
				    const data = await response.json();
				    showError(`Too many queries. Please wait \${data.retry_after || 60} seconds.`);
				    return;
				}

                if (!response.ok) {
                    throw new Error(`Server returned \${response.status}`);
                }

                const data = await response.json();

                if (data.error && !data.response) {
                    showError(data.error);
                } else {
                    showResponse(data);
                }

            } catch (error) {
                console.error('Query error:', error);
                showError(`Could not connect to agent at \${getAgentUrl()}. Is the server running?`);
            } finally {
                button.disabled = false;
            }
        }

        // === EVENT LISTENERS ===
        document.getElementById('queryInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                submitQuery();
            }
        });

        // Focus input on load
        document.getElementById('queryInput').focus();
    </script>
"""

		
	}
}
