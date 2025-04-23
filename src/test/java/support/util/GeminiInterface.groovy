package support.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test

class GeminiInterface {

	@Test
	void test() {
		submit()
	}
	
	def submit() {
		def key = "AIzaSyC_s_lc0IGh14-n4u1UaNOTHET7EKS4Q9Y"
		def type = "watercolor"
		def image = "http://visualartsdna.org/images/RhinoStudy.jpg"
		def label = "Rhino Study"
		def weight = "300"
		def paper = "cold press"
		def ht = "12"
		def wd = "9"
		def desc = "A white rhino visits a watering hole."
//		def message =  """{
//  "contents": [{
//    "parts":[{"text": "Write a gallery description of the $type at $image"}]
//    }]
//   }"""
		def message =  """{
  "contents": [{
    "parts":[{"text": "Write a gallery description of the $type at $image 
with label '$label', on $weight pound $paper paper 
with height=$ht inches, width=$wd inches 
and description '$desc'"}]
    }]
   }"""
		  
// POST
def post = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$key").openConnection();
//def message = '{"message":"this is a message"}'
post.setRequestMethod("POST")
post.setDoOutput(true)
post.setRequestProperty("Content-Type", "application/json")
post.getOutputStream().write(message.getBytes("UTF-8"));
def postRC = post.getResponseCode();
println(postRC);
def json = ""
if (postRC.equals(200)) {
    json = post.getInputStream().getText()
}
	def col = new JsonSlurper().parseText(json)
		col.each{
			println it
		}
	}

}
