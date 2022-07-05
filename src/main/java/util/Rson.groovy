package util

import groovy.json.JsonSlurper

/**
 * 
 Utility to make JSON files more usable as configuration files.
 Includes comments and imports.  By convention these files have 
 extension ".rson".
 
 Rson syntax: 
 # comment, a comment starts with '#' as first char of a line, 
 remainder of the line is ignored.
 @import test2.rson, an import starts with '@' as first char of a line
 immediately followed by 'import' (lowercase), space, then the name
 of the file to import as the remainder of the line.
 E.g.,
 ...    },
 # comment
 @import test2.rson
 "network": { ...
 
Import requirement: Include an RSON import file of default 
configuration data within another RSON configuration file.

Import rules for simple top-level combining of imported json:

Assume a base json file and one import json file.

1. An import can occur anywhere inside a base file containing a top-level map or a list

2. If base is a list, import of map or list is added to the base list as last element

3. If base is a map, import of a map is included in the base map, 
where top-level keys are the same for base and import, the base key values win.

4. If base is a map, import of a list is included as "rson:import":[...].
This situation is probably not desirable.

5. Nested imports are supported.  Multiple imports per file are not supported.
 */
class Rson {

	// load a rson file
	static def load(fs) {
		def sb = new StringBuffer()
		def icol

		new File(fs).eachLine {
			if (it =~ /^[#]/) {
				return
			}
			if (it =~ /^[@]import [A-Za-z0-9._:\\\\/]+$/) {
				def fs2 = it.substring("@import ".length())
				def ic = Rson.load(fs2)
				if (icol) throw new RuntimeException(
					"Unsupported multiple imports per file: $fs2")
				else icol = ic
				return
			}
			sb.append("$it\n")
		}
		def col = new JsonSlurper().parseText(""+sb)
		if (col instanceof List) {
			if (icol) col += icol
		}
		else if (col instanceof Map) {
			if (icol instanceof List) {
				col["rson:import"] = icol
			} else {
				icol.each { k,v->
					if (!col.containsKey(k)) {
						col[k]= v
					}
				}
			}
		}
		col
	}

}
