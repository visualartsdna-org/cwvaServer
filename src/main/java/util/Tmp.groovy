package util

class Tmp {
	
	// local temp files
//	def temps=[]
	
	static def getTemp(pre,suf) {
		File tempFile = File.createTempFile(pre, suf)
		tempFile.deleteOnExit()
//		temps << tempFile
		tempFile.absolutePath
	}
	
//	def rmTemps() {
//		temps.each{
//			it.delete()
//		}
//	}
	
	static def delTemp(tmp) {
		new File(tmp).delete()
	}
	static def getTemp(suf) {
		new Tmp().getTemp("tmp",suf)
	}
}
