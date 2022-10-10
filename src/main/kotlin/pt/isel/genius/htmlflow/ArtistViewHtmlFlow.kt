package pt.isel.genius.htmlflow

import htmlflow.HtmlFlow
import htmlflow.HtmlView
import org.reactivestreams.Publisher
import pt.isel.genius.model.AllMusicArtist
import pt.isel.genius.model.AppleMusicArtist
import pt.isel.genius.model.SpotifyArtist
import reactor.core.publisher.Flux
import java.lang.System.currentTimeMillis

fun htmlFlowArtistDoc(
    startTime: Long,
    artisName: String,
    allMusic: Flux<AllMusicArtist>,
    spotify: Flux<SpotifyArtist>,
    apple: Flux<AppleMusicArtist>
) : Publisher<String> {
    return PrintStreamSink().let { sink ->
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

val htmlFlowArtistAsyncView = HtmlFlow.viewAsync(System.out, ::htmlFlowArtistAsyncViewTemplate)

fun htmlFlowArtistAsyncViewTemplate(view: HtmlView<ArtistAsyncModel>, model: ArtistAsyncModel)
{
    view
        .html()
            .body()
                .div()
                    .h3().text(model.artistName).`__`()
                    .hr().`__`()
                    .h3().text("AllMusic info:").`__`()
                    .ul()
                    .async(model.allMusic) { ul, allMusic -> Flux.from(allMusic)
                        .subscribe { ul
                            .li().text("Founded: ${it.year}").`__`()
                            .li().text("From: ${it.from}").`__`()
                            .li().text("Genre: ${it.genre}").`__`()
                        }
                    }
                    .then { ul -> ul
                        .`__`() // ul
                        .hr().`__`()
                        .b().text("Spotify popular tracks:").`__`()
                    }
                    .async(model.spotify) { div, spotify -> Flux.from(spotify)
                        .subscribe { song ->
                            song.popularSongs.forEach {
                                div.span().text("$it, ").`__`()
                            }
                        }
                    }
                    .then { div -> div
                        .hr().`__`()
                        .b().text("Apple Music top songs:").`__`()
                    }
                    .async(model.apple) { div, apple -> Flux.from(apple)
                        .subscribe { song ->
                            song.topSongs.forEach {
                                 div.span().text("$it, ").`__`()
                            }
                        }
                    }
                    .then { div -> div
                                    .`__`() // div
                                    .hr().`__`()
                                    .footer()
                                        .small()
                                            .text("${currentTimeMillis() - model.startTime} ms (response handling time)")
                                        .`__`() // small
                                    .`__`() // footer
                                    .`__`() // body
                                    .`__`() // html
                    }
}


class ArtistAsyncModel(
    val startTime: Long,
    val artistName: String,
    val allMusic: Flux<AllMusicArtist>,
    val spotify: Flux<SpotifyArtist>,
    val apple: Flux<AppleMusicArtist>,
)
