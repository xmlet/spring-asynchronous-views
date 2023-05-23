package pt.isel.genius.htmlflow

import org.reactivestreams.Publisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import pt.isel.genius.AppendableSink
import pt.isel.genius.artists
import pt.isel.genius.model.Artist
import reactor.core.publisher.Mono
import java.lang.System.currentTimeMillis


fun artistRouterHtmlFlow(): RouterFunction<ServerResponse> {
    return RouterFunctions
        .route()
        .path("/htmlflow") { builder ->
            builder
                .GET("/blocking/artist/{name}", ::htmlflowBlockingHandlerArtist)
                .GET("/reactive/artist/{name}", ::htmlflowReactiveHandlerArtist)
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
