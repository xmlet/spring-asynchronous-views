package pt.isel.genius.groovy

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import pt.isel.genius.AppendableWriter
import pt.isel.genius.tracks
import reactor.core.publisher.Mono


fun artistRouterHtmlGroovy(): RouterFunction<ServerResponse> {
    return RouterFunctions
        .route()
        .path("/groovy") { builder ->
            builder
                .GET("/reactive/playlist", ::handlerPlaylist)
        }
        .build()
}


private fun handlerPlaylist(req: ServerRequest): Mono<ServerResponse>  {
    val writer = AppendableWriter {
        ArtistViewGroovy.playlistView(this, tracks);
    }
    return ServerResponse
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(writer.asFlux(), object : ParameterizedTypeReference<String>() {})
}
