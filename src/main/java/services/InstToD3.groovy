package services
import org.apache.commons.text.WordUtils
import org.apache.jena.rdf.model.Model

import rdf.JenaUtils


// https://visjs.github.io/vis-network/

class InstToD3 {

	static JenaUtils ju = new JenaUtils()
	def model
	// https://imagecolorpicker.com/color-code
	def wrapWidth = 20

	InstToD3(ttl) {
		model = ju.loadFiles(ttl)
	}
	
	static def driverSelectType(ttl,html,type) {
		def sql = """
# pretty watercolors
${rdf.Prefixes.forQuery}
			
construct {
?s rdfs:label ?label .
?s schema:description ?desc .
?s a ?type .
} {
		?s a ?type .
		FILTER ( ?type in (${type}))
		?s rdfs:label ?label .
		?s schema:description ?desc .
		}
"""
		driverFromConstruct(ttl,html,sql)
	}
		
	static def driverSelectTypeNFT(ttl,html,type) {
		def sql = """
# pretty watercolors
${rdf.Prefixes.forQuery}
			
construct {
?s rdfs:label ?label .
?s schema:description ?desc .
?s vad:hasNFT ?nft .
?nft vad:hasNFTTokenID ?tid .
?nft vad:hasNFTContractAddress ?ca .
?s a ?type .
} {
		?s a ?type .
		FILTER ( ?type in (${type}))
		?s rdfs:label ?label .
		?s schema:description ?desc .
		?s vad:hasNFT ?nft .
		?nft vad:hasNFTTokenID ?tid .
		?nft vad:hasNFTContractAddress ?ca .
		}
"""
		driverFromConstruct(ttl,html,sql)
	}
		
	static def driverSelectTypePhysical(ttl,html,type) {
		def sql = """
# pretty watercolors
${rdf.Prefixes.forQuery}
			
construct {
?s rdfs:label ?label .
?s schema:description ?desc .
?s vad:hasPaperFinish ?finish .
?s vad:hasPaperWeight ?weight .
?s schema:dateCreated ?compdt .
?s schema:height ?len .
?s schema:width ?wid .
?s a ?type .
} {
		?s a ?type .
		FILTER ( ?type in (${type}))
		?s rdfs:label ?label .
		?s schema:description ?desc .
		?s vad:hasPaper ?paper .
		?s vad:hasPaperFinish ?finish .
		?s vad:hasPaperWeight ?weight .
		?s schema:dateCreated ?compdt .
		?s schema:height ?len .
		?s schema:width ?wid .
		}
"""
		driverFromConstruct(ttl,html,sql)
	}
		
	static def driverFromConstruct(ttl,html,sql) {
		
		def m = ju.loadDirModel(ttl)
		//def m = ju.loadFileModelFilespec(ttl)
		def m2 = ju.queryExecConstruct(m,"", sql)
		def ttl2 = getTempFile()
		ju.saveModelFile(m2, ttl2, "TTL")
		driverAll(ttl2,html)
	}
	
	static def driverAll(ttl,html) {
		def sql = """
${rdf.Prefixes.forQuery}

SELECT ?s ?p ?o {
		?s ?p ?o .
		# FILTER ( !isBlank(?s))
		} order by ?s
"""
		driver(ttl,html,sql)
	}
	
	static def driverKind(ttl,html,kind) {
		def sql = """
${rdf.Prefixes.forQuery}
			
SELECT ?s ?p ?o {
		?s ?p ?o .
		?s a ?type .
		FILTER ( ?type in ($kind))
		} order by ?s
"""
		driver(ttl,html,sql)
	}
	
	static def driver(ttl,html,sql) {
			
		def otd = new InstToD3(ttl)
		def json = otd.getData(sql)
		
		def doc = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style type="text/css">
        .node {}

        .link { stroke: #999; stroke-opacity: .6; stroke-width: 1px; }
    </style>
</head>
<body>
<svg width="1800" height="1200"></svg>

<script src="https://d3js.org/d3.v4.min.js" type="text/javascript"></script>
<script src="https://d3js.org/d3-selection-multi.v1.js"></script>

<script type="text/javascript">

	var graph = 
{
  "nodes": [
		${json[0]}
   ],
  "links": [
		${json[1]}
  ]
};
    var colors = d3.scaleOrdinal(d3.schemeCategory10);

    var svg = d3.select("svg"),
        width = +svg.attr("width"),
        height = +svg.attr("height"),
        node,
        link;

    svg.append('defs').append('marker')
        .attrs({'id':'arrowhead',
            'viewBox':'-0 -5 10 10',
            'refX':13,
            'refY':0,
            'orient':'auto',
            'markerWidth':13,
            'markerHeight':13,
            'xoverflow':'visible'})
        .append('svg:path')
        .attr('d', 'M 0,-5 L 10 ,0 L 0,5')
        .attr('fill', '#999')
        .style('stroke','none');

    var simulation = d3.forceSimulation()
        .force("link", d3.forceLink().id(function (d) {return d.id;}).distance(200).strength(1))
        .force("charge", d3.forceManyBody())
        .force("center", d3.forceCenter(width / 2, height / 2));

//    d3.json("graph.json", function (error, graph) {
//        if (error) throw error;
//        update(graph.links, graph.nodes);
//    })
        update(graph.links, graph.nodes);


    function update(links, nodes) {
        link = svg.selectAll(".link")
            .data(links)
            .enter()
            .append("line")
            .attr("class", "link")
            .attr('marker-end','url(#arrowhead)')

        link.append("title")
            .text(function (d) {return d.type;});

        edgepaths = svg.selectAll(".edgepath")
            .data(links)
            .enter()
            .append('path')
            .attrs({
                'class': 'edgepath',
                'fill-opacity': 0,
                'stroke-opacity': 0,
                'id': function (d, i) {return 'edgepath' + i}
            })
            .style("pointer-events", "none");

        edgelabels = svg.selectAll(".edgelabel")
            .data(links)
            .enter()
            .append('text')
            .style("pointer-events", "none")
            .attrs({
                'class': 'edgelabel',
                'id': function (d, i) {return 'edgelabel' + i},
                'font-size': 11,
                'fill': '#aaa'
            });

        edgelabels.append('textPath')
            .attr('xlink:href', function (d, i) {return '#edgepath' + i})
            .style("text-anchor", "middle")
            .style("pointer-events", "none")
            .attr("startOffset", "50%")
            .text(function (d) {return d.type});

        node = svg.selectAll(".node")
            .data(nodes)
            .enter()
            .append("g")
            .attr("class", "node")
            .call(d3.drag()
                    .on("start", dragstarted)
                    .on("drag", dragged)
                    //.on("end", dragended)
            );

        node.append("circle")
            .attr("r", 5)
            .style("fill", function (d, i) {
				if (d.label.startsWith("work:"))
					return colors(0)
				if (d.label.startsWith("vad:"))
					return colors(1)
				return colors(6);
			}) // 2=blue

        node.append("title")
            .text(function (d) {return d.id;});

        node.append("text")
            .attr("dy", -3)
            .text(function (d) {return d.label;});
            //.text(function (d) {return d.name+":"+d.label;});

        simulation
            .nodes(nodes)
            .on("tick", ticked);

        simulation.force("link")
            .links(links);
    }

    function ticked() {
        link
            .attr("x1", function (d) {return d.source.x;})
            .attr("y1", function (d) {return d.source.y;})
            .attr("x2", function (d) {return d.target.x;})
            .attr("y2", function (d) {return d.target.y;});

        node
            .attr("transform", function (d) {return "translate(" + d.x + ", " + d.y + ")";});

        edgepaths.attr('d', function (d) {
            return 'M ' + d.source.x + ' ' + d.source.y + ' L ' + d.target.x + ' ' + d.target.y;
        });

        edgelabels.attr('transform', function (d) {
            if (d.target.x < d.source.x) {
                var bbox = this.getBBox();

                rx = bbox.x + bbox.width / 2;
                ry = bbox.y + bbox.height / 2;
                return 'rotate(180 ' + rx + ' ' + ry + ')';
            }
            else {
                return 'rotate(0)';
            }
        });
    }

    function dragstarted(d) {
        if (!d3.event.active) simulation.alphaTarget(0.3).restart()
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
    }

//    function dragended(d) {
//        if (!d3.event.active) simulation.alphaTarget(0);
//        d.fx = undefined;
//        d.fy = undefined;
//    }

</script>

</body>
</html>

"""

		new File(html).text = doc
	}

		
	
	def getPrefixedLabel(s) {
		def n = s.lastIndexOf("#")
		if (n < 0) n = s.lastIndexOf("/")
		def label = (s).substring(n+1)
		def ns = (s).substring(0,n+1)
		def pre = model.getNsURIPrefix(ns)
		if (!pre) return s
		"$pre:$label"
	}

	def cnt=1
	def getId(s){
		if (s.startsWith("http://")
			|| s.startsWith("vad:")
			|| s.startsWith("work:")
			|| s =~ /^b[0-9]+$/
			) return s.hashCode()
		return cnt++
	}
	
	def getData(sql) {

		def list0 = ju.queryListMap1(model,"",sql)

		def list = []
		list0.each{
			def m=[:]
				m.s = getPrefixedLabel(it.s)
				m.p = getPrefixedLabel(it.p)
				m.o = getPrefixedLabel(it.o)
			list += m
		}

		def l = [:]
		def g = []
		list.each{
			
				def sid = getId(it.s)
				l[sid] = it.s
				def oid = getId(it.o)
				l[oid] = it.o
				def l2 = [sid,oid,it.p]
				g.add l2
		}
		
		def nodes= ""
		l.each{k,v->
			nodes += """
{
			"id": $k,
			"label": "$v"
},
"""
		}
		def links=""
		g.each{
			links += """
{
			"source": ${it[0]},
			"target": ${it[1]},
			"type": "${it[2]}"
},
"""
		}
		[nodes,links]
	}
		
	def fixLabel(s) {
		def s0 = fixQuotes(s)
		//s0.replaceAll(/[ ]+/,"\\\\n")
		WordUtils.wrap(s0,WrapWidth)
	}

	def fixQuotes(s) {
		if (!s.contains('"')) return s
		def s1 = s.replaceAll(/"/,"\\\\\"")
		s1
	}

	def simplify(p) {
		if (p == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
			return "a"
		p
	}

	static def getTempFile() {
		File file = File.createTempFile("rdf",".tmp")
		file.deleteOnExit()
		file.absolutePath
	}
	
}
