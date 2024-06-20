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
import java.lang.String.join

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
    return AppendableSink().start {
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

val htmlFlowArtistAsyncView = HtmlFlow.viewAsync<Artist> { page -> page
.html()
.body()
.h3().dynamic<Artist> {
    h3, m -> h3.text(m.name)
}
.l // h3
.h3().text("MusicBrainz info:").l
.ul()
    .await<Artist> { ul, m, cb -> m
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
    .await<Artist> { p, m, cb -> m
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

val htmlFlowArtistSuspendingView = viewSuspend<Artist> {
  html {
    body {
      h3 { dyn { m: Artist -> text(m.name) } }
      h3 { text("MusicBrainz info:") }
      ul {
        suspending { m: Artist ->
          val mb = m.musicBrainz.await()
          li { text("Founded: ${mb.year}") }
          li { text("From: ${mb.from}") }
          li { text("Genre: ${mb.genres}") }
        }
      }
      p {
        b { +"Spotify popular tracks:" }
        suspending { m: Artist ->
          val spotify = m.spotify.await()
          text(spotify.popularSongs.joinToString(", "))
        }
      } // p
    } // body
  } // html
}

class Artist(
    val startTime: Long,
    val name: String,
    val musicBrainz: CompletableFuture<MusicBrainz>,
    val spotify: CompletableFuture<SpotifyArtist>,
    val apple: CompletableFuture<AppleMusicArtist>,
)

val artistView: HtmlView<Artist> = view<Artist> {
  html {
    body {
      h3 {
        dyn { m: Artist ->  text(m.name) }
      } // h3
      h3 { +"MusicBrainz info:" }
      ul {
        dyn { m: Artist -> m.musicBrainz
          .thenAccept {
            li { +"Founded: ${it.year}" }
            li { +"From: ${it.from}" }
            li { +"Genre: ${it.genres}" }
          }  // thenAccept
        } // dyn
      } // ul
      p {
        b { +"Spotify popular tracks:" }
        dyn { m: Artist -> m.spotify
          .thenAccept {
            text(join(", ", it.popularSongs))
          } // thenAccept
        } // dyn
      } // p
    } // body
  } // html
}

val wxView: HtmlView<Weather> = view<Weather> {
        html {
            head {
                title { dyn { m: Weather ->
                    text(m.country)
                } } // title
            } // head
            body {
                table { attrBorder(_1)
                    tr {
                        th { +"City" }
                        th { +"Temperature" }
                    }
                    dyn { m: Weather ->
                        m.cities.forEach {
                            tr {
                                td { text(it.city) }
                                td { text(it.celsius) }
                            }
                        }
                    }
                } // table
            } // body
        } // html
}

/**
 * Show a Mallformed HTML using an HtmlView with dyn and forEach
 * on an Observable.
 */
val wxRxView: HtmlView<WeatherRx> = view<WeatherRx> {
    html {
        head {
            title { dyn { m: WeatherRx ->
                text(m.country)
            } } // title
        } // head
        body {
            table { attrBorder(_1)
                tr {
                    th { text("City") }
                    th { text("Temperature") }
                }
                dyn { m: WeatherRx ->
                    m.cities.forEach {
                        tr {
                            td { text(it.city) }
                            td { text(it.celsius) }
                        }
                    }
                }
            } // table
        } // body
    } // html
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
data class WeatherRx(val country: String, var cities: Observable<Location>)
data class Location(val city: String, val desc: String, val celsius: Int)