package pt.isel.disco.htmlflow

import htmlflow.HtmlFlow
import htmlflow.suspending
import htmlflow.viewSuspend
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.reactivestreams.Publisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import org.xmlet.htmlapifaster.EnumBorderType
import pt.isel.disco.AppendableSink
import pt.isel.disco.artists
import pt.isel.disco.model.Artist
import pt.isel.disco.model.Track
import pt.isel.disco.tracks
import reactor.core.publisher.Mono
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit


fun artistRouterHtmlFlow(): RouterFunction<ServerResponse> {
    return RouterFunctions
        .route()
        .path("/htmlflow") { builder ->
            builder
                .GET("/blocking/weather/australia", ::htmlflowBlockingHandlerWeather)
                .GET("/reactive/weather/australia", ::htmlflowReactiveHandlerWeather)
                .GET("/suspending/weather/australia", ::htmlflowSuspendingHandlerWeather)
                .GET("/blocking/artist/{name}", ::htmlflowBlockingHandlerArtist)
                .GET("/reactive/artist/{name}", ::htmlflowReactiveHandlerArtist)
                .GET("/reactive/playlist", ::handlerPlaylist)
                .GET("/asyncview/artist/{name}", ::htmlflowAsyncViewHandlerArtist)
        }
        .build()
}


fun artistCoRouterHtmlFlow() = coRouter {
    "/htmlflow".nest {
        GET("/suspending/artist/{name}") (::htmlflowSuspendViewHandlerArtist)
        GET("/suspending/playlist")(::handlerPlaylistSuspending)
    }
}


private fun htmlflowBlockingHandlerWeather(req: ServerRequest): Mono<ServerResponse> {
    val australia = Weather("Australia", listOf(
        Location("Adelaide", "Light rain", 9),
        Location("Darwin", "Sunny day", 31),
        Location("Perth", "Sunny day", 16)))
    val html = AppendableSink().start {
        wxView.setOut(this).write(australia)
        this.close()
    }.asFlux()

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}
private fun htmlflowReactiveHandlerWeather(req: ServerRequest): Mono<ServerResponse> {
    val australia = WeatherRx("Australia", Observable
        .fromArray(
            Location("Adelaide", "Light rain", 9),
            Location("Darwin", "Sunny day", 31),
            Location("Perth", "Sunny day", 16)
        ).concatMap { Observable.just(it).delay(1000, TimeUnit.MILLISECONDS) }
    )
    val html = AppendableSink().start {
        australia.cities = australia.cities.doOnComplete {
            close()
        }
        wxRxView.setOut(this).write(australia)
    }.asFlux()
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}
private fun htmlflowSuspendingHandlerWeather(req: ServerRequest): Mono<ServerResponse> {
    val australia = WeatherRx("Australia", Observable
        .fromArray(
            Location("Adelaide", "Light rain", 9),
            Location("Darwin", "Sunny day", 31),
            Location("Perth", "Sunny day", 16)
        ).concatMap { Observable.just(it).delay(10000, TimeUnit.MILLISECONDS) }
    )
    val html = AppendableSink().start {
        wxSuspView.writeAsync(this, australia).thenAccept { this.close() }
    }.asFlux()
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}

private fun htmlflowBlockingHandlerArtist(req: ServerRequest): Mono<ServerResponse> {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val html: Publisher<String> = AppendableSink().start {
          artistView
            .setOut(this)
            .write(
                Artist(
                    currentTimeMillis(),
                    artist.name,
                    artist.monoMusicBrainz(),
                    artist.monoSpotify(),
                    artist.monoApple()
                )
            )
          close()
        }
        .asFlux()
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}

private fun htmlflowReactiveHandlerArtist(req: ServerRequest): Mono<ServerResponse> {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val view: Publisher<String> = htmlFlowArtistDoc(
        currentTimeMillis(),
        artist.name,
        artist.monoMusicBrainz(),
        artist.monoSpotify(),
        artist.monoApple()
    )
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(view, object : ParameterizedTypeReference<String>() {})
}

private fun htmlflowAsyncViewHandlerArtist(req: ServerRequest): Mono<ServerResponse>  {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val model = Artist(
        currentTimeMillis(),
        artist.name,
        artist.monoMusicBrainz(),
        artist.monoSpotify(),
        artist.monoApple()
    )
    val html = AppendableSink().start {
        htmlFlowArtistAsyncView.writeAsync(this, model)
            .thenAccept { this.close() }
    }
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html.asFlux(), object : ParameterizedTypeReference<String>() {})
}

private suspend fun htmlflowSuspendViewHandlerArtist(req: ServerRequest): ServerResponse  {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val model = Artist(
        currentTimeMillis(),
        artist.name,
        artist.monoMusicBrainz(),
        artist.monoSpotify(),
        artist.monoApple()
    )
    val html = AppendableSink().startSuspending {
        htmlFlowArtistSuspendingView.write(this, model)
        this.close()
    }
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html.asFlux(), object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}


private fun handlerPlaylist(req: ServerRequest): Mono<ServerResponse>  {
    val view = HtmlFlow.view<Observable<Track>> { page ->
            page
                .html()
                .head().title().text("Playlist").`__`().`__`()
                .body()
                .table().attrBorder(EnumBorderType._1)
                .tr().th().text("Track name").`__`().`__`()
                .dynamic<Observable<Track>>{ table, tracks ->
                    tracks
                        .subscribe { track ->
                            table.tr().td().text(track.name).`__`().`__`()
                        }
                }
                .`__`() // table
                .`__`() // body
                .`__`() // html
        }
    val html: Publisher<String> = AppendableSink().start {
        view.setOut(this).write(tracks.doOnComplete { close() })
    }.asFlux()
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}

private suspend fun handlerPlaylistSuspending(req: ServerRequest): ServerResponse  {
    val view = viewSuspend<Flow<Track>> {
                html()
                .head().title().text("Playlist").`__`().`__`()
                .body()
                .table().attrBorder(EnumBorderType._1)
                .tr().th().text("Track name").`__`().`__`()
                .suspending { tracks: Flow<Track> ->
                    tracks.collect{ track ->
                        tr().td().text(track.name).`__`().`__`()
                    }
                }
                .`__`() // table
                .`__`() // body
                .`__`() // html
        }
    val html: Publisher<String> = AppendableSink().startSuspending {
        view
            .write(this, tracks.toFlowable(BackpressureStrategy.BUFFER).asFlow())
            this.close()
    }.asFlux()

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}
