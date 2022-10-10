package pt.isel.genius.kotlinx

import kotlinx.coroutines.reactor.awaitSingle
import org.reactivestreams.Publisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import pt.isel.genius.artists
import pt.isel.genius.htmlflow.htmlFlowArtistReactive
import pt.isel.genius.model.Artist

fun artistCoRouterKotlinX(): RouterFunction<ServerResponse> {
    return coRouter {
        GET("/kotlinx/reactive/artist/{name}", ::handlerArtistKotlinXReactive)
        GET("/kotlinx/coroutine/artist/{name}", ::handlerArtistKotlinXCoroutine)
    }
}

private suspend fun handlerArtistKotlinXCoroutine(req: ServerRequest): ServerResponse {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val body = kotlinXArtistCoroutine(artist.cfAllMusicArtist)

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
        artist.pubAllMusicArtist,
        artist.pubSpotify,
        artist.pubApple
    )

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(body, object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}
