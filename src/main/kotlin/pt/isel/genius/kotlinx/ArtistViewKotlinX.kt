package pt.isel.genius.kotlinx

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.reactivestreams.Publisher
import pt.isel.genius.model.AllMusicArtist
import java.lang.StringBuilder
import java.util.concurrent.CompletableFuture

suspend fun coKotlinXArtistReactive(cfAllMusicArtist: CompletableFuture<AllMusicArtist>): Publisher<String> {
    return AppendableSink().use { sink ->
        sink.asFlux().also {
            // val artist = cfAllMusicArtist.await() // OK but delays start emitting HTML
            sink
                .appendHTML()
                .html {
                    body {
                        div {
                            h3 { +"Artist information" }
                            // val artist = cfAllMusicArtist.await() // ERROR Suspension functions can be called only within coroutine body
                            // p { +"${artist.genre} ..." }
                            // ...
                        }
                    }
                }
            sink.close()
        }
    }
}
