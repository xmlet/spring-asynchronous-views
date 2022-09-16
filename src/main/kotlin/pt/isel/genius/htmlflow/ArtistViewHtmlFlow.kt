package pt.isel.genius.htmlflow

import htmlflow.StaticHtml
import org.reactivestreams.Publisher
import pt.isel.genius.model.AllMusicArtist
import pt.isel.genius.model.AppleMusicArtist
import pt.isel.genius.model.SpotifyArtist
import reactor.core.publisher.Flux
import java.lang.System.currentTimeMillis

fun htmlFlowArtistReactive(
    startTime: Long,
    artisName: String,
    allMusic: Flux<AllMusicArtist>,
    spotify: Flux<SpotifyArtist>,
    apple: Flux<AppleMusicArtist>
) : Publisher<String> {
    val sink = PrintStreamSink()
    return sink.asFLux().also {
        StaticHtml
            .view(sink)
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
                    .li().text("Founded: ${it.genre}").`__`()
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
