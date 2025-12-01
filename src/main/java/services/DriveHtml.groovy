package services

import util.Args

class DriveHtml {

	public static void main(String[] args){

		def map = Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, services.Driver ...
"""
			return
		}

		if (map.containsValue("print")){
			map.each{k,v->
				println "\t$k = $v"
			}
			return
		}

		def cmd = map["cmd"]
		def ttl = map["ttl"]
		def path = map["path"]
		def domain = map["domain"]
		def ns = map["ns"]

		assert cmd , "no cmd"

		def jl2h = new JsonLd2Html()

		if (cmd in (
		["browse", "work", "index"])) {
			switch (cmd) {
				case "index":
					def s = new About().get()
					println s
					break;

				case "browse":
					assert ttl , "no ttl"
					def s = new BrowseWorks().browse(ttl)
					println s
					break;

				case "work":
					assert ttl , "no ttl"
					assert path, "no path"
					assert domain, "no domain"
					assert ns, "no namespace (ns)"
					def relPath = jl2h.parsePath(path)
					def guid = jl2h.parseGuid(path)
					def s = jl2h.process(ttl,domain,relPath,ns,guid)
					println s
					break;
			}
		}
	}
}
