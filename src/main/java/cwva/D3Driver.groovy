package cwva

class D3Driver extends BaseDriver {
	
	def doGet(ttl,kind,html) {
		
		switch(kind) {
		   case "wcDrawBasic":
				 def args = toArgs("""
				   -ttl $ttl -html $html -types "vad:Drawing, vad:Watercolor" -scope basic""")
			  services.OntoToD3Driver.main(args)
			  break

		   case "drawBasic":
		   	  def args = toArgs("""
				   -ttl $ttl -html $html -types "vad:Drawing" -scope basic""")
			  services.OntoToD3Driver.main(args)
			  break

		   case "drawPhysical":
		   	  def args = toArgs("""
				   -ttl $ttl -html $html -types "vad:Drawing" -scope physical""")
			  services.OntoToD3Driver.main(args)
			  break
			  
		   case "drawNFT":
		   	  def args = toArgs("""
				   -ttl $ttl -html $html -types "vad:Drawing" -scope NFT""")
			  services.OntoToD3Driver.main(args)
			  break

		   case "wcBasic":
		   	  def args = toArgs("""
				   -ttl $ttl -html $html -types "vad:Watercolor" -scope basic""")
			  services.OntoToD3Driver.main(args)
			  break

		   case "wcPhysical":
		   	  def args = toArgs("""
				    -ttl $ttl -html $html -types "vad:Watercolor" -scope physical""")
			  services.OntoToD3Driver.main(args)
			  break

		   case "wcNFT":
		   	  def args = toArgs("""
				   -ttl $ttl -html $html -types "vad:Watercolor" -scope NFT""")
			  services.OntoToD3Driver.main(args)
			  break

		   case "all":
		   	  def args = toArgs("""
				   -ttl $ttl -html $html -types "vad:Drawing" -scope all""")
			  services.OntoToD3Driver.main(args)
			  break
			  
			  default:
			  break
		  }
	  }
  
}
