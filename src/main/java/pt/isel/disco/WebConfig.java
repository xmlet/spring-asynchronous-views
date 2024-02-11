package pt.isel.disco;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import pt.isel.disco.groovy.ArtistRouterGroovy;
import pt.isel.disco.htmlflow.ArtistRouterHtmlFlowKt;
import pt.isel.disco.kotlinx.ArtistRouterKotlinxKt;
import pt.isel.disco.thymeleaf.ArtistRouterThymeleafKt;

@Configuration
@ComponentScan
public class WebConfig {
    @Bean
    public RouterFunction<ServerResponse> routerArtistHtmlFlow() {
        return ArtistRouterHtmlFlowKt.artistRouterHtmlFlow();
    }

    @Bean
    public RouterFunction<ServerResponse> corouterArtistHtmlFlow() {
        return ArtistRouterHtmlFlowKt.artistCoRouterHtmlFlow();
    }

    @Bean
    public RouterFunction<ServerResponse> routerArtistKotlinX() {
        return ArtistRouterKotlinxKt.artistCoRouterKotlinX();
    }

    @Bean
    public RouterFunction<ServerResponse> routerDemoKotlinX() {
        return DslDemoRouter.dslDemoRouter();
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
