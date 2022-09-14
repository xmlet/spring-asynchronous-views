package pt.isel.genius

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GeniusApplication

fun main(args: Array<String>) {
	/**
	 * Limit request handler threads to a single worker thread.
	 * Thus, handlers should be non-blocking to allow multiple requests concurrently.
	 */
	System.setProperty("reactor.netty.ioWorkerCount", "1");
	runApplication<GeniusApplication>(*args)
}
