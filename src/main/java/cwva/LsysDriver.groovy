package cwva

class LsysDriver extends BaseDriver {
	
	def doGet(ttl,kind,img) {
		
		switch(kind) {
		   case "wcDrawBasic":
		   	  def args = toArgs("""
				   -inFile $ttl -types "vad:Drawing, vad:Watercolor" -scope basic -imgFile $img""")
			  drawing.Main.main(args)
			  break

		   case "drawBasic":
		   	  def args = toArgs("""
				   -inFile $ttl -types "vad:Drawing" -scope basic -imgFile $img""")
			  drawing.Main.main(args)
			  break

		   case "drawPhysical":
		   	  def args = toArgs("""
				   -inFile $ttl -types "vad:Drawing" -scope physical -imgFile $img""")
			  drawing.Main.main(args)
			  break
		   case "drawNFT":
		   	  def args = toArgs("""
				   -inFile $ttl -types "vad:Drawing" -scope NFT -imgFile $img""")
			  drawing.Main.main(args)
			  break

		   case "wcBasic":
		   	  def args = toArgs("""
				   -inFile $ttl -types "vad:Watercolor" -scope basic -imgFile $img""")
			  drawing.Main.main(args)
			  break

		   case "wcPhysical":
		   	  def args = toArgs("""
				    -inFile $ttl -types "vad:Watercolor" -scope physical -imgFile $img""")
			  drawing.Main.main(args)
			  break

		   case "wcNFT":
		   	  def args = toArgs("""
				   -inFile $ttl -types "vad:Watercolor" -scope NFT -imgFile $img""")
			  drawing.Main.main(args)
			  break

		   case "all":
		   	  def args = toArgs("""
				   -inFile $ttl -scope inst -imgFile $img""")
			  drawing.Main.main(args)
			  break

		   case "onto":
		   	  def args = toArgs("""
				   -inFile ttl/cwva.ttl -scope onto -imgFile $img -path "http://visualartsdna.org/data" -base owl:Thing""")
			  drawing.Main.main(args)
			  break

			default:
			break
		}
	}
	
}
