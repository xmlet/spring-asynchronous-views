package pt.isel.genius.htmlflow

import htmlflow.HtmlViewAsync
import org.reactivestreams.Publisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
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
        artist.cfAllMusicArtist.toFuture(),
        artist.cfSpotify.toFuture(),
        artist.cfApple.toFuture()
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
        artist.pubAllMusicArtist,
        artist.pubSpotify,
        artist.pubApple
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
        artist.pubAllMusicArtist,
        artist.pubSpotify,
        artist.pubApple
    )
    val view: Publisher<String> = PrintStreamSink().let { sink ->
        sink.asFLux().also {
            (htmlFlowArtistAsyncView
                .setPrintStream(sink) as HtmlViewAsync<ArtistAsyncModel>)
                .writeAsync(model)
                .thenAccept { sink.close() }
        }
    }
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(view, object : ParameterizedTypeReference<String>() {})
}
