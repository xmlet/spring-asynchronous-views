package pt.isel.disco.kotlinx

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.reactivestreams.Publisher
import pt.isel.disco.AppendableSink
import pt.isel.disco.model.AppleMusicArtist
import pt.isel.disco.model.MusicBrainz
import pt.isel.disco.model.SpotifyArtist
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

/**
 * KotlinX.html view in suspend function trying to await on CompletableFutures.
 * Illegal due to crossinline lambdas used in builders that loose the coroutine context.
 */
suspend fun kotlinXArtistCoroutine(cfMusicBrainz: Mono<MusicBrainz>): Publisher<String> {
    return AppendableSink().apply {
            // val artist = cfMusicBrainz.toFuture().await() // OK but delays start emitting HTML
                appendHTML()
                .html {
                    body {
                        div {
                            h3 { +"Artist information" }
                            // val artist = cfMusicBrainz.toFuture().await() // ERROR Suspension functions can be called only within coroutine body
                            // p { +"${artist.genre} ..." }
                            // ...
                        }
                    }
                }
            close()
        }
        .asFlux()
}

fun kotlinXArtistReactive(
    startTime: Long,
    artisName: String,
    musicBrainz: CompletableFuture<MusicBrainz>,
    spotify: CompletableFuture<SpotifyArtist>
): AppendableSink {
    return AppendableSink().apply {
        appendHTML()
            .html {
                body { // body
                    div {
                        h3 { +"$artisName" }
                        hr()
                        h3 { +"MusicBrainz info:" }
                        ul {
                            musicBrainz
                                .thenAccept {
                                    li { +"Founded: ${it.year}" }
                                    li { +"From: ${it.from}" }
                                    li { +"Genres: ${it.genres}" }
                                    hr()
                                    b { +"Spotify popular tracks: " }
                                    span {
                                        spotify
                                            .thenAccept {
                                                +it.popularSongs.joinToString(", ")
                                                hr()
                                                footer {
                                                    small { +"${System.currentTimeMillis() - startTime} ms (response handling time)" }
                                                }
                                                close()
                                            }
                                    }
                                }
                        }
                    }
                }
            }
    }
}

fun kotlinXArtistBlocking(
    startTime: Long,
    artisName: String,
    cfMusicBrainz: CompletableFuture<MusicBrainz>,
    cfSpotify: CompletableFuture<SpotifyArtist>,
    cfApple: CompletableFuture<AppleMusicArtist>,
): Publisher<String> {
    return AppendableSink().apply { appendHTML()
                .html {
                    body {
                        h3 { +"$artisName" }
                        h3 { +"MusicBrainz info:" }
                        ul {
                            val musicBrainz = cfMusicBrainz.join()
                            li { +"Founded: ${musicBrainz.year}" }
                            li { +"From: ${musicBrainz.from}" }
                            li { +"Genre: ${musicBrainz.genres}" }
                        }
                        p {
                            b { +"Spotify popular tracks:" }
                            +cfSpotify.join().popularSongs.joinToString(", ")
                        }
                        /*
                        hr {  }
                        b { +"Apple Music top songs:" }
                        span {
                            +cfApple.join().topSongs.joinToString(",")
                        }
                        */
                    }
                }
            this.close()
        }
        .asFlux()
}
