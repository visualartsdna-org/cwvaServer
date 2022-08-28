package util

import static org.junit.jupiter.api.Assertions.*
import groovy.transform.*
import org.junit.jupiter.api.Test

class TestTmp { 

	@Test
	void test() {
		//def tmp = new TmpExpire(1000 * 2,1000 * 7)
		def tmp = new TmpExpire(1000 * 2,1000 * 7,"C:/temp/images/study/tmp")
		
		tmp.getTemp("tm1","xyz")

		sleep(1000 * 5 * 1)
		
		tmp.getTemp("tm2","xyz")

		sleep(1000 * 5 * 1)
		
		tmp.getTemp("tm3","xyz")
		
		def tf = tmp.getTemp("tt3","mno")
		sleep(1000 * 3)
		tmp.rmTemp(tf)

		sleep(1000 * 5 * 1)
		
		tmp.getTemp("tm4","xyz")

		sleep(1000 * 5 * 1)
		
		tmp.getTemp("tm5","xyz")

		sleep(1000 * 10 * 2)
		
		tmp.getTemps().each{
			println it
		}
	}

}