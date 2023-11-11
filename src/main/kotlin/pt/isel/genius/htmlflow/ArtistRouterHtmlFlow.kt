package pt.isel.genius.htmlflow

import htmlflow.HtmlFlow
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.reactivestreams.Publisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.xmlet.htmlapifaster.EnumBorderType
import org.xmlet.htmlapifaster.Table
import pt.isel.genius.AppendableSink
import pt.isel.genius.artists
import pt.isel.genius.model.Artist
import pt.isel.genius.model.Track
import pt.isel.genius.tracks
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.System.currentTimeMillis


fun artistRouterHtmlFlow(): RouterFunction<ServerResponse> {
    return RouterFunctions
        .route()
        .path("/htmlflow") { builder ->
            builder
                .GET("/blocking/artist/{name}", ::htmlflowBlockingHandlerArtist)
                .GET("/reactive/artist/{name}", ::htmlflowReactiveHandlerArtist)
                .GET("/reactive/playlist", ::handlerPlaylist)
                .GET("/asyncview/artist/{name}", ::htmlflowAsyncViewHandlerArtist)
        }
        .build()
}

private fun htmlflowBlockingHandlerArtist(req: ServerRequest): Mono<ServerResponse> {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val view: Publisher<String> = htmlFlowArtistDocBlocking(
        currentTimeMillis(),
        artist.name,
        artist.monoMusicBrainz.toFuture(),
        artist.monoSpotify.toFuture(),
        artist.monoApple.toFuture()
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
        artist.monoMusicBrainz.toFuture(),
        artist.monoSpotify.toFuture(),
        artist.monoApple.toFuture()
    )
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(view, object : ParameterizedTypeReference<String>() {})
}

private fun htmlflowAsyncViewHandlerArtist(req: ServerRequest): Mono<ServerResponse> {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val model = ArtistAsyncModel(
        currentTimeMillis(),
        artist.name,
        artist.monoMusicBrainz.toFuture(),
        artist.monoSpotify.toFuture(),
        artist.monoApple.toFuture()
    )
    val html: Publisher<String> = AppendableSink {
        htmlFlowArtistAsyncView
            .writeAsync(this, model)
            .thenAccept { this.close() }
    }
        .asFlux()
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(html, object : ParameterizedTypeReference<String>() {})
}


private fun handlerPlaylist(req: ServerRequest): Mono<ServerResponse>  {
    val view = HtmlFlow.view { page ->
            page
                .html()
                .head().title().text("Playlist").`__`().`__`()
                .body()
                .table().attrBorder(EnumBorderType._1)
                .tr().th().text("Track name").`__`().`__`()
                .dynamic<Observable<Track>>{ table, tracks ->
                    tracks
                        .doOnNext { track ->
                            table.tr().td().text(track.name).`__`().`__`()
                        }
                        .subscribe()
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
