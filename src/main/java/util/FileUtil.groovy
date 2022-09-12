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

		if (fl.isEmpty()) {
			def src = "images"
			try {
				def url = getLs(src,f,dir)
				if (url) Gcp.gcpCp(url,dir)
			} catch (RuntimeException re) {
				System.err.println ("$re")
				assert false, "$f not found"
			}
			def f2 = new File("$dir/$f")
			if (!f2.exists()) {
				assert false, "$f not found"
			}
			fl= [f2]
		}
		fl.first()
	}
	
	static def getLs(src,file,tgt) {
//		def tgt = "C:/temp/images"
//		def src = "images"
//		def file = "IMG_1944.jpg"
		def a = Gcp.gcpLs(src,file)
		println "${a[0]}"
		println "${a[1]}"
		if (a[0].isEmpty()
			&& a[1].contains("One or more URLs matched no objects"))
			return null
		a[0]
	}
}
