package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import rdf.JenaUtils
import rdf.tools.SparqlConsole
import util.Guid
import groovy.io.FileType

class TestLoadEbayLists {

	def ml = [
		// grp1
		"IMG_9779.JPG" :	"http://visualartsdna.org/work/15d407f6-53ec-4d6b-aaca-dd89bd18cc17",
		"IMG_9781.JPG" :	"http://visualartsdna.org/work/9a01b960-d45d-4b1c-9fd3-49582f9ee5f6",
		"IMG_9783.JPG" :	"http://visualartsdna.org/work/3edce4bc-7179-4646-9d10-83730c56735a",
		"IMG_9788.JPG" :	"http://visualartsdna.org/work/091d9ffe-5f00-42e9-b304-3ee127d5fcb3",
		"IMG_9791.JPG" :	"http://visualartsdna.org/work/a25f2373-f62d-44ed-acda-a63fee9bb6a2",
		"IMG_9794.JPG" :	"http://visualartsdna.org/work/23b90ced-c943-4cac-bf7c-00344badba8a",
		"IMG_9797.JPG" :	"http://visualartsdna.org/work/9da4bdb5-cd9d-42c1-b5f5-97066967b2d2",
		"IMG_9799.JPG" :	"http://visualartsdna.org/work/21adf909-3eec-4771-9cb5-9368484b76fa",
		"IMG_9801.JPG" :	"http://visualartsdna.org/work/e6d515fb-0201-4702-8059-072af0379829",
		"IMG_9803.JPG" :	"http://visualartsdna.org/work/ce9dfb4a-afdf-497a-a12f-c22f736df3f6",
		"IMG_9805.JPG" :	"http://visualartsdna.org/work/16495b38-2337-436d-a464-4c2a636c6551",
		"IMG_9807.JPG" :	"http://visualartsdna.org/work/11a9080a-a0f0-4bd4-9385-b8085008bcb4",
		"IMG_9809.JPG" :	"http://visualartsdna.org/work/0030af5f-9133-4156-a214-72672bf470e9",
		//grp2
		"IMG_9812.JPG" : "http://visualartsdna.org/work/ae363eda-62cb-43ad-b7f7-daa43db31713",
		"IMG_9815.JPG" : "http://visualartsdna.org/work/7541eba8-388a-4534-a9ab-e3b6893c3b87",
		"IMG_9817.JPG" : "http://visualartsdna.org/work/c8491fad-a0fb-4a9d-8547-10f57e388ad4",
		"IMG_9819.JPG" : "http://visualartsdna.org/work/d26985f2-0e79-4cbe-979f-050ea880b26e",
		"IMG_9821.JPG" : "http://visualartsdna.org/work/480ca005-b879-46e1-865f-ea16d14db4ef",
		"IMG_9823.JPG" : "http://visualartsdna.org/work/da79b4be-3442-4b6b-bdb4-107b2682c560",
		"IMG_9825.JPG" : "http://visualartsdna.org/work/351b879f-0680-4817-a338-729c7cd083a1",
		"IMG_9828.JPG" : "http://visualartsdna.org/work/290bd418-9464-419a-9024-0f20155952fb",
		"IMG_9831.JPG" : "http://visualartsdna.org/work/7fb88759-7927-4184-b6a8-14ef26c7e72d",
		"IMG_9834.JPG" : "http://visualartsdna.org/work/bc1db886-d33d-418d-b1d1-379ca081df89",
		"IMG_9837.JPG" : "http://visualartsdna.org/work/7c98f6db-ec70-4b23-ace8-8711b0cf77cd",
		"IMG_9840.JPG" : "http://visualartsdna.org/work/1920062f-09e6-4f6f-8fca-e7e537f2e1cf",
		"IMG_9844.JPG" : "http://visualartsdna.org/work/c14260da-e0ed-4591-b19e-e91ea9439bf1",
		"IMG_9847.JPG" : "http://visualartsdna.org/work/a3288a41-d632-4f9a-b72d-52607e4b6d0d",
		"IMG_9850.JPG" : "http://visualartsdna.org/work/8da5ad54-36d4-42b0-8705-ff643151ed62",
		"IMG_9853.JPG" : "http://visualartsdna.org/work/d4d3eb34-c2a4-4b1b-a3b7-2fd2f7743890",
		"IMG_9856.JPG" : "http://visualartsdna.org/work/977dee18-5ca3-4ff0-a61e-2abbc72d379f",
		"IMG_9859.JPG" : "http://visualartsdna.org/work/d967db08-869e-4978-8af3-0dd6819aad0d",
		"IMG_9861.JPG" : "http://visualartsdna.org/work/c49cb9ed-6c27-4d5e-b4a9-9a11808f526c",
		"IMG_9864.JPG" : "http://visualartsdna.org/work/f474403f-7d91-4c7f-a980-5c87eb49efa4",
		
		//grp3
		"IMG_9867.JPG" : "http://visualartsdna.org/work/e66844d4-ddd6-41a8-85f2-3e5afc73227a",
		"IMG_9870.JPG" : "http://visualartsdna.org/work/b9f0ea89-b5d7-49aa-b692-133a35ed5717",
		"IMG_9873.JPG" : "http://visualartsdna.org/work/cac65a74-c7ce-41f3-8cf1-2249e042238d",
		"IMG_9877.JPG" : "http://visualartsdna.org/work/5efd8ca4-4108-4cf7-a744-69e43c1e1821",
		"IMG_9880.JPG" : "http://visualartsdna.org/work/ef2e8679-4d34-4074-8a56-57c29201ebb6",
		"IMG_9883.JPG" : "http://visualartsdna.org/work/0a8cb92b-39e7-4dc9-972e-25007d8c6efc",
		"IMG_9886.JPG" : "http://visualartsdna.org/work/04f15f5f-d17a-485f-88d6-64fb42a7b4db",
		"IMG_9889.JPG" : "http://visualartsdna.org/work/ba1c53bf-9215-4e51-b0d9-c66fbf84b69a",
		//grp4
		"IMG_9892.JPG" : "http://visualartsdna.org/work/58249634-9cc1-4d22-98a2-489e774ca709",
		"IMG_9895.JPG" : "http://visualartsdna.org/work/542671b5-4001-499d-a107-4f24b7e36a0a",
		"IMG_9898.JPG" : "http://visualartsdna.org/work/a8d26f91-5b75-491e-b037-13671cf126d7",
		"IMG_9901.JPG" : "http://visualartsdna.org/work/40520fc6-d99e-4f67-b0b7-ee18a38c0e22",
		"IMG_9904.JPG" : "http://visualartsdna.org/work/f7e95bf4-d611-4799-b2ed-78e104a6c936",
		"IMG_9907.JPG" : "http://visualartsdna.org/work/e6a1bdb4-e1ff-4901-a303-9863b91406ec",
		"IMG_9910.JPG" : "http://visualartsdna.org/work/1f0cbe30-97da-4b7a-b068-0d168f45d33f",
		"IMG_9914.JPG" : "http://visualartsdna.org/work/ccdcedb6-e9a6-43a3-bc30-9bd17194c353",
		"IMG_9917.JPG" : "http://visualartsdna.org/work/11767189-943e-4dc9-9119-ef14b440543c",
		"IMG_9920.JPG" : "http://visualartsdna.org/work/0c55f6b3-8512-47f1-a9cb-7a2fe0626fed",
		//grp5
		"IMG_9923.JPG" : "http://visualartsdna.org/work/0257b921-b64f-4ba2-be89-14bb3b12c253",
		"IMG_9926.JPG" : "http://visualartsdna.org/work/f3c20abf-5978-4f82-affc-005482a5acbe",
		"IMG_9929.JPG" : "http://visualartsdna.org/work/32298413-7343-471f-b921-866d1bf2f4af",
		"IMG_9934.JPG" : "http://visualartsdna.org/work/da4bccc6-dfd3-4062-ae5b-3756e4eed354",
		"IMG_9937.JPG" : "http://visualartsdna.org/work/b89ca4b5-f28b-4a0f-b30f-91973058d670",
		"IMG_9940.JPG" : "http://visualartsdna.org/work/b8adb67a-e5d4-4ef8-a4e2-e2c626b8a42f",
		"IMG_9943.JPG" : "http://visualartsdna.org/work/739787c3-6a3a-47ba-98f3-5b66293c3069",
		"IMG_9946.JPG" : "http://visualartsdna.org/work/358a6706-dafc-4f42-a5ba-614b31526f3a",
		"IMG_9949.JPG" : "http://visualartsdna.org/work/b767a70b-31ce-4953-9e2d-ad7b5bcd248e",
		"IMG_9952.JPG" : "http://visualartsdna.org/work/4b72a59a-7c26-4093-80d1-91dc971e821f",
		"IMG_9955.JPG" : "http://visualartsdna.org/work/c93fca25-f9a8-4004-b0d2-a7bf3abd78bf",
		"IMG_9958.JPG" : "http://visualartsdna.org/work/c7f10694-e259-457f-837d-28bed16e0b4f",
		"IMG_9961.JPG" : "http://visualartsdna.org/work/57061844-2820-478d-862b-332670ddb5f8",
		"IMG_9965.JPG" : "http://visualartsdna.org/work/435bc499-7096-4090-9f76-b88121749253",
		//grp6
		"IMG_9973.JPG" : "http://visualartsdna.org/work/6c7b5950-9506-4d6d-9bd5-eb1fbdde5cdb",
		"IMG_9976.JPG" : "http://visualartsdna.org/work/e93ce543-d4cb-4282-a4ed-52fb58a939b1",
		"IMG_9979.JPG" : "http://visualartsdna.org/work/a2566698-a78c-43f5-bf9a-2a698b7fe1d3",
		"IMG_9982.JPG" : "http://visualartsdna.org/work/8d749567-2acf-496e-9790-089ccae302df",
		"IMG_9985.JPG" : "http://visualartsdna.org/work/12c48084-a354-4d59-8e53-1f7138311cf4",
		"IMG_9988.JPG" : "http://visualartsdna.org/work/ffe74108-2f3e-411e-b0aa-429cc576abcc",
		
	]

	def subdirs = [
		"eBay gp 1",
		"eBay gp 2",
		"eBay gp 3",
		"eBay gp 4",
		"eBay gp 5",
		"eBay gp 6",
	]

	@Test
	void testConsole() {
		def ju = new JenaUtils()
		def mod = ju.loadFiles("c:/stage/server/cwvaContent/ttl")
		mod.add ju.loadFiles("G:/My Drive/art/eBay/listings/listings.ttl")
		
		new SparqlConsole().show(mod)
		
	}
	
	@Test
	void testGenerate() {
		def base = "G:/My Drive/art/eBay/listings"
//		def fs = "$base/${subdirs[0]}"

		def pre = ""
		def map = [:]
		def grpMap = [:]
		subdirs.each{subdir->
			def list = []
			def dir = new File("$base/$subdir")
			dir.eachFileRecurse (FileType.FILES) { file ->
				list << file
			}
			def ascendingOrder = list.sort { it.name } // Sort by last modified date in ascending order

			def id
			def ttl
			ascendingOrder.each { file ->
				def uri = ml[file.name]
				if (uri) {
					ttl = new URL("${uri}?format=ttl") .getText()
					id = getId(ttl)
					if (!pre) {
						ttl.eachLine{
							if (it.startsWith("@"))
								pre += "$it\n"
						}
					}
				}
				if (!map[id])map[id]= []
				map[id] += file.name
			grpMap[id] = subdir
			}
		}
		def ms = printTtl(map, grpMap, pre)
		new File("$base/listings.ttl").text = ms
		def m = new JenaUtils().loadFileModelFilespec("$base/listings.ttl")
		println "model=${m.size()}"
	}

	def printTtl(map,grpMap,pre) {

		def s = """
$pre
"""
		map.each{k,v->
			s += """
	work:${new Guid().get()}
		a vad:Listing ;
		schema:datePublished	"2023-03-08T08:55:08"^^xs:date ;
		vad:reference	$k ;
		vad:group "${grpMap[k]}" ;
		schema:image	"""
			int i=0
			v.each{
				if (i++) s+= ","
				s += "\"$it\""
			}
			s += " ;\n  ."
		}
		s
	}

	def getId(ttl) {

		def id
		ttl.eachLine{
			if (it.startsWith("work:"))
				id =  it.trim()
		}
		id
	}
}
