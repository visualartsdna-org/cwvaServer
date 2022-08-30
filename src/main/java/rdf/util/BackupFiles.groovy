package rdf.util

import static org.junit.jupiter.api.Assertions.*
import org.apache.commons.io.*
import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

class BackupFiles {

	@Test
	void test() {
		//backup("C:/temp/junk/tags.ttl","C:/temp/junk")
		backup("C:/temp/junk/tags.ttl")
	}

	static def backup(fn) {
		def dn = FilenameUtils.getFullPathNoEndSeparator(fn)
		backup(fn,dn)
	}
		
	// for text files
	/**
	 * for text or binary files
	 * file is renamed to filename with time component to the second
	 * @param fn source
	 * @param dn target folder
	 * @return
	 */
	static void backup(fn,dn) {
		def file = new File(fn)
		def folder = new File(dn)
		assert file.exists(), "File for backup, $fn, does not exist"
		assert folder.exists(), "Folder for backup, $dn, does not exist"
		
		def baseFn = FilenameUtils.getBaseName(fn)
		def extFn = FilenameUtils.getExtension(fn)
		def time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
		def bfn = "$folder/${baseFn}_$time.${extFn}.bkp"
		Path source = Paths.get(fn)
		Path target = Paths.get(bfn)
		Files.copy(source, target)
	}

}
