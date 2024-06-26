package pt.isel.disco.kotlinx

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.reactivestreams.Publisher
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.*
import pt.isel.disco.AppendableSink
import pt.isel.disco.artists
import pt.isel.disco.model.Artist
import pt.isel.disco.tracks
import reactor.core.publisher.Mono.fromFuture

fun artistCoRouterKotlinX(): RouterFunction<ServerResponse> {
    return coRouter {
        GET("/kotlinx/blocking/artist/{name}", ::handlerArtistKotlinXBlocking)
        GET("/kotlinx/reactive/artist/{name}", ::handlerArtistKotlinXReactive)
        GET("/kotlinx/reactive/playlist", ::handlerJatl)
        GET("/kotlinx/coroutine/artist/{name}", ::handlerArtistKotlinXCoroutine)
    }
}

private suspend fun handlerArtistKotlinXCoroutine(req: ServerRequest): ServerResponse {
    val name = req.pathVariable("name")
    val artist: Artist = requireNotNull(artists[name.lowercase()]) {
        "No resource for artist name $name"
    }
    val body = kotlinXArtistCoroutine(fromFuture(artist.monoMusicBrainz()))

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
        artist.monoMusicBrainz(),
        artist.monoSpotify(),
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
        artist.monoMusicBrainz(),
        artist.monoSpotify(),
        artist.monoApple()
    )

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(body, object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}

private suspend fun handlerJatl(req: ServerRequest): ServerResponse {
    val view: AppendableSink = AppendableSink().apply {
        appendHTML()
            .html {
                head { title("Playlist") }
                body {
                    table {
                        attributes["border"] = "1"
                        tr { th { text("Track name") } }
                        tracks
                            .doOnNext { track ->
                                tr { td { text(track.name) } }
                            }
                            .doOnComplete { close() }
                            .subscribe()
                    } // table
                } // body
            } // html
        }

    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(view.asFlux(), object : ParameterizedTypeReference<String>() {})
        .awaitSingle()
}
