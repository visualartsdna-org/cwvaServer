package support

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import net.glxn.qrgen.javase.*
import net.glxn.qrgen.core.image.ImageType

// https://github.com/kenglxn/QRGen
class QRCode {


	def ns = "http://visualartsdna.org/work"
	@Test
	public void test() {
		def dir = "C:/test/archivo/qrcodes"
		def guids=[
 "5351f503-c8fe-45e2-8758-5db5b7778160", 
 "80ce0d3c-ca47-4500-abfb-b2ebf3681306", 
 "04e5719d-0480-4849-aa3d-af7c3b003f39", 
 "1ad5c641-36a5-49f7-89bb-a206a2b9237f", 
 "6badcbaf-245b-4424-9b53-45d2c466fdc8", 
 "d5c5acfb-0b56-4e85-9b82-e23c0ca1b830", 
 "028bb289-ef6d-4aa2-86c7-6aa31eae9eeb", 
 "7ac88212-b03b-4b19-8539-5c57fdd98643", 
 "c0e07525-9187-4b18-a628-0b2872a58995", 
 "e9c22488-58bc-4e75-a879-96750219d03d", 
 "487f169f-895a-425d-bcae-bed121d53c58", 
 "6ccc8d5f-9da3-484b-94dd-72702c64af5e", 
 "c98170ae-ca57-43cd-8d95-69596ab85bfd", 
 "3a19c2e6-f1b5-454b-8635-74e91d6f66b8", 
 "07175c65-14f0-46c6-bdea-9fc3b781ca5b", 
 "d4cde14d-abae-4250-b387-e929f0e49023", 
 "edf5b0c9-464f-4e82-9ae3-22f95d800d79", 
 "7890a1f2-f762-42bd-89be-c15feb1242ee", 
 "cf8aae8b-e9df-46ad-bb66-0f6acefd5d37", 
 "0581d4f8-75b0-4215-bba4-f48ca36a7822", 
 "b3385ea6-d1f2-4f91-934d-3cfbbeaabd60", 
 "bd431407-6688-4e90-866f-fc2774ea6227", 
 "1cfc0f4e-32c4-4811-a621-0eb55cf84904", 
 "780dd45c-ee4c-4762-bdf3-c78705353302", 
 "174e4562-aaba-4833-a5af-fd7380c026ad", 
 "855bbde2-013b-4cc3-b0ae-8c3ea9eea4d0", 
 "30ff0d6f-00a6-4cdf-b6e3-fceb305f49a2", 
 "6b0dc944-7b09-455a-9f72-fc3f802274fe", 
 "7d41564e-29e4-4b18-9545-9b10cab74219", 
 "d022594c-28f0-44f0-95e3-4994fe6ffd1f", 
 "e9688ebf-ec70-4cec-8ee0-d570a1a3f67c", 
 "54496be2-43e4-414c-b41a-a587d806ff59", 
 "f386d8dd-c43a-4769-8c6a-1caf14dcb9f1", 
 "78002010-23a3-48a1-a35f-778fd3a1e70a", 
 "160969ee-3652-4116-a184-213cb0f0e656", 
 "0acc6d02-f34e-4ee2-bed2-9f9a3863f492", 
 "5552e554-47fd-4e7c-aedc-7e47529d16cf", 
 "cb0820f2-f71c-4f08-b5f7-aa66eaf28853", 
 "affcf3a9-9e38-4937-a7ab-cb396be04c14", 
 "67de65c6-7d00-4743-9814-fd500efe712d", 
 "b721d2fa-a94a-4cc0-bf9a-346899196f50", 
 "b59527e9-0db2-4fa4-aa3e-f564a9883cdb", 
 "7cf7d128-1e61-43da-88f8-8493b5d8d195", 
 "81d95b64-84fb-4acf-9fc4-44218d6fad01", 
 "812cb5a1-ae9f-4e77-a09d-d22e630f616e", 
 "579e4ae3-0fb5-448c-926f-4bff27f5c71b", 
 "496d6fa0-1cc1-48c8-981b-7d6bbf02d290", 
 "eb77aab9-a520-4922-a2f4-977cb539dee7", 
 "a043bdfc-36da-4877-a001-8a7a5342f2b6", 
 "c72cc7b8-d479-46ca-9440-b625e6eb8ff9", 
 "41334883-bf33-4571-9fd8-039435cdf232", 
 "0cef5f83-92b4-4ca4-b3ad-6042ff60a548", 
 "d004e6bf-ac94-4be9-a026-5795a2358936", 
 "bbb17d42-fb4f-4558-bfe3-91e19fe56649", 
 "0e92ca4e-c849-414b-bd7a-50013ecfcedb", 
 "81344303-73ea-4f27-9380-936b38422071", 
 "167bf795-e124-4869-842d-d96fa2f500da", 
 "d22c9803-e023-4e3b-a2ca-aee6563bc740", 
 "2f6cf64e-6adc-43fe-9717-51bd60f388c7", 
 "3509c74f-d34a-4923-89e2-b85c82a4e920", 
 "0ac44188-5c65-4b2f-b3e6-a21df964fda7", 
 "b6e8617d-29db-43d1-ad80-975aa50b2907", 
 "3e612d3f-ca2e-49b7-9a0c-fb80ae6711e2", 
 "75b4d9e8-25e9-4117-93b2-cb08fdfc6508", 
 "450a08ef-c4fd-47ed-bb7d-cc65de9ac91b", 
 "ee454490-fb77-43ed-865f-b746bb9198ec", 
 "47cb1430-4815-4f20-aa19-ad357285154e", 
 "27570895-3eca-4dc4-b651-5797385badf9", 
 "4449a460-447d-4baa-a7f7-51a21858a400", 
 "3e3b6b06-b307-4ed0-8c0e-186f707a1193", 
 "a37d0557-c5f6-46c9-a2d3-8e819a15ce1b", 
 "e437e64e-7d04-4245-9897-99937d5a726f", 
 "07ee84be-1ba1-45e9-8516-f3d1ef76a533", 
 "07a3385f-e150-4d5d-9422-c65c23798873", 
 "50292ae6-83bf-4fd2-b139-34330baec872", 
 "20bc7dd1-a27e-4547-83e1-ca78c271fb7d", 
 "1824398c-3295-4aff-8057-79cc9525943c", 
 "d863d930-5415-4aad-a571-688889e4811c", 
 "284348f7-beda-4534-b1bd-8badf3a80843", 
 "6cba8f54-2be8-42c0-8cac-9f59a8ba83e7", 
 "82db59c2-3889-4467-8c0d-2ea00f226f41", 
 "d054064d-48d7-4e7d-a8c4-93f25e7eaee0", 
 "e10fb610-58ad-4579-872b-1dc0ec7a282e", 
 "ddebd28e-77e7-4352-aaf4-aca2bd802619", 
 "366e45c2-0ade-4c6c-9bc2-f6cc3417fb62", 
 "335f551f-85a2-4dbf-a043-0cf6cc3e8baf", 
 "53dea64d-1b77-4ffc-98e7-2fb93d674670", 
 "4c950576-6c8f-43db-8d43-9fa5d4d08cd7", 
 "052a2946-2198-4dba-a157-415e6d50a575", 
 "4dabc675-4689-4829-a40c-5236d705e8d8", 
 "d065794a-669c-490b-a7b0-55271307b8a7", 
 "1420e422-83b5-4cb2-813b-72d1ba2b3676", 
 "a57d6288-2d7a-4b95-aff9-f6a37902061e", 
 "311b973e-72c8-43e4-8ce6-1b701487d3ed", 
 "5978557a-62f4-4250-96f9-b9b219151ace", 
 "415970f1-edef-4f2c-88b6-9c013ce22697", 
 "3843885e-b57b-4eac-9554-40ad94299679", 
 "a9459870-6d05-44ef-801a-def08c79ba84", 
 "71bd84a7-d5ce-40e9-baf1-e4c332ff1b31", 
 "529a7a22-0b04-464e-b827-e1b8b825ee3b", 
 "c31be610-ced8-45d0-b0a7-1be4519a8681", 
 "bde7aec2-e445-4854-aada-81878d56828d", 
 "810c02b1-ce1c-4e73-9fa8-e65bcc68f650", 
 "94d0ac70-a358-4ca2-a89a-8f584344765e", 
 "4a5134f1-ffd6-492a-bb5d-ca503eb1c71b", 
 "37fb05da-da16-407e-9b2b-96c5819459c3", 
 "25511984-4a7b-4fce-94f6-4e8f2505a4db", 
 "9e35e4ab-77f5-4ed1-bc15-f869357846d5", 
 "09e0fe93-23ef-47ec-95ee-3d065541fb62", 
 "1c39e525-3acf-4290-9938-74e258b6ccd7", 
 "ad599b0c-9531-4d0a-a8c6-c9552212f3f0", 
 "56fa8a4e-f4ac-4020-843b-d3c74ce6aadd", 
 "2652a402-efde-4c24-a82f-23b2c94c6a51", 		].each{guid->
			qrcode(guid,dir)
		}
	}
	

	def qrcode(guid,dir) {
		def qrcName = "qrc_${guid}.jpg"
		def qrcFile = new File("$dir/$qrcName")
		if (!qrcFile.exists()) {
			File file = net.glxn.qrgen.javase.QRCode.from("$ns/$guid").to(ImageType.JPG).file()
			def newFile = new File("$dir/$qrcName")
			newFile << file.bytes
		}

	}

}
