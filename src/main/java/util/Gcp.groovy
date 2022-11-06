package util

class Gcp {

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
		if (! oa[1].contains("Operation completed"))
			throw new RuntimeException("${oa[1]}")
		oa
	}
	// gcp gsutil copy from bucket to local
	static def gcpCp(src,file,tgt,bucket) {

		def cmd = """$gsutil cp "gs://$bucket/$src/$file" $tgt"""
		def oa = new Exec().execQuiet(cmd)
		if (! oa[1].contains("Operation completed"))
			throw new RuntimeException("${oa[1]}")
		oa
	}
	static def gcpLs(src,String file) {

		def bucket = System.getProperty("gcp_bucket")
		assert bucket, "no gcp bucket"
		def cmd = """$gsutil ls "gs://$bucket/$src/**/$file" """
		def oa = new Exec().execQuiet(cmd)
//		if (! oa[1].contains("Operation completed"))
//			throw new RuntimeException("${oa[1]}")
		oa
	}
	static def gcpLs(src, boolean full) {

		def bucket = System.getProperty("gcp_bucket")
		assert bucket, "no gcp bucket"
		def cmd = """$gsutil ls ${full?"-l":""} "gs://$bucket/$src" """
		def oa = new Exec().execQuiet(cmd)
//		if (! oa[1].contains("Operation completed"))
//			throw new RuntimeException("${oa[1]}")
		oa
	}
	static def gcpLs(url) {

		def cmd = """$gsutil ls "$url" """
		def oa = new Exec().execQuiet(cmd)
//		if (! oa[1].contains("Operation completed"))
//			throw new RuntimeException("${oa[1]}")
		oa
	}
}


