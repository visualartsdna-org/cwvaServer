package util
import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class Guid {
	
	def get() {
		
		UUID.randomUUID()
	}
	
	@Test
	void test() {
		println new Guid().get()
	}

}
