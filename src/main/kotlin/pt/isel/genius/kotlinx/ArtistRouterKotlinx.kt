package pt.isel.genius.kotlinx

import kotlinx.coroutines.reactor.awaitSingle
import org.reactivestreams.Publisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import pt.isel.genius.artists
import pt.isel.genius.model.Artist

fun artistCoRouterKotlinX(): RouterFunction<ServerResponse> {
    return coRouter {
        GET("/kotlinx/blocking/artist/{name}", ::handlerArtistKotlinXBlocking)
        GET("/kotlinx/reactive/artist/{name}", ::handlerArtistKotlinXReactive)
        GET("/kotlinx/coroutine/artist/{name}", ::handlerArtistKotlinXCoroutine)
    }
}

private suspend fun handlerArtistKotlinXCoroutine(req: ServerRequest): ServerResponse {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val body = kotlinXArtistCoroutine(artist.monoMusicBrainz)

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(body, object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}

private suspend fun handlerArtistKotlinXReactive(req: ServerRequest): ServerResponse {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val body: Publisher<String> = kotlinXArtistReactive(
        System.currentTimeMillis(),
        name,
        artist.monoMusicBrainz.toFuture(),
        artist.monoSpotify.toFuture(),
    ).asFlux()

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(body, object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}

private suspend fun handlerArtistKotlinXBlocking(req: ServerRequest): ServerResponse {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val body: Publisher<String> = kotlinXArtistBlocking(
        System.currentTimeMillis(),
        name.split(" ").joinToString(" ") { it.capitalize() },
        artist.monoMusicBrainz.toFuture(),
        artist.monoSpotify.toFuture(),
        artist.monoApple.toFuture()
    )

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(body, object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}
