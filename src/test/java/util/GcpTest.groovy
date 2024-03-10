package util


import static org.junit.jupiter.api.Assertions.*

import java.text.SimpleDateFormat

import static groovy.io.FileType.FILES

import org.junit.jupiter.api.Test

class GcpTest {

	@Test
	void test() {
		def gDir = "images"
		def fDir = "/temp/images"
		def filter = /.*\.JPG|.*\.jpg/
		Gcp.folderCleanup(gDir,fDir,filter,true)
	}

	@Test
	void test4() {
		//def m = getGcpMap("images")

		def m2 = Gcp.getDirMap("/temp/images",/.*\.JPG|.*\.jpg/)

		m2.each{k,v-> println "$k=$v"}
	}

	@Test
	void test3() {
		def m = Gcp.getGcpMap("images")

		m.each{k,v-> println "$k=$v"}
	}

	@Test
	void test2() {
		def m = [:]
		def l = Gcp.gcpLs("images", true)
		l
				.findAll{
					it.split(/[ ]+/).size() == 4
				}
				.each{
					def l2 = it.split(/[ ]+/)
					def name = (l2[3] =~ /.*\/([A-Za-z0-9_\-\.',!]+)$/)[0][1]
					m[name] = [size:l2[1], date:l2[2], path:l2[3]]
				}

		m.each{k,v-> println "$k=$v"}
	}

	@Test
	void test1() {
		def l = Gcp.gcpLs("images", true)
		l
				.findAll{
					it.split(/[ ]+/).size() == 4
				}
				.each{
					println it
				}
	}

	@Test
	void test01() {
		def l = Gcp.gcpLsNoDir("images", false)
		l.each{ println it}
	}

	@Test
	void test0() {
		def l = Gcp.gcpLs("images", true)
		l.each{ println it}
	}

}
