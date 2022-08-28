package util

import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock

/**
 * Create temp files with an expiration
 * 
 * @author ricks
 *
 */
class TmpExpire {

	def period = 1000 * 10 * 1 // how often to check
	def expire = 1000 * 60 * 1 // expire time
	def dir

	/**
	 * Create an instance to manage
	 * temp file creation and expiration  
	 * @param per period of checking temp file expirations
	 * @param exp minimum amount of time until temp file expiration
	 */
	TmpExpire(per,exp){
		this(per,exp,(File)null)
	}
	
	TmpExpire(per,exp,String dir){
		this(per,exp,new File(dir))
	}
	
	TmpExpire(per,exp,File dir){
		this.period = per
		this.expire = exp
		this.dir = dir
		
		new Thread().start{

			while (true) {
				def delList = []
				getTemps().each{
					if (it.exists()) {
						if (expire(it)) {
							try {
								it.delete()
								delList << it
							} catch(Exception e) {
								println "error deleting temp $it, $e"
							}
						}
					}
				}
				delList.each{ del it }
				sleep(period)
			}
		}
	}

	// local temp files
	final def temps=[]

	@WithWriteLock
	void add(item) {
		temps << item
	}

	@WithWriteLock
	void del(item) {
		if (temps.contains(item))
			temps.remove item
	}

	@WithReadLock
	List getTemps() {
		temps
	}

	def expire(t) {
		def time = t.lastModified()
		def ctms = System.currentTimeMillis()
		time + expire < ctms
	}

	/**
	 * create an expiring temp file
	 * @param pre prefix
	 * @param suf suffix
	 * @return temp file absolute path string
	 */
	def getTemp(pre,suf) {
		File tempFile = File.createTempFile(pre, suf, dir)
		tempFile.deleteOnExit()
		add tempFile
		tempFile.absolutePath
	}

	/**
	 * remove a temp file
	 * @param t absolute path string of temp file
	 * @return
	 */
	def rmTemp(t) {
		def tf = new File(t)
		if (tf.exists())
			try {
				tf.delete()
			} catch(Exception e) {
				println "error deleting temp $tf, $e"
			}
		del tf
	}
}
