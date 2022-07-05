package function

class CheckState {
	
	static def url="http://localhost:8080/fxai/state"
	
	static def check() {
		//return 1
		try {
			return url.toURL().text as int //  hmmmmm
		} catch (java.net.ConnectException e) {
			return 0	// website may be down, pause trade entry
		}
		return 0	// shouldn't reach here, pause trade entry
	}
}
