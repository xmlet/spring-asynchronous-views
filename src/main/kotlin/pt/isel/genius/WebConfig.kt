package pt.isel.genius

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import pt.isel.genius.htmlflow.artistRouterHtmlFlow

@Configuration
@ComponentScan
class WebConfig {
    @Bean
    fun routerTracksHtmlFlow() = artistRouterHtmlFlow()
}
