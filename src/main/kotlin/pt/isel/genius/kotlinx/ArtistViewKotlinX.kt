package pt.isel.genius.kotlinx

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.reactivestreams.Publisher
import pt.isel.genius.model.AllMusicArtist
import pt.isel.genius.model.AppleMusicArtist
import pt.isel.genius.model.SpotifyArtist
import reactor.core.publisher.Flux
import java.util.concurrent.CompletableFuture

/**
 * KotlinX.html view in suspend function trying to await on CompletableFutures.
 * Illegal due to crossinline lambdas used in builders that loose the coroutine context.
 */
suspend fun kotlinXArtistCoroutine(cfAllMusicArtist: CompletableFuture<AllMusicArtist>): Publisher<String> {
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

fun kotlinXArtistReactive(
    startTime: Long,
    artisName: String,
    allMusic: Flux<AllMusicArtist>,
    spotify: Flux<SpotifyArtist>,
    apple: Flux<AppleMusicArtist>,
): Publisher<String> {
    return AppendableSink().let { sink ->
        sink.asFlux().also {
            sink
                .appendHTML()
                .html {
                    body {
                        div {
                            h3 { +"$artisName" }
                            hr {  }
                            h3 { +"AllMusic info:" }
                            ul { allMusic
                                .doOnNext {
                                    li { +"Founded: ${it.year}" }
                                    li { +"From: ${it.from}" }
                                    li { +"Founded: ${it.genre}" }
                                }
                                .doOnComplete {
                                    hr {  }
                                    b { +"Spotify popular tracks:" }
                                    ul { spotify
                                        .doOnNext {
                                            it.popularSongs.forEach { song ->
                                                span { +"$song, " }
                                            }
                                        }
                                        .doOnComplete {
                                            hr {  }
                                            b { +"Apple Music top songs:" }
                                            ul { apple
                                                .doOnNext {
                                                    it.topSongs.forEach { song ->
                                                        span { +"$song, " }
                                                    }
                                                }
                                                .doOnComplete {
                                                    sink.close()
                                                }
                                                .subscribe()
                                            }

                                        }
                                        .subscribe()
                                    }
                                }
                                .subscribe()
                            }
                            hr {  }
                            footer {
                                small { +"${System.currentTimeMillis() - startTime} ms (response handling time)" }
                            }
                        }
                    }
                }
        }
    }
}
