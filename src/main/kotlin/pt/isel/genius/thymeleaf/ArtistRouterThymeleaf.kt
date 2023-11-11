package pt.isel.genius.thymeleaf

import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable
import pt.isel.genius.artists
import pt.isel.genius.model.Artist
import pt.isel.genius.model.Track
import pt.isel.genius.tracks
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.TimeUnit


fun artistRouterThymeleaf(): RouterFunction<ServerResponse> {
    return RouterFunctions
        .route()
        .path("/thymeleaf") { builder ->
            builder
                .GET("/blocking/artist/{name}", ::thymeleafBlockingHandlerArtist)
                .GET("/reactive/artist/{name}", ::thymeleafReactiveHandlerArtist)
                .GET("/reactive/playlist", ::thymeleafReactiveHandlerPlaylist)
        }
        .build()
}

private fun thymeleafBlockingHandlerArtist(req: ServerRequest): Mono<ServerResponse> {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val model = mapOf<String, Any>(
        "startTime" to System.currentTimeMillis(),
        "artistName" to artist.name,
        "musicBrainz" to artist.monoMusicBrainz, // Implicit Call to join() made by Spring
        "spotify" to artist.monoSpotify
    )
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .render("artist", model);
}

private fun thymeleafReactiveHandlerArtist(req: ServerRequest): Mono<ServerResponse> {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val model = mapOf<String, Any>(
        "startTime" to System.currentTimeMillis(),
        "artistName" to artist.name,
        /**
         * !!! Only one data-driver variable is allowed to be specified as a model attribute!!!!
         * Otherwise, it causes TemplateProcessingException.
         */
        "musicBrainz" to ReactiveDataDriverContextVariable(artist.monoMusicBrainz.flux(), 1),
        "spotify" to ReactiveDataDriverContextVariable(artist.monoSpotify, 1)
    )
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .render("artistReactive", model);
}

private fun thymeleafReactiveHandlerPlaylist(re: ServerRequest): Mono<ServerResponse> {

    val model = mapOf<String, Any>(
        "tracks" to ReactiveDataDriverContextVariable(tracks.toFlowable(BackpressureStrategy.BUFFER),1)
    )
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .render("playlist", model);
}
