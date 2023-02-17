package pt.isel.genius.htmlflow

import htmlflow.HtmlFlow
import htmlflow.HtmlPage
import org.reactivestreams.Publisher
import pt.isel.genius.model.AllMusicArtist
import pt.isel.genius.model.AppleMusicArtist
import pt.isel.genius.model.SpotifyArtist
import reactor.core.publisher.Flux
import java.lang.System.currentTimeMillis
import java.util.concurrent.CompletableFuture

/**
 * This web template produces well-formed HTML, and it is non-blocking.
 * Yet, the template suffers from the "pyramid of doom" due to nested chain
 * of continuation in doOnComplete() event.
 */
fun htmlFlowArtistDoc(
    startTime: Long,
    artisName: String,
    allMusic: Flux<AllMusicArtist>,
    spotify: Flux<SpotifyArtist>,
    apple: Flux<AppleMusicArtist>
) : Publisher<String> {
    return AppendableSink().let { sink ->
        sink.asFLux().also {
            HtmlFlow
                .doc(sink)
                .html()
                .body()
                .div()
                .h3().text(artisName).`__`()
                .hr().`__`()
                .h3().text("AllMusic info:").`__`()
                .ul().of { ul -> allMusic
                    .doOnNext { ul
                        .li().text("Founded: ${it.year}").`__`()
                        .li().text("From: ${it.from}").`__`()
                        .li().text("Genre: ${it.genre}").`__`()
                    }
                    .doOnComplete { ul
                        .`__`() // ul
                        .hr().`__`()
                        .b().text("Spotify popular tracks:").`__`()
                        .of { div -> spotify
                            .doOnNext { song ->
                                song.popularSongs.forEach {
                                    div.span().text("$it, ").`__`()
                                }
                            }
                            .doOnComplete {
                                div
                                    .hr().`__`()
                                    .b().text("Apple Music top songs:").`__`()
                                apple
                                    .doOnNext { song ->
                                        song.topSongs.forEach {
                                             div.span().text("$it, ").`__`()
                                        }
                                    }
                                    .doOnComplete { div
                                        .`__`() // div
                                        .hr().`__`()
                                        .footer()
                                            .small()
                                                .text("${currentTimeMillis() - startTime} ms (response handling time)")
                                            .`__`() // small
                                        .`__`() // footer
                                        .`__`() // body
                                        .`__`() // html
                                        sink.close()
                                    }
                                    .subscribe()
                            }
                            .subscribe()
                        }
                    }
                    .subscribe()
                }
        }
    }
}

val htmlFlowArtistAsyncView = HtmlFlow.viewAsync(::htmlFlowArtistAsyncViewTemplate)

fun htmlFlowArtistAsyncViewTemplate(view: HtmlPage)
{
    view
        .html()
            .body()
                .div()
                    .h3().dynamic<ArtistAsyncModel> {
                        h3, m -> h3.text(m.artistName)
                    }
                    .`__`() // h3
                    .hr().`__`()
                    .h3().text("AllMusic info:").`__`()
                    .ul()
                    .await<ArtistAsyncModel> { ul, m, cb -> Flux
                        .from(m.allMusic)
                        .doOnComplete(cb::finish)
                        .subscribe { ul
                            .li().text("Founded: ${it.year}").`__`()
                            .li().text("From: ${it.from}").`__`()
                            .li().text("Genre: ${it.genre}").`__`()
                        }
                    }
                    .`__`() // ul
                    .hr().`__`()
                    .b().text("Spotify popular tracks:").`__`()
                    .await<ArtistAsyncModel> { div, m, cb -> Flux
                        .from(m.spotify)
                        .doOnComplete(cb::finish)
                        .subscribe { song ->
                            song.popularSongs.forEach {
                                div.span().text("$it, ").`__`()
                            }
                        }
                    }
                    .hr().`__`()
                    .b().text("Apple Music top songs:").`__`()
                    .await<ArtistAsyncModel> { div, m, cb -> Flux
                        .from(m.apple)
                        .doOnComplete(cb::finish)
                        .subscribe { song ->
                            song.topSongs.forEach {
                                 div.span().text("$it, ").`__`()
                            }
                        }
                    }

                    .`__`() // div
                    .hr().`__`()
                    .footer()
                        .small()
                            .dynamic<ArtistAsyncModel> { small, m ->
                                small.text("${currentTimeMillis() - m.startTime} ms (response handling time)")
                            }
                        .`__`() // small
                    .`__`() // footer
                    .`__`() // body
                    .`__`() // html
}


class ArtistAsyncModel(
    val startTime: Long,
    val artistName: String,
    val allMusic: Flux<AllMusicArtist>,
    val spotify: Flux<SpotifyArtist>,
    val apple: Flux<AppleMusicArtist>,
)

/**
 * This web template solves the "pyramid of doom" of former htmlFlowArtistDocBlocking,
 * avoiding nested continuations in doOnComplete().
 * On the other, it is blocking on every CF with join().
 */
fun htmlFlowArtistDocBlocking(
    startTime: Long,
    artisName: String,
    cfAllMusic: CompletableFuture<AllMusicArtist>,
    cfSpotify: CompletableFuture<SpotifyArtist>,
    cfApple: CompletableFuture<AppleMusicArtist>
) : Publisher<String> {
    return AppendableSink().let { sink ->
        sink.asFLux().also {
            HtmlFlow
                .doc(sink)
                .html()
                .body()
                .div()
                .h3().text(artisName).`__`()
                .hr().`__`()
                .h3().text("AllMusic info:").`__`()
                .ul().of {
                    val allMusic = cfAllMusic.join()
                    it.li().text("Founded: ${allMusic.year}").`__`()
                    it.li().text("From: ${allMusic.from}").`__`()
                    it.li().text("Genre: ${allMusic.genre}").`__`()
                }
                .`__`() // ul
                .hr().`__`()
                .b().text("Spotify popular tracks:").`__`()
                .of { it.span().text(cfSpotify.join().popularSongs.joinToString(",")).`__`() }
                .hr().`__`()
                .b().text("Apple Music top songs:").`__`()
                .of { it.span().text(cfApple.join().topSongs.joinToString(",")).`__`() }
                .`__`() // div
                .hr().`__`()
                .footer()
                    .small()
                        .text("${currentTimeMillis() - startTime} ms (response handling time)")
                    .`__`() // small
                .`__`() // footer
                .`__`() // body
                .`__`() // html
            sink.close()
        }
    }
}

