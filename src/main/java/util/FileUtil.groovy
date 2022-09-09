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
	static def loadImage(dir,fn) {
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


}
