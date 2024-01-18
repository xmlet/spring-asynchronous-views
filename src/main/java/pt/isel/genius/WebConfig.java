package pt.isel.genius;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import pt.isel.genius.groovy.ArtistRouterGroovy;
import pt.isel.genius.htmlflow.ArtistRouterHtmlFlowKt;
import pt.isel.genius.kotlinx.ArtistRouterKotlinxKt;
import pt.isel.genius.thymeleaf.ArtistRouterThymeleafKt;

@Configuration
@ComponentScan
public class WebConfig {
    @Bean
    public RouterFunction<ServerResponse> routerArtistHtmlFlow() {
        return ArtistRouterHtmlFlowKt.artistRouterHtmlFlow();
    }

    @Bean
    public RouterFunction<ServerResponse> routerArtistKotlinX() {
        return ArtistRouterKotlinxKt.artistCoRouterKotlinX();
    }

    @Bean
    public RouterFunction<ServerResponse> routerArtistThymeleaf() {
        return ArtistRouterThymeleafKt.artistRouterThymeleaf();
    }

    @Bean
    public RouterFunction<ServerResponse> routerArtistGroovy() {
        return ArtistRouterGroovy.artistRouterHtmlGroovy();
    }
}
