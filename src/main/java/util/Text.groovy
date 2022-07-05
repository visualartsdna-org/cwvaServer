package util

import static org.junit.jupiter.api.Assertions.*

import java.text.SimpleDateFormat
import org.junit.jupiter.api.Test

class Text {

	@Test
	public void test() {
		def cc= camelCase("Enhanced mind map")
		println cc
		println unCamelCase(cc)
	}

	static def camelCase(s) {
		s.replaceAll( /( )([A-Za-z0-9])/, {
			it[2].toUpperCase() 
			} )
	}
	
	def unCamelCase(String s) {
		def sb = new StringBuilder()
		int i=0
		for(char c : s.toCharArray())
			{
				if (i++==0) sb.append(c.toUpperCase())
				else if(Character.isUpperCase(c))
						sb.append(" ${c}")
				else sb.append c
			}
		sb.toString()
	}
	
	def extractRootFileName(s) {
		(s =~ /.*[\\\/](.+)\.mm$/)[0][1]
	}
	
	def getFilenamePath(fn) {
		new File(fn).getParent()
	}
	def getFilenameNoExt(fn) {
		new File(fn).name.replaceFirst(/[.][^.]+$/, "")
	}
	def getYear(){
		return new SimpleDateFormat( "yyyy").format(new Date())
	}
	def getDate(){
		return new SimpleDateFormat( "yyyy-MM-dd").format(new Date())
	}
	def getDateTime(){
		getDateTime(new Date())
	}
	def getDateTime(date){
		return new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ss").format(date)
	}

	
}
