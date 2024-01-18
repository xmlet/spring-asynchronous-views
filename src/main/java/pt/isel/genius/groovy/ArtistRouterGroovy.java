package pt.isel.genius.groovy;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pt.isel.genius.AppendableWriter;
import pt.isel.genius.GeniusRepositoryKt;
import reactor.core.publisher.Mono;

public class ArtistRouterGroovy {
    public static RouterFunction<ServerResponse> artistRouterHtmlGroovy() {
        return RouterFunctions
                .route()
                .path("/groovy", builder -> builder
                        .GET("/reactive/playlist", req -> handlerPlaylist(req))
                )
                .build();
    }

    private static Mono<ServerResponse> handlerPlaylist(ServerRequest req) {
        final var out = new AppendableWriter(writer -> {
            ArtistViewGroovy.playlistView(writer, GeniusRepositoryKt.getTracks());
            return null;
        });
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(out.asFlux(), new ParameterizedTypeReference<String>() {});
    }
}
