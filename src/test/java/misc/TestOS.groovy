package misc

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

class TestOS {

	public static void main(String[] args){
		
		def os = System.getProperty("os.name");
		System.out.println(os);
		getStatus();

	}
	
	static void getStatus() {
		String c = "top -b -n1 | head -5 ; top -b -n1 | grep java; df | grep 'sda1' ;ls -l *.log;echo errors; grep Exception *err.log | wc";
		String s = new util.Exec().exec(c);
		System.out.println(s);
	}

}
