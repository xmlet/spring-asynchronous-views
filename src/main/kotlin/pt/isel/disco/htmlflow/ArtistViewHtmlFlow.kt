package pt.isel.disco.htmlflow

import htmlflow.*
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.future.await
import org.reactivestreams.Publisher
import org.xmlet.htmlapifaster.EnumBorderType._1
import pt.isel.disco.AppendableSink
import pt.isel.disco.model.AppleMusicArtist
import pt.isel.disco.model.MusicBrainz
import pt.isel.disco.model.SpotifyArtist
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
                .h3().text(artisName).l
                .h3().text("MusicBrainz info:").l
                .ul().of { ul -> musicBrainz
                    .thenAccept { ul
                        .li().text("Founded: ${it.year}").l
                        .li().text("From: ${it.from}").l
                        .li().text("Genre: ${it.genres}").l
                    }
                    .thenAccept { ul
                        .l // ul
                        .p()
                        .b().text("Spotify popular tracks:").l
                        .of { div -> spotify
                            .thenAccept { song ->
                                div.text(song.popularSongs.joinToString(", "))
                            }
                            .thenAccept { div
                                .l // p
                                .l // body
                                .l // html
                                this.close()
                            }
                        }
                    }
                }
        }
        .asFlux()
}

val htmlFlowArtistAsyncView = HtmlFlow.viewAsync<ArtistAsync> { page -> page
.html()
.body()
.h3().dynamic<ArtistAsync> {
    h3, m -> h3.text(m.artistName)
}
.l // h3
.h3().text("MusicBrainz info:").l
.ul()
    .await<ArtistAsync> { ul, m, cb -> m
        .musicBrainz
        .thenAccept { ul
            .li().text("Founded: ${it.year}").l
            .li().text("From: ${it.from}").l
            .li().text("Genre: ${it.genres}").l
            cb.finish()
        }
    }
.l // ul
.p()
.b().text("Spotify popular tracks:").l
    .await<ArtistAsync> { p, m, cb -> m
        .spotify
        .thenAccept { song ->
            p.text(song.popularSongs.joinToString(", "))
            cb.finish()
        }
    }
.l // p
.l // body
.l // html
}

val htmlFlowArtistSuspendingView = HtmlFlow.viewAsync<ArtistAsync> { page -> page
    .html()
    .body()
    .h3().dyn { m: ArtistAsync -> text(m.artistName) }
    .l // h3
    .h3().text("MusicBrainz info:").l
    .ul()
    .suspending { m: ArtistAsync ->
        val mb = m.musicBrainz.await()
        li().text("Founded: ${mb.year}").l
        li().text("From: ${mb.from}").l
        li().text("Genre: ${mb.genres}").l
    }
    .l // ul
    .p().b().text("Spotify popular tracks:").l
    .suspending { m: ArtistAsync ->
        val spotify = m.spotify.await()
        text(spotify.popularSongs.joinToString(", "))
    }
    .l // p
    .l // body
    .l // html
}


class ArtistAsync(
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
val htmlFlowArtistBlocking = HtmlFlow.view<ArtistAsync> { page -> page
    .html()
    .body()
    .h3().dyn { m: ArtistAsync ->  text(m.artistName) }.l
    .h3().text("MusicBrainz info:").l
    .ul()
      .dyn { m: ArtistAsync -> m.musicBrainz.thenAccept { mb ->
        li().text("Founded: ${mb.year}").l
        li().text("From: ${mb.from}").l
        li().text("Genre: ${mb.genres}").l
      }}
    .l // ul
    .p()
    .b().text("Spotify popular tracks:").l
    .dyn { m: ArtistAsync -> m.spotify.thenAccept { spt ->
        text(spt.popularSongs.joinToString(", "))
    }}
//                .hr().l
//                .b().text("Apple Music top songs:").l
//                .of { it.span().text(cfApple.join().topSongs.joinToString(", ")).l }
    .l // p
    .l // body
    .l // html
}

val wxView = view<Weather> {
        html()
            .head()
                .title().dyn { m: Weather ->
                    text(m.country)
                }
                .l // title
            .l // head
            .body()
                .table().attrBorder(_1)
                    .tr()
                        .th().text("City").l
                        .th().text("Temperature").l
                    .l // tr
                .dyn { m: Weather ->
                    m.cities.forEach {
                        tr()
                            .td().text(it.city).l
                            .td().text(it.celsius).l
                        .l // tr
                    }
                }
                .l // table
            .l // body
        .l // html
}

val wxRxView = view<WeatherRx> {
    html()
        .head()
          .title().dyn { m: WeatherRx ->
            text(m.country)
          }
          .l // title
        .l // head
        .body()
          .table().attrBorder(_1)
            .tr()
              .th().text("City").l
              .th().text("Temperature").l
            .l // tr
            .dyn { m: WeatherRx ->
              m.cities.forEach {
                tr()
                    .td().text(it.city).l
                    .td().text(it.celsius).l
                .l // tr
              }
            }
          .l // table
        .l // body
    .l // html
}

val wxSuspView: HtmlViewAsync<WeatherRx> = viewAsync<WeatherRx> {
    html()
        .head()
        .title().dyn { m: WeatherRx ->
            text(m.country)
        }
        .l // title
        .l // head
        .body()
        .table().attrBorder(_1)
        .tr()
        .th().text("City").l
        .th().text("Temperature").l
        .l // tr
        .await { table, m: WeatherRx, cb ->
            m.cities
                .doOnComplete { cb.finish() }
                .forEach { table
                    .tr()
                        .td().text(it.city).l
                        .td().text(it.celsius).l
                    .l // tr
                }
        }
        .l // table
        .l // body
        .l // html
}



data class Weather(val country: String, val cities: Iterable<Location>)
data class WeatherRx(val country: String, val cities: Observable<Location>)
data class Location(val city: String, val desc: String, val celsius: Int)