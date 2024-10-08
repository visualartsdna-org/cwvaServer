package util

class Exec {
	
	def execVerbose(command) {
		Process process = command.execute()
		def out = new StringBuffer()
		def err = new StringBuffer()
		process.consumeProcessOutput( out, err )
		process.waitFor()
		if( out.size() > 0 ) println out
		if( err.size() > 0 ) println err
		out
	}

	def exec(command) {
		Process process = command.execute()
		def out = new StringBuffer()
		def err = new StringBuffer()
		process.consumeProcessOutput( out, err )
		process.waitFor()
		//if( out.size() > 0 ) println out
		if( err.size() > 0 ) System.err.println err
		out
	}

	def execQuiet(command) {
		Process process = command.execute()
		def out = new StringBuffer()
		def err = new StringBuffer()
		process.consumeProcessOutput( out, err )
		process.waitFor()
		//if( out.size() > 0 ) println out
		//if( err.size() > 0 ) System.err.println err
		[out,err]
	}


}
