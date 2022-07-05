package util

import static org.junit.Assert.*;
import groovy.json.JsonBuilder
import java.util.Map;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * The Class Args for command line argument processing
 */
class Args {
	
	/**
	 * Gets the.
	 *
	 * @param sa the sa
	 * @return the map
	 */
	static Map get(sa){
		def map=[:]
		def key = null
		def cnt = 0
		sa.each{
			if (it.startsWith("-")) {
				key = it.substring(1)
				cnt++
			}
			else if (key==null){
				map[cnt++] = it
			}
			else {
				map[key] = it
				key=null
			}
				
		}
		return map
	}
	
	/**
	 * Gets the bindings.
	 *
	 * @param map the map
	 * @param list the list
	 * @return the bindings
	 */
	static Map getBindings(map,list){
		
		def bindMap = [:]
			list.each{
			if (map.containsKey(it))
				bindMap[it] = map[it]
		}
		return bindMap
	}

	/**
	 * Gets the bindings as json.
	 *
	 * @param map the map
	 * @param list the list
	 * @return the bindings as json
	 */
	static String getBindingsAsJson(map,list){
		
		def bindMap = getBindings(map,list)
		
		JsonBuilder jb = new JsonBuilder(bindMap)
		return jb.toString()
	}
	
	/**
	 * Count unkeyed.
	 *
	 * @param map the map
	 * @return the int
	 */
	static int countUnkeyed(map){
		int n=0
		map.each {k,v->
			if (k =~/^[0-9]+$/)
				n++
		}
		return n
	}

	/**
	 * To console.
	 *
	 * @param map the map
	 * @return the string
	 */
	static String toConsole(map){
		for (def i=0;map[i]!= null;i++) {
			println "$i = ${map[i]}"
		}

	}

	/**
	 * Test.
	 */
	@Test
	public void test() {
		String[] sa = [
			"one",
			"two",
			"three"
		]
		main(sa)
	}

	/**
	 * Test 2.
	 */
	@Test
	public void test2() {
		String[] sa = [
			"-a",
			"one",
			"-b",
			"two",
			"-c",
			"three"
		]
		main(sa)
	}

	/**
	 * Test 3.
	 */
	@Test
	public void test3() {
		String[] sa = [
			"-a",
			"one",
			"one and a half",
			"two",
			"-c",
			"three"
		]
		main(sa)
	}

	// ReportsML/$1 ./Report.xlsx http://52.27.252.34:8000 username password genome-dev "$2"
	
	/**
	 * Test 4.
	 */
	@Test
	public void test4() {
		String[] sa = [
			"ReportsML/Analytics",
			"./Report.xlsx",
			"http://52.27.252.34:8000",
			"username",
			"password",
			"genome-dev",
			"series"
		]
		main(sa)
	}

	/**
	 * Test 4 a.
	 */
	@Test
	public void test4a() {
		String[] sa = [
			"ReportsML/Analytics",
			"./Report.xlsx",
			"http://52.27.252.34:8000",
			"username",
			"password",
			"genome-dev",
			"-Series",
			"Grey's Anatomy",
			"-Season",
			"2",
			"-Episode",
			"25",
		]
		main(sa)
	}

	/**
	 * Test 4 b.
	 */
	@Test
	public void test4b() {
		String[] sa = [
			"ReportsML/Analytics",
			"./Report.xlsx",
			"http://52.27.252.34:8000",
			"username",
			"password",
			"genome-dev",
			"-Series",
			"Grey's Anatomy",
		]
		main(sa)
	}

	// ReportsML/$1 ./Report.xlsx http://52.25.64.8:8080/mwd/sparql "$2"
	
	/**
	 * Test 5.
	 */
	@Test
	public void test5() {
		String[] sa = [
			"ReportsML/Analytics",
			"./Report.xlsx",
			"http://52.25.64.8:8080/mwd/sparql",
			"series"
		]
		main(sa)
	}

	/**
	 * Test 5 a.
	 */
	@Test
	public void test5a() {
		String[] sa = [
			"ReportsML/Analytics",
			"./Report.xlsx",
			"http://52.25.64.8:8080/mwd/sparql",
			"-Series",
			"Grey's Anatomy",
			"-Season",
			"2",
			"-Episode",
			"25",
		]
		main(sa)
	}

	// a test of Args
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	static void main(String[] args) {
		def map = Args.get(args)
		for (def i=0;map[i]!= null;i++) {
			println "$i = ${map[i]}"
		}
		def n = Args.countUnkeyed(map)
		println n
		
		def bindings = Args.getBindingsAsJson(map, ["Series","Season","Episode"])
		println bindings
		
		def args2 = Args.getBindings(map, (0..3))
		println args2
	}
}
