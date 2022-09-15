package pt.isel.genius.thymeleaf

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable
import pt.isel.genius.artists
import pt.isel.genius.model.Artist

@Controller
@RequestMapping("/thymeleaf")
class ArtistControllerThymeleaf {

    @GetMapping("/blocking/artist/{name}")
    fun blockingHandlerArtist(@PathVariable("name") name: String, model: Model): String {
        val artist: Artist = requireNotNull(artists[name.lowercase()]) {
            "No resource for artist name $name"
        }
        model.addAttribute("startTime", System.currentTimeMillis())
        model.addAttribute("artist", name)
        model.addAttribute("allMusic", artist.cfAllMusicArtist.join())
        model.addAttribute("spotify", artist.cfSpotify.join())
        model.addAttribute("apple", artist.cfApple.join())
        return "artist"
    }

    @GetMapping("/reactive/artist/{name}")
    fun reactiveHandlerArtist(@PathVariable("name") name: String, model: Model): String {
        val artist: Artist = requireNotNull(artists[name.lowercase()]) {
            "No resource for artist name $name"
        }
        model.addAttribute("startTime", System.currentTimeMillis())
        model.addAttribute("artist", name)
        /**
         * !!! Only one data-driver variable is allowed to be specified as a model attribute!!!!
         * Otherwise, it causes TemplateProcessingException.
         */
        // model.addAttribute("allMusic", ReactiveDataDriverContextVariable(artist.pubAllMusicArtist, 1))
        // model.addAttribute("spotify", ReactiveDataDriverContextVariable(artist.pubSpotify, 1))
        model.addAttribute("apple", ReactiveDataDriverContextVariable(artist.pubApple, 1))
        return "artistReactive"
    }
}
