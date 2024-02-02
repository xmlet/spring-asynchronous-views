package pt.isel.disco.htmlflow

import htmlflow.HtmlFlow
import htmlflow.HtmlViewAsync
import htmlflow.suspending
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.reactivestreams.Publisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
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
                .GET("/blocking/weather/portugal", ::htmlflowBlockingHandlerWeather)
                .GET("/reactive/weather/portugal", ::htmlflowReactiveHandlerWeather)
                .GET("/suspending/weather/portugal", ::htmlflowSuspendingHandlerWeather)
                .GET("/blocking/artist/{name}", ::htmlflowBlockingHandlerArtist)
                .GET("/reactive/artist/{name}", ::htmlflowReactiveHandlerArtist)
                .GET("/reactive/playlist", ::handlerPlaylist)
                .GET("/suspending/playlist", ::handlerPlaylistSuspending)
                .GET("/asyncview/artist/{name}", htmlflowAsyncViewHandlerArtist { out, model ->
                    htmlFlowArtistAsyncView
                    .writeAsync(out, model)
                    .thenAccept { out.close() }
                })
                .GET("/suspending/artist/{name}", htmlflowAsyncViewHandlerArtist { out, model ->
                    htmlFlowArtistSuspendingView
                        .writeAsync(out, model)
                        .thenAccept { out.close() }
                })
        }
        .build()
}

private fun htmlflowBlockingHandlerWeather(req: ServerRequest): Mono<ServerResponse> {
    val portugal = Weather("Portugal", listOf(
        Location("Porto", "Light rain", 14),
        Location("Lisbon", "Sunny day", 14),
        Location("Sagres", "Sunny day", 18)))
    val html = AppendableSink {
        wxView.setOut(this).write(portugal)
        this.close()
    }.asFlux()

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}
private fun htmlflowReactiveHandlerWeather(req: ServerRequest): Mono<ServerResponse> {
    val portugal = WeatherRx("Portugal", Observable
        .fromArray(
            Location("Porto", "Light rain", 14),
            Location("Lisbon", "Sunny day", 14),
            Location("Sagres", "Sunny day", 18)
        ).concatMap { Observable.just(it).delay(1000, TimeUnit.MILLISECONDS) }
    )
    val html = AppendableSink {
        wxRxView.setOut(this).write(portugal)
    }.asFlux()
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}
private fun htmlflowSuspendingHandlerWeather(req: ServerRequest): Mono<ServerResponse> {
    val portugal = WeatherRx("Portugal", Observable
        .fromArray(
            Location("Porto", "Light rain", 14),
            Location("Lisbon", "Sunny day", 14),
            Location("Sagres", "Sunny day", 18)
        ).concatMap { Observable.just(it).delay(10000, TimeUnit.MILLISECONDS) }
    )
    val html = AppendableSink {
        wxSuspView.writeAsync(this, portugal).thenAccept { this.close() }
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
    val view: Publisher<String> = htmlFlowArtistDocBlocking(
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

private fun htmlflowAsyncViewHandlerArtist(view: (AppendableSink, ArtistAsyncModel) -> Unit): (ServerRequest) -> Mono<ServerResponse> = { req ->
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val model = ArtistAsyncModel(
        currentTimeMillis(),
        artist.name,
        artist.monoMusicBrainz(),
        artist.monoSpotify(),
        artist.monoApple()
    )
    val html: Publisher<String> = AppendableSink { view(this, model) }
        .asFlux()
    ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
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
    val html: Publisher<String> = AppendableSink {
        view.setOut(this).write(tracks.doOnComplete { close() })
    }.asFlux()
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}

private fun handlerPlaylistSuspending(req: ServerRequest): Mono<ServerResponse>  {
    val view: HtmlViewAsync<Flow<Track>> = HtmlFlow.viewAsync<Flow<Track>> { page ->
            page
                .html()
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
    val html: Publisher<String> = AppendableSink {
        view
            .writeAsync(this, tracks.toFlowable(BackpressureStrategy.BUFFER).asFlow())
            .thenAccept { this.close() }
    }.asFlux()

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}
