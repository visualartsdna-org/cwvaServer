package support.util

import static org.junit.jupiter.api.Assertions.*

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test

class GeminiInterface {

	@Test
	void test() {
		def col = submit([
type:"vad:Painting",
kind:"Gallery Description",
media:"watercolor",
image:"http://visualartsdna.org/images/RhinoStudy.jpg",
label:"Rhino Study",
hasPaperWeight:"300",
hasPaperFinish:"cold press",
height:"12",
width:"9",
description:"A white rhino visits a watering hole."
])
		println col
		println col.candidates[0].content.parts[0].text
		
	}
	
	def submit(m) {
		def key = util.Secrets.get("geminiKey")
		def gemini_image_url_working = false
		def message =  gemini_image_url_working ?
		"""
{
  "contents": [
    {
      "parts": [
        {
          "image_url": {
            "url": "m.image",
            "mime_type": "image/jpeg"
          }
        },
        {
${getTemplate(m)}
        }
      ]
    }
  ]
}
"""	:
		"""{
  "contents": [
			{
			"parts": [
		{
          "inline_data": {
            "mime_type": "image/jpeg",
            "data": "${new URL(m.image).getBytes().encodeBase64()}"
          }
			},

			{
${getTemplate(m)}
		}
		]
    }]
   }"""
		  
// POST
    def version = cwva.Server.getInstance().cfg.aiVersion.gemini ?: "gemini-2.5-flash"
	def post = new URL("https://generativelanguage.googleapis.com/v1beta/models/$version:generateContent?key=$key").openConnection();
	//def message = '{"message":"this is a message"}'
	post.setRequestMethod("POST")
	post.setDoOutput(true)
	post.setRequestProperty("Content-Type", "application/json")
	post.getOutputStream().write(message.getBytes("UTF-8"));
	def postRC = post.getResponseCode();
	println(postRC);
	def json = """
{
	"candidates": [
		{
		"content": {"parts": [
				{"text": "AI Ask produced an error, could be a token limit issue.  Try again later"
				}
			]
			}
		}
	]
}
"""
	if (postRC.equals(200)) {
	    json = post.getInputStream().getText()
	}
	def col = new JsonSlurper().parseText(json)
	col
	}
	
	def getTemplate(m) {
		def s = ""
		if (m.type == "vad:ComputerArt") {
			s = """
"text": "Write a $m.kind of the $m.media as a computer generated work of art
with label '$m.label',
with height=$m.height pixels, width=$m.width pixels 
and description '$m.description'"
"""
				
		}
		else if (m.type == "vad:Painting") {
		s = """
"text": "Write a $m.kind of the $m.media 
with label '$m.label', on $m.hasPaperWeight pound $m.hasPaperFinish paper 
with height=$m.height inches, width=$m.width inches 
and description '$m.description'"
"""
			
		}
	}

}
