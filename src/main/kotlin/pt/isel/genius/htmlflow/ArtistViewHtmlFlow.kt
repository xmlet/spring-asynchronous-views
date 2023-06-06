package pt.isel.genius.htmlflow

import htmlflow.HtmlFlow
import htmlflow.HtmlPage
import htmlflow.HtmlViewAsync
import org.reactivestreams.Publisher
import pt.isel.genius.AppendableSink
import pt.isel.genius.model.AppleMusicArtist
import pt.isel.genius.model.MusicBrainz
import pt.isel.genius.model.SpotifyArtist
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
    musicBrainz: CompletableFuture<MusicBrainz>,
    spotify: CompletableFuture<SpotifyArtist>,
    apple: CompletableFuture<AppleMusicArtist>
) : Publisher<String> {
    return AppendableSink {
            HtmlFlow
                .doc(this)
                .html()
                .body()
                .div()
                .h3().text(artisName).`__`()
                .hr().`__`()
                .h3().text("MusicBrainz info:").`__`()
                .ul().of { ul -> musicBrainz
                    .thenAccept { ul
                        .li().text("Founded: ${it.year}").`__`()
                        .li().text("From: ${it.from}").`__`()
                        .li().text("Genre: ${it.genres}").`__`()
                    }
                    .thenAccept { ul
                        .`__`() // ul
                        .hr().`__`()
                        .b().text("Spotify popular tracks:").`__`()
                        .of { div -> spotify
                            .thenAccept { song ->
                                song.popularSongs.forEach {
                                    div.span().text("$it, ").`__`()
                                }
                            }
                            .thenAccept {
                                div
                                    .hr().`__`()
                                    .b().text("Apple Music top songs:").`__`()
                                apple
                                    .thenAccept { song ->
                                        song.topSongs.forEach {
                                             div.span().text("$it, ").`__`()
                                        }
                                    }
                                    .thenAccept { div
                                        .`__`() // div
                                        .hr().`__`()
                                        .footer()
                                            .small()
                                                .text("${currentTimeMillis() - startTime} ms (response handling time)")
                                            .`__`() // small
                                        .`__`() // footer
                                        .`__`() // body
                                        .`__`() // html
                                        this.close()
                                    }
                            }
                        }
                    }
                }
        }
        .asFlux()
}

val htmlFlowArtistAsyncView = HtmlFlow.viewAsync { page -> page
.html()
.body()
.div()
.h3().dynamic<ArtistAsyncModel> {
    h3, m -> h3.text(m.artistName)
}
.`__`() // h3
.hr().`__`()
.h3().text("MusicBrainz info:").`__`()
.ul()
    .await<ArtistAsyncModel> { ul, m, cb -> m
        .musicBrainz
        .thenAccept { ul
            .li().text("Founded: ${it.year}").`__`()
            .li().text("From: ${it.from}").`__`()
            .li().text("Genre: ${it.genres}").`__`()
            cb.finish()
        }
    }
.`__`() // ul
.hr().`__`()
.b().text("Spotify popular tracks:").`__`()
.span()
    .await<ArtistAsyncModel> { span, m, cb -> m
        .spotify
        .thenAccept { song ->
            span.text(song.popularSongs.joinToString(", "))
            cb.finish()
        }
    }
.`__`() // span
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
    val musicBrainz: CompletableFuture<MusicBrainz>,
    val spotify: CompletableFuture<SpotifyArtist>,
    val apple: CompletableFuture<AppleMusicArtist>,
)

/**
 * This web template solves the "pyramid of doom" of former htmlFlowArtistDocBlocking,
 * avoiding nested continuations in doOnComplete().
 * On the other, it is blocking on every CF with join().
 */
fun htmlFlowArtistDocBlocking(
    startTime: Long,
    artisName: String,
    cfMusicBrainz: CompletableFuture<MusicBrainz>,
    cfSpotify: CompletableFuture<SpotifyArtist>,
    cfApple: CompletableFuture<AppleMusicArtist>
) : Publisher<String> {
    return AppendableSink {
            HtmlFlow
                .doc(this)
                .html()
                .body()
                .div()
                .h3().text(artisName).`__`()
                .hr().`__`()
                .h3().text("MusicBrainz info:").`__`()
                .ul().of {
                    val musicBrainz = cfMusicBrainz.join()
                    it.li().text("Founded: ${musicBrainz.year}").`__`()
                    it.li().text("From: ${musicBrainz.from}").`__`()
                    it.li().text("Genre: ${musicBrainz.genres}").`__`()
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
            this.close()
        }
        .asFlux()
}


