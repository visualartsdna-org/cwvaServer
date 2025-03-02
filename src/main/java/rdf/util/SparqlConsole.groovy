package rdf.util

import rdf.JenaUtilities

class SparqlConsole {
	
	/**
	 * Run a cwva server
	 * @param args
	 */
	public static void main(String[] args){

		def map = util.Args.get(args)
		if (map.isEmpty()) {
			println """
Usage, 
rdf.util.SparqlConsole -path {file-path}
"""
			return
		}
		
		def path = map["path"]
		assert path, "no path argument"
		def data = new JenaUtilities().loadFiles(path)
		new rdf.tools.SparqlConsole().show(data)

	}
}
