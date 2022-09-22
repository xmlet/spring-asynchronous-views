package pt.isel.genius.kotlinx

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import pt.isel.genius.artists
import pt.isel.genius.model.Artist

fun artistCoRouterKotlinX(): RouterFunction<ServerResponse> {
    return coRouter {
        GET("/kotlinx/coroutine/artist/{name}", ::reactiveHandlerArtistKotlinX)
    }
}

private suspend fun reactiveHandlerArtistKotlinX(req: ServerRequest): ServerResponse {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val body = coKotlinXArtistReactive(artist.cfAllMusicArtist)

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(body, object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}
