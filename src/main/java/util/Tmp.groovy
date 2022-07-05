package util

class Tmp {
	
		// local temp files
		def temps=[]
		
		def getTemp(pre,suf) {
			File tempFile = File.createTempFile(pre, suf)
			tempFile.deleteOnExit()
			temps << tempFile
			tempFile.absolutePath
		}
		
		def rmTemps() {
			temps.each{
				it.delete()
			}
		}
	static def delTemp(tmp) {
		new File(tmp).delete()
	}
	static def getTemp(suf) {
		getTemp("tmp",suf)
	}
}
