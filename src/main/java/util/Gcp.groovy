package util

import java.text.SimpleDateFormat
import org.apache.commons.lang.NotImplementedException
import static groovy.io.FileType.FILES

class Gcp {

	static def gsutil = System.getProperty("os.name")
	.toLowerCase().startsWith("windows") ? "gsutil.cmd" : "/usr/local/bin/gsutil.cmd"
//	static def gsutil = "gsutil.cmd"
	
	// gcp gsutil copy from bucket to local
	static def gcpCp(src,file,tgt) {
		def bucket = System.getProperty("gcp_bucket")
		assert bucket, "no gcp bucket"
		gcpCp(src,file,tgt,bucket)
	}
	
	// TODO: refactor and reorg
	// gcp gsutil copy from bucket to local
	static def gcpCp(url,tgt) {

		def cmd = """$gsutil cp "${(""+url).trim()}" $tgt"""
		def oa = new Exec().execQuiet(cmd)
		if (oa[1].contains("Exception"))
			throw new RuntimeException("${oa[1]}")
		oa
	}
	// gcp gsutil copy from bucket to local
	static def gcpCp(src,file,tgt,bucket) {

		def cmd = """$gsutil cp "gs://$bucket/$src/$file" $tgt"""
		def oa = new Exec().execQuiet(cmd)
		if (oa[1].contains("Exception"))
			throw new RuntimeException("${oa[1]}")
		oa
	}
	// load the dir content of bucket-src to tgt dir
	// tgt dir is folder containing folder name
	// corresponding to (leaf) folder name under bucket
	// e.g., cp $bucket/ttl to /tmp copies to /tmp/ttl
	static def gcpCpDirRecurse(src,tgt,clobber,multithreaded) {
		def bucket = System.getProperty("gcp_bucket")
		assert bucket, "no gcp bucket"
		def cmd = """$gsutil ${multithreaded?"-m":""}  cp ${!clobber?"-n":""} -r "gs://$bucket/$src" $tgt"""
		def oa = new Exec().execQuiet(cmd)
		if (oa[1].contains("Exception"))
			throw new RuntimeException("${oa[1]}")
		oa
	}
	// no clobber
	static def gcpCpDirRecurse(src,tgt) {
		gcpCpDirRecurse(src,tgt,false,false)
	}
	// overwrite existing files at tgt
	static def gcpCpDirRecurseClobber(src,tgt) {
		gcpCpDirRecurse(src,tgt,true,false)
	}
	static def gcpLs(src,String file) {

		def bucket = System.getProperty("gcp_bucket")
		assert bucket, "no gcp bucket"
		def cmd = """$gsutil ls "gs://$bucket/$src/**/$file" """
		def oa = new Exec().execQuiet(cmd)
		if (oa[1].contains("Exception"))
			throw new RuntimeException("${oa[1]}")
		def url = oa.isEmpty() ? null : oa[0]
		url
	}
	static def gcpLs(src, boolean full) {

		def bucket = System.getProperty("gcp_bucket")
		assert bucket, "no gcp bucket"
		def url = "gs://$bucket/$src/"
		def cmd = """$gsutil ls ${full?"-l":""} $url """
		def al = new Exec().execQuiet(cmd)
		def l = sbToList(al[0])
//		if (! oa[1].contains("Operation completed"))
//			throw new RuntimeException("${oa[1]}")
		l.findAll{
			it != url
		}
	}
	// get only file specs
	static def gcpLsNoDir(src, boolean full) {
		def l = gcpLs(src, false)
		l.findAll{
			it =~ /.*[^\/]$/
		}
	}
	// get only dir specs
	static def gcpLsOnlyDir(src, boolean full) {
		def l = gcpLs(src, false)
		l.findAll{
			it =~ /.*[\/]$/
		}
	}
	static sbToList(sb) {
		def s = ""+sb
		def l = []
		s.split(/[\r\n]+/).each{
			l += it
		}
	}
	static def gcpLs(url) {

		def cmd = """$gsutil ls "$url" """
		def oa = new Exec().execQuiet(cmd)
//		if (! oa[1].contains("Operation completed"))
//			throw new RuntimeException("${oa[1]}")
		oa
	}
	// copy a file to a bucket
	static def gcpCpBucket(src,tgt) {
		def bucket = System.getProperty("gcp_bucket")
		assert bucket, "no gcp bucket"
		gcpCpBucket(src,tgt,bucket)
	}
	// copy a file to a bucket
	static def gcpCpBucket(src,tgt,bucket) {
//		throw new NotImplementedException()
		def cmd = """$gsutil cp $src "gs://$bucket/$tgt" """
		def oa = new Exec().execQuiet(cmd)
		if (oa[1].contains("Exception"))
			throw new RuntimeException("${oa[1]}")
		oa
	}
	
	static def folderCleanup(gDir,fDir,filter) {
		folderCleanup(gDir,fDir,filter, false)
	}
	// compares dates of gcp listing with
	// file dates for same name
	// deletes older files
	static def folderCleanup(gDir,fDir,filter, listOnly) {
		def m = getGcpMap(gDir)

		def m2 = getDirMap(fDir,filter)

		m.each{k,v->
			if (m2[k]) {
				println "$k, gcp:${v.date}, dir:${m2[k].date}, gcpIsNewer:${v.date>m2[k].date}"
				if (v.date>m2[k].date) {
					if (!listOnly) {
						m2[k].path.delete()
					}
					println "delete ${m2[k].path}"
				}
			}
		}
	}

	// returns map of file data
	// for a folder and a filter
	static def getDirMap(src, filter) {
		def m = [:]
		def dir = new File(src);
		def files = [];
		dir.traverse(type: FILES, maxDepth: 0) { files.add(it) }

		files.findAll{
			it.name =~ filter
		}.each{

			def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
			TimeZone gmtTime = TimeZone.getTimeZone("GMT")
			sdf.setTimeZone(gmtTime)
			def dt = sdf.format(new Date(it.lastModified()))
			m[it.name] = [size:it.length(), date:dt, path:it.getAbsoluteFile() ]
		}
		m
	}

	// returns map of gcp data
	static def getGcpMap(src) {
		def m = [:]
		def l = Gcp.gcpLs(src, true)
		l
				.findAll{
					it.split(/[ ]+/).size() == 4
				}
				.each{
					def l2 = it.split(/[ ]+/)
					if (!(l2[3].endsWith("/"))) { // exclude folder entries
						def name = (l2[3] =~ /.*\/([A-Za-z0-9_\-\.',!]+)$/)[0][1]
						m[name] = [size:l2[1], date:l2[2], path:l2[3]]
					}
				}
		m
	}


}


