package pt.isel.disco.htmlflow

import htmlflow.HtmlFlow
import htmlflow.suspending
import htmlflow.viewSuspend
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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


/**
 * It executes the initial continuation of a coroutine in the current
 * call-frame and lets the coroutine resume in whatever thread.
 * Better performance but not suitable for blocking IO, only for NIO.
 * NIO will release threads to perform other task.
 */
private val unconf = CoroutineScope(Dispatchers.Unconfined)

fun artistRouterHtmlFlow(): RouterFunction<ServerResponse> {
    return RouterFunctions
        .route()
        .path("/htmlflow") { builder ->
            builder
                .GET("/blocking/weather/australia", ::htmlflowBlockingHandlerWeather)
                .GET("/reactive/weather/australia", ::htmlflowReactiveHandlerWeather)
                .GET("/async/weather/australia", ::htmlflowAsyncHandlerWeather)
                .GET("/blocking/artist/{name}", ::htmlflowBlockingHandlerArtist)
                .GET("/reactive/artist/{name}", ::htmlflowReactiveHandlerArtist)
                .GET("/reactive/playlist", ::handlerPlaylist)
                .GET("/asyncview/artist/{name}", ::htmlflowAsyncViewHandlerArtist)
        }
        .build()
}


fun artistCoRouterHtmlFlow() = coRouter {
    "/htmlflow".nest {
        GET("/suspending/weather/australia", ::htmlflowSuspendingHandlerWeather)
        GET("/suspending/artist/{name}") (::htmlflowSuspendViewHandlerArtist)
        GET("/suspending/playlist")(::handlerPlaylistSuspending)
    }
}

val australia = WeatherRx("Australia", Observable
    .fromArray(
        Location("Adelaide", "Light rain", 9),
        Location("Darwin", "Sunny day", 31),
        Location("Perth", "Sunny day", 16)
    ).concatMap { Observable.just(it).delay(1000, TimeUnit.MILLISECONDS) }
)

private fun htmlflowBlockingHandlerWeather(req: ServerRequest): Mono<ServerResponse> {
    val australia = Weather("Australia", australia.cities.toList().blockingGet())
    val html = AppendableSink().also {
        wxView.setOut(it).write(australia)
        it.close()
    }.asFlux()

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}
private fun htmlflowReactiveHandlerWeather(req: ServerRequest): Mono<ServerResponse> {
    val html = AppendableSink().also {
        australia.cities = australia.cities.doOnComplete {
            it.close()
        }
        wxRxView.setOut(it).write(australia)
    }.asFlux()
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}
private fun htmlflowAsyncHandlerWeather(req: ServerRequest): Mono<ServerResponse> {
    val html = AppendableSink().also { out ->
        wxAsyncView.writeAsync(out, australia).thenAccept { out.close() }
    }.asFlux()
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}

private suspend fun htmlflowSuspendingHandlerWeather(req: ServerRequest): ServerResponse  {
    /*
     * We need another co-routine to render concurrently and ensure
     * progressive server-side rendering (PSSR)
     * Here we are using Unconfined running in same thread and avoiding context switching.
     * That's ok since we are NOT blocking on htmlFlowTemplateSuspending.
     */
    val html = AppendableSink().also { unconf.launch {
        wxSuspendingView.write(it, australia)
        it.close()
    }}
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html.asFlux(), object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}

private fun htmlflowBlockingHandlerArtist(req: ServerRequest): Mono<ServerResponse> {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val html: Publisher<String> = AppendableSink().also {
          artistView
            .setOut(it)
            .write(
                Artist(
                    currentTimeMillis(),
                    artist.name,
                    artist.monoMusicBrainz(),
                    artist.monoSpotify(),
                    artist.monoApple()
                )
            )
          it.close()
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
    val html = AppendableSink().also { out ->
        htmlFlowArtistAsyncView.writeAsync(out, model)
            .thenAccept { out.close() }
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
    /*
     * We need another co-routine to render concurrently and ensure
     * progressive server-side rendering (PSSR)
     * Here we are using Unconfined running in same thread and avoiding context switching.
     * That's ok since we are NOT blocking on htmlFlowTemplateSuspending.
     */
    val html = AppendableSink().also { unconf.launch {
        htmlFlowArtistSuspendingView.write(it, model)
        it.close()
    }}
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
    val html = AppendableSink().also { view
        .setOut(it)
        .write(tracks.doOnComplete { it.close() })
    }
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html.asFlux(), object : ParameterizedTypeReference<String>() {})
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
    /*
     * We need another co-routine to render concurrently and ensure
     * progressive server-side rendering (PSSR)
     * Here we are using Unconfined running in same thread and avoiding context switching.
     * That's ok since we are NOT blocking on htmlFlowTemplateSuspending.
     */
    val html: Publisher<String> = AppendableSink().also {  unconf.launch {
        view
            .write(it, tracks.toFlowable(BackpressureStrategy.BUFFER).asFlow())
        it.close()
    }}.asFlux()

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}
