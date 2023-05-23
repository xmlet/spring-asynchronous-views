package pt.isel.genius

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import pt.isel.genius.htmlflow.artistRouterHtmlFlow
import pt.isel.genius.kotlinx.artistCoRouterKotlinX
import pt.isel.genius.thymeleaf.artistRouterThymeleaf

@Configuration
@ComponentScan
class WebConfig {
    @Bean
    fun routerArtistHtmlFlow() = artistRouterHtmlFlow()

    @Bean
    fun routerArtistKotlinX() = artistCoRouterKotlinX()

    @Bean
    fun routerArtistThymeleaf() = artistRouterThymeleaf()
}
