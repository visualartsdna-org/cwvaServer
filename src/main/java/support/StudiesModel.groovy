package support
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.Font
import java.awt.BasicStroke
import javax.imageio.ImageIO
import org.junit.jupiter.api.Test
import rdf.JenaUtilities
import java.awt.Graphics2D

class StudiesModel {
	def rdfs
	def sf
	def ju = new JenaUtilities()
	StudiesModel(rdfs,sf) {
		this.rdfs = rdfs
		this.sf = sf
	}

	def process(path,query) {

		def studyGUID = (path =~ /^\/study\/([0-9A-Fa-f\-]+)$/)[0][1]
		def imgDir = "/images/study/tmp"
		def tmp = new util.TmpExpire(1000 * 2,1000 * 60 * 2,
				"C:/temp/images/study/tmp")
		def sb = new StringBuilder()
		sb.append """
<html>
<head>
<style>
table, th, td {
  border: 1px solid black;
  border-collapse: collapse;
}
</style>
</head>
<body>
<h3><a href="../studies">Back to studies</a></h3>
<br/>
<br/>
<br/>
<table>
"""
		def l = ju.queryListMap1(rdfs,rdf.Prefixes.forQuery,"""
			select  ?label ?file ?height ?width ?x ?y ?def ?note ?tag ?work {
			bind(work:$studyGUID as ?s)
			?s a vad:Project .
			?s the:design ?d .
			?d vad:filename ?file .
			optional {
				?d the:work ?work 
			}
			?d the:member ?m .
			?m rdfs:label ?label .
			?m vad:height ?height .
			?m vad:width ?width .
			?m vad:x ?x .
			?m vad:y ?y .
			optional {
				?m skos:definition ?def .
				?m skos:note ?note .
				?m the:tag ?tag .
			}
			} order by ?file
			""")
			
		def work = [:]
		def m2 = [:]
		l.each{m->
			work[m.file] = m.work ? m.work : null
			if (!m2[m.file]) m2[m.file] = [:]
			if (!m2[m.file][m.label]) m2[m.file][m.label] = [:]
			m2[m.file][m.label].height = m.height as int
			m2[m.file][m.label].width = m.width as int
			m2[m.file][m.label].x = m.x as int
			m2[m.file][m.label].y = m.y as int
			m2[m.file][m.label].def = m.def
			m2[m.file][m.label].tag = m.tag
			m2[m.file][m.label].note = m.note
			
		}

		m2.each{k,v->
				
			def ifile = "/temp/images/study/${k}"
			def ofile = tmp.getTemp("study",".jpg")
			def oname = new File(ofile).name
			def img = "$imgDir/$oname"
			annote(ifile,ofile,v)
			sb.append """
<tr><td>
<img src="${img}" style="width:800px">
</td><td>
$ifile
<br/>
${work[k]?"work: "+work[k]:""}
<table>
"""
			int n=1
			v.each{k2,v2->
				sb.append """
<tr>
<td>${n++}</td>
<td>${k2}</td>
<td>${v2.def?v2.def:""}</td>
<td>${v2.note?v2.note:""}</td>
<td>${v2.tag?v2.tag.replaceAll("http://visualartsdna.org/thesaurus/","the:"):""}</td>
</tr>
"""
			}
			sb.append """
</table>
"""
		}
		sb.append """
</td></tr>
</table>
</body>
</html>

""" 
		""+sb
	}


	def annote(ifile,ofile,m ){
		BufferedImage sbi = ImageIO.read(new File(ifile));
		assert sbi, "Image file $ifile is not JPG"
		def ht = sbi.getHeight()
		//def wt = sbi.getWidth()
		//println "$ht, $wt, $ifile"
		def fsize = 27 * (ht / 1000) as int
		def offset = 20 * (ht / 1000) as int
		Font nodeFont = new Font ("Courier New", 1, fsize)
		def size = 4 * (ht / 1000) as int
		Graphics2D ig2
		ig2 = sbi.createGraphics();
		ig2.setFont (nodeFont)
		ig2.setPaint(Color.black);
		ig2.setStroke(new BasicStroke(size))
		int n=1
		m.each{k,v->
			ig2.drawRect(v.x as int,
				 v.y as int,
				  v.height as int,
				   v.width as int
				   )
		ig2.setPaint(Color.white);
		
			ig2.fillRect(
				(v.x as int) +offset,
				(v.y as int),
				  offset,
				   offset
				   )
		
		ig2.setPaint(Color.black);
			ig2.drawString("${n++}", 
				(v.x as int) +offset, 
				(v.y as int) +offset)
		}
		File img = new File(ofile)
		ImageIO.write(sbi, "JPEG", img);
	}
	
	def directory(path,query) {
		def l = ju.queryListMap1(rdfs,rdf.Prefixes.forQuery,"""
			select  ?s ?l ?dt {
			?s a vad:Project .
			?s rdfs:label ?l .
			?s schema:datePublished ?dt .
			} order by ?l desc(?dt)
			""")
			
	def sb = new StringBuilder()
	sb.append """
<h2>Projects</h2>
<br/>
<br/>
<br/>
"""
		l.each{m->
			def guid = (m.s =~ /^.*\/([0-9A-Fa-f\-]+)$/)[0][1]

			sb.append """
			<a href="http://localhost:8083/study/${guid}">${m.l}</a>
${m.dt}
<br/>
"""
		}
		sb.append """
"""
		""+sb
		
	}
}