package util

import static org.junit.jupiter.api.Assertions.*
import static groovy.io.FileType.FILES

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test

/**
 * Creates and validates a token 
 * for a secure web request
 * (TODO: Need https for man in the middle threat)
 * @author ricks
 *
 */
class Token { 
	static def verbose=true
	Random rand = new Random()
	
		def phrase = "I wanna hold your hand"
		def hc = BigInteger.valueOf(phrase.hashCode())
		def tolerance= 30000 // milliseconds

	def getTimeToken() {
		getToken(new Date().getTime())
	}
	
	def getToken(long time) {
		getToken(BigInteger.valueOf(time))
	}
	
	def getToken(BigInteger time) {
		time.multiply(hc)
	}
	
	def validate(String token) {
		validate(new BigInteger(token))
	}
		
	def validate(BigInteger token) {
		
		def time = new Date().getTime()
		BigInteger sec = token.divide(hc)
		def timeMinus = BigInteger.valueOf(time - tolerance)
		def timePlus = BigInteger.valueOf(time + tolerance)
		if (verbose) println "dif=${BigInteger.valueOf(time).subtract(sec)}"
		sec.compareTo(timeMinus) > 0 && sec.compareTo(timePlus) < 0 
	}
	
	/**
	 * Run a cwva server
	 * @param args
	 */
	public static void main(String[] args){

		def map = Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, 
util.Token -url localhost:8082 -cmd abc
"""
			return
		}
		
		def url = map["url"]
		def cmd = map["cmd"]
		def parm = map["parm"]
		
		switch (cmd) {
			case "delete":
			println "delete"
			fileList("/temp/images", /.*\.JPG|.*\.jpg/)
			.each{
				println it.name
		
				def token = new Token().getTimeToken()
				def s = "http://$url/cmd?token=$token&cmd=delete&parm=${it.name}"
				println new JsonSlurper().parse(s.toURL())
				}
	
			
			break;
			
			case "restart":
			case "purge":
			default:
			
				def token = new Token().getTimeToken()
				def s = "http://$url/cmd?token=$token&cmd=$cmd&parm=$parm"
				println s
				println new JsonSlurper().parse(s.toURL())
	
			break;
			
		}
	}
	
	static def fileList(dirName,filter) {
		def dir = new File(dirName);
		def files = [];
		dir.traverse(type: FILES, maxDepth: 0) { files.add(it) }
		
		files.findAll{
			it.name =~ filter
		}
	}
	
	@Test
	void testDir() {
		
		fileList("/temp/images", /.*\.JPG|.*\.jpg/)
		.each{println it.name}
	}

	@Test
	void testRequestRestart() {
		
		def token = getTimeToken()
		
		def s = "http://localhost:8082/cmd?token=$token&cmd=restart"
		println s
		def map = new JsonSlurper().parse(s.toURL())
		println map
	}

	@Test
	void testRequestDelete() {
		
		def token = getTimeToken()
		
		def s = "http://localhost:8082/cmd?token=$token&cmd=delete&parm=deckView3.jpg"
		println s
		def map = new JsonSlurper().parse(s.toURL())
		println map
	}

	@Test
	void testRequest() {
		
		def token = getTimeToken()
		
		def s = "http://localhost:8082/cmd?token=$token&cmd=abc"
		println s
		def map = new JsonSlurper().parse(s.toURL())
		println map
	}


	@Test
	void testRequest0() {
		
		def token = getTimeToken()
		
		def s = "http://localhost:8082/cmd?token=$token&cmd=abc"
		println s
		def conn = new URL(s).openConnection()
		conn.requestMethod = 'GET'
		assert conn.responseCode == 200
	}

		
	@Test
	void testString() {
		def max=10
		def tot=0
		for (int i=0;i<max;i++) {
			def s = "" + getTimeToken()
			println s
			def res = validate ( s )
			tot+=(res?1:0)
		}
		println "trues=$tot / $max"
	}
	
	@Test
	void test() {
		def max=3
		def tot=0
		for (int i=0;i<max;i++) {
			def res = validate ( getTimeToken() )
			tot+=(res?1:0)
		}
		println "trues=$tot / $max"
	}
	
	@Test
	void testVariance() {
		def factor=3
		def max=100
		def tot=0
		for (int i=0;i<max;i++) {
			def time= new Date().getTime()
			def ran=rand.nextInt(factor * tolerance)
			//def time2 = BigInteger.valueOf(time + ran)
			def time2 = time + ran
			def res = validate ( getToken(time2) )
			tot+=(res?1:0)
		}
		println "trues=$tot / $max"
	}
	
}
