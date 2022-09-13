package util

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class ExecTest {
	
	// TODO: remove before commit
	def bucket = ""

	@Test
	void test6() {
		def tgt = "C:/test/junk"
		//def src = "images"
		def file = "gs://$bucket/images/study/crocus/IMG_1944.jpg"
		def f = Gcp.gcpCp(file,tgt)
		println "$f"
	}
	
	@Test
	void test5() {
		def url = "gs://$bucket/images/study/crocus/IMG_1944.jpg"
		def a = Gcp.gcpLs(url)
		println "${a[0]}"
		println "${a[1]}"
		if (a[0].isEmpty() 
			&& a[1].contains("One or more URLs matched no objects"))
			return null
		a[0]
	}
	
	@Test
	void test4() {
		//def tgt = "C:/temp/images"
		def src = "images"
		def file = "IMG_1944.jpg"
		def a = Gcp.gcpLs(src,file)
		println "${a[0]}"
		println "${a[1]}"
		if (a[0].isEmpty() 
			&& a[1].contains("One or more URLs matched no objects"))
			return null
		a[0]
	}
	
	@Test
	void test() {
		def tgt = "C:/test/junk"
		//def src = "images"
		def file = "IMG_1944.jpg"
		def f = FileUtil.loadImage(tgt,file)
		println "$f"
	}
	
	@Test
	void test2() {
		def tgt = "C:/test/junk"
		//def src = "images"
		def file = "Mandate.jpg"
		def f = FileUtil.loadImage(tgt,file)
		println "$f"
	}
	
	@Test
	void test1() {
		def tgt = "C:/temp/junk"
		def src = "images"
		def file = "Mandate.jpg"
		def oa = Gcp.gcpCp(src,tgt,file)
		println "out = ${oa[0]}"
		println "err = ${oa[1]}"
	}
	
	@Test
	void test0() {
		def tgt = "C:/temp/junk"
		def src = "images"
		def file = "Mandate.jpg"
		def bucket = ""
		def oa = Gcp.gcpCp(src,tgt,file,bucket)
		println "out = ${oa[0]}"
		println "err = ${oa[1]}"
	}
	

}
