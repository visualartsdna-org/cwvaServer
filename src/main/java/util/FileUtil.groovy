package util

import groovy.io.FileType

class FileUtil {

	/**
	 * load request path images
	 * The policy is to search for the image
	 * from the parent path of the request path
	 * below the images root dir
	 * breadth-first recursive through subdirs
	 * returning the first one found.
	 * This allows for folder organization of
	 * image files.
	 * E.g., request for images/myfile.jpg
	 * may be found at /content/images/stuff/myfile.jpg
	 * @param dir the base content folder containing images
	 * @param fn the request path for the image
	 * @return
	 */
	static def loadImage0(dir,fn) {
		def fl=[]
		def file = fn.replaceAll("%20"," ")
		def p = new File(file).getParent() ?: ""
		def f = new File(file).getName()
		def path = dir + p
		if (new File(path).isDirectory()) {
			new File(path).eachFileRecurse(FileType.FILES) {
				if (it.name == f)
					fl += it
			}
		}
		assert !fl.isEmpty(), "$f not found"
		//assert !fl.isEmpty(), "$f not found from path $path"
		fl.first()
	}

	// TODO: refactor and reorg
	// gcp bucket enabled
	static def loadImage(dir,fn) {
		def fl=[]
		def file = fn.replaceAll("%20"," ")
		def f1 = new File(file)
		def p = f1.getParent() ?: ""
		def f = f1.getName()
		def path = dir// + f
		if (new File(path).isDirectory()) {
			new File(path).eachFileRecurse(FileType.FILES) {
				if (it.name == f)
					fl += it
			}
		}
		
		//if (!fl.isEmpty()) println "found file: ${fl.first()}" // debug

		if (fl.isEmpty()) {
			def src = "images"
			try {
				def url = Gcp.gcpLs(src,f)
				//if (url) println "found url: ${url}" // debug
				if (url) Gcp.gcpCp(url,dir)
			} catch (RuntimeException re) {
				System.err.println ("$f not found, $re")
				throw new FileNotFoundException("$f not found") 
			}
			def f2 = new File("$dir/$f")
			
			//if (f2.exists()) println "url to file: ${f2}" // debug
			
			if (!f2.exists()) {
				throw new FileNotFoundException("$f not found") 
			}
			fl= [f2]
		}
		fl.first()
	}
	
	// TODO: refactor and reorg
	// gcp bucket enabled
	static def deleteImage(dir,fn) {
		def fl=[]
		def file = fn.replaceAll("%20"," ")
		def f1 = new File(file)
		def f = f1.getName()
		def path = dir// + f
		if (new File(path).isDirectory()) {
			new File(path).eachFileRecurse(FileType.FILES) {
				if (it.name == f)
					it.delete()
			}
		}
		
	}
	
	// TODO: refactor and reorg
	// gcp bucket enabled
	static def purgeImages(dir) {
		def fl=[]
		def path = dir
		if (new File(path).isDirectory()) {
			new File(path).eachFileRecurse(FileType.FILES) {
				if (it.name =~ /.*\.JPG|.*\.jpg/)
					it.delete()
			}
		}
		
	}
}
