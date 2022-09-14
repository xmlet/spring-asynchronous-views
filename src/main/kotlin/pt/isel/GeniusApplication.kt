package pt.isel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GeniusApplication

fun main(args: Array<String>) {
	runApplication<GeniusApplication>(*args)
}
