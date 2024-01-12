package pt.isel.genius.htmlflow

import htmlflow.*
import kotlinx.coroutines.future.await
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
                .h3().text(artisName).l
                .hr().l
                .h3().text("MusicBrainz info:").l
                .ul().of { ul -> musicBrainz
                    .thenAccept { ul
                        .li().text("Founded: ${it.year}").l
                        .li().text("From: ${it.from}").l
                        .li().text("Genre: ${it.genres}").l
                    }
                    .thenAccept { ul
                        .l // ul
                        .hr().l
                        .b().text("Spotify popular tracks:").l
                        .of { div -> spotify
                            .thenAccept { song ->
                                song.popularSongs.forEach {
                                    div.span().text("$it, ").l
                                }
                            }
                            .thenAccept {
                                div
                                    .hr().l
                                    .b().text("Apple Music top songs:").l
                                apple
                                    .thenAccept { song ->
                                        song.topSongs.forEach {
                                             div.span().text("$it, ").l
                                        }
                                    }
                                    .thenAccept { div
                                        .l // div
                                        .hr().l
                                        .footer()
                                            .small()
                                                .text("${currentTimeMillis() - startTime} ms (response handling time)")
                                            .l // small
                                        .l // footer
                                        .l // body
                                        .l // html
                                        this.close()
                                    }
                            }
                        }
                    }
                }
        }
        .asFlux()
}

val htmlFlowArtistAsyncView = HtmlFlow.viewAsync<ArtistAsyncModel> { page -> page
.html()
.body()
.div()
.h3().dynamic<ArtistAsyncModel> {
    h3, m -> h3.text(m.artistName)
}
.l // h3
.hr().l
.h3().text("MusicBrainz info:").l
.ul()
    .await<ArtistAsyncModel> { ul, m, cb -> m
        .musicBrainz
        .thenAccept { ul
            .li().text("Founded: ${it.year}").l
            .li().text("From: ${it.from}").l
            .li().text("Genre: ${it.genres}").l
            cb.finish()
        }
    }
.l // ul
.hr().l
.b().text("Spotify popular tracks:").l
.span()
    .await<ArtistAsyncModel> { span, m, cb -> m
        .spotify
        .thenAccept { song ->
            span.text(song.popularSongs.joinToString(", "))
            cb.finish()
        }
    }
.l // span
.l // div
.hr().l
.footer()
    .small()
        .dynamic<ArtistAsyncModel> { small, m ->
            small.text("${currentTimeMillis() - m.startTime} ms (response handling time)")
        }
    .l // small
.l // footer
.l // body
.l // html
}

val htmlFlowArtistSuspendingView = HtmlFlow.viewAsync<ArtistAsyncModel> { page -> page
    .html()
    .body()
    .div()
    .h3().dynamic<ArtistAsyncModel> {
            h3, m -> h3.text(m.artistName)
    }
    .l // h3
    .hr().l
    .h3().text("MusicBrainz info:").l
    .ul()
    .suspending { ul, m: ArtistAsyncModel ->
        val mb = m.musicBrainz.await()
        ul
            .li().text("Founded: ${mb.year}").l
            .li().text("From: ${mb.from}").l
            .li().text("Genre: ${mb.genres}").l

    }
    .l // ul
    .hr().l
    .b().text("Spotify popular tracks:").l
    .span()
    .suspending { span, m: ArtistAsyncModel ->
        val spotify = m.spotify.await()
        span.text(spotify.popularSongs.joinToString(", "))
    }
    .l // span
    .l // div
    .hr().l
    .footer()
    .small()
    .dynamic<ArtistAsyncModel> { small, m ->
        small.text("${currentTimeMillis() - m.startTime} ms (response handling time)")
    }
    .l // small
    .l // footer
    .l // body
    .l // html
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
                .h3().text(artisName).l
                .hr().l
                .h3().text("MusicBrainz info:").l
                .ul().of {
                    val musicBrainz = cfMusicBrainz.join()
                    it.li().text("Founded: ${musicBrainz.year}").l
                    it.li().text("From: ${musicBrainz.from}").l
                    it.li().text("Genre: ${musicBrainz.genres}").l
                }
                .l // ul
                .hr().l
                .b().text("Spotify popular tracks:").l
                .of { it.span().text(cfSpotify.join().popularSongs.joinToString(",")).l }
                .hr().l
                .b().text("Apple Music top songs:").l
                .of { it.span().text(cfApple.join().topSongs.joinToString(",")).l }
                .l // div
                .hr().l
                .footer()
                    .small()
                        .text("${currentTimeMillis() - startTime} ms (response handling time)")
                    .l // small
                .l // footer
                .l // body
                .l // html
            this.close()
        }
        .asFlux()
}


