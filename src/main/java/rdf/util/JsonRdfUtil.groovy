package rdf.util

class JsonRdfUtil {
	/**
	 *  this utility method converts
	 *  JSON to TTL
	 * @param m
	 * @param sb
	 * @param prefix
	 * @return
	 */
	static def jsonToTtl(m, sb, prefix){
		
		// handle the case where m is a Map
		if (m instanceof Map){
			sb.append "["
			m.each{ k,v->
				if (v == null || v == "") return	// no value to consider
//				if (k=="mpmProductIds"){
//					println "here"
//				}
				sb.append  "\t${k.contains(":")? "" : prefix}$k "		// the attribute/key becomes the property name
				if (v instanceof Collection && v.isEmpty())
					sb.append"""[] ; \n"""		// an empty map
				if (v instanceof List
					|| v instanceof Map) {
					jsonToTtl(v, sb, prefix)	// recurse to process the collection found
					sb.append"""; \n"""
				}
				else {
					setLiteralData(sb,v)		// value is literal data
					sb.append ";"
				}
			}
			sb.append  "]\n"
		}
		else if (m instanceof List) {	// handle the case where m is a List
			def i=0;
			m.each{
				if (i++>0) sb.append ","
				if (it==null) sb.append"""[]""" 	// handle empty list
				else jsonToTtl(it, sb, prefix)	// recurse to process the collection found
				if (i==m.size()) sb.append ";"
			}
		}
		else { // m is a literal value 
			setLiteralData(sb,m)		// value is literal data
		}
	}
	
	/**
	 *  match and append appropriate literal data
	 * @param sb
	 * @param v
	 * @return
	 */
	static def setLiteralData(sb,v) {
		if (v =~ /^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:\.\d{1,9})?(?:Z|[+-][01]\d:[0-5]\d)$/)
			sb.append  """"$v"^^xs:dateTime ;\n"""
				else if (v =~ /^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29) (?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:\.\d{1,9})?(?:Z|[+-][01]\d:[0-5]\d)?$/) {
					def t = v.replaceAll(" ", "T")
					sb.append  """"$t"^^xs:dateTime \n"""
		}
		else if (v =~ /^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)$/)
			sb.append  """"$v"^^xs:date \n"""
		else if (v instanceof BigDecimal)
			sb.append  """"$v"^^xs:double \n"""
		else if (v instanceof Double)
			sb.append  """"$v"^^xs:double \n"""
		else if (v instanceof Float)
			sb.append  """"$v"^^xs:float \n"""
		else if (v instanceof BigInteger)
			sb.append  """"$v"^^xs:int \n"""
		else if (v instanceof Integer)
			sb.append  """"$v"^^xs:int \n"""
		else if (v instanceof Long)
			sb.append  """"$v"^^xs:long \n"""
		else if (v instanceof Boolean)
			sb.append  """"$v"^^xs:boolean;\n""" // TODO: should this have ';'
		else if (v == null) {
			sb.append  """\"\"\"\"\"\"^^xs:string \n"""  // unfortunate "null" representation
		}
		else if (v instanceof String) {
			def v0 = org.apache.commons.lang.StringEscapeUtils.unescapeJava(v)
			def v1 = v0.replaceAll(/"["]+/,'"')
			if (v1.endsWith('"')
			&& !v1.endsWith('\\"')) { // escape a quote at the end of the string for TTL
			def v2 = v1.replaceAll(/"$/, '\\\\"')
				sb.append  """\"\"\"$v2\"\"\"^^xs:string \n"""
			}
			else
				sb.append  """\"\"\"${fixDanglingChars(v1)}\"\"\"^^xs:string \n"""
			}
//		else sb.append("""$v""")  // don't know a type?
	}

	/**
	 *  a dangling escape char is removed
	 * @param s
	 * @return s
	 */
	static def fixDanglingChars(s) {
		if (s.endsWith('\\')) {
			def n = s.lastIndexOf('\\')
			def s2 = s.substring(0,n)
			return s2
		}
		return s
	}
}
