package pt.isel.genius

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GeniusApplication

fun main(args: Array<String>) {
	/**
	 * Limit request handler threads to a single worker thread.
	 * Thus, handlers should be non-blocking to allow multiple requests concurrently.
	 * NOTICE minimum default to 4.
	 */
	System.setProperty("reactor.netty.ioWorkerCount", "1"); // minimum default to 4
	runApplication<GeniusApplication>(*args)
}
