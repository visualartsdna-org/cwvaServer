package util

import org.apache.commons.lang.NotImplementedException

class Gcp {

//	static def gsutil = System.getProperty("os.name")
//	.toLowerCase().startsWith("windows") ? "gsutil.cmd" : "sudo gsutil"
	static def gsutil = "gsutil.cmd"
	
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
	static def gcpCpDirRecurse(src,tgt,clobber) {
		def bucket = System.getProperty("gcp_bucket")
		assert bucket, "no gcp bucket"
		def cmd = """$gsutil cp ${!clobber?"-n":""} -r "gs://$bucket/$src" $tgt"""
		def oa = new Exec().execQuiet(cmd)
		if (oa[1].contains("Exception"))
			throw new RuntimeException("${oa[1]}")
		oa
	}
	// no clobber
	static def gcpCpDirRecurse(src,tgt) {
		gcpCpDirRecurse(src,tgt,false)
	}
	// overwrite existing files at tgt
	static def gcpCpDirRecurseClobber(src,tgt) {
		gcpCpDirRecurse(src,tgt,true)
	}
	static def gcpLs(src,String file) {

		def bucket = System.getProperty("gcp_bucket")
		assert bucket, "no gcp bucket"
		def cmd = """$gsutil ls "gs://$bucket/$src/**/$file" """
		def oa = new Exec().execQuiet(cmd)
		if (oa[1].contains("Exception"))
			throw new RuntimeException("${oa[1]}")
		oa
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
	static def gcpLsNoDir(src, boolean full) {
		def l = gcpLs(src, false)
		l.findAll{
			it =~ /.*[^\/]$/
		}
	}
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
	static def gcpCpBucket(src,tgt,bucket) {
		throw new NotImplementedException()
	}
}


