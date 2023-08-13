package rdf.util.parser

import java.time.Instant
import java.time.Instant
import java.util.concurrent.TimeUnit

class Util {
	
	static Instant getInstantFromMicros(long microsSinceEpoch) {
		return Instant.ofEpochSecond(
				TimeUnit.MICROSECONDS.toSeconds(microsSinceEpoch),
				TimeUnit.MICROSECONDS.toNanos(
						Math.floorMod(microsSinceEpoch, TimeUnit.SECONDS.toMicros(1))
				)
		);
	}
	
	static Instant now() {
		Instant.now()
	}
	
}
