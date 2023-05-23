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
    fun handlerArtist(@PathVariable("name") name: String, model: Model): String {
        val artist: Artist = requireNotNull(artists[name.lowercase()]) {
            "No resource for artist name $name"
        }
        model.addAttribute("startTime", System.currentTimeMillis())
        model.addAttribute("artistName", artist.name)
        model.addAttribute("musicBrainz", artist.monoMusicBrainz) // Implicit Call to join() made by Spring
        model.addAttribute("spotify", artist.monoSpotify)
        // model.addAttribute("apple", artist.cfApple)
        return "artist"
    }

    @GetMapping("/reactive/artist/{name}")
    fun reactiveHandlerArtist(@PathVariable("name") name: String, model: Model): String {
        val artist: Artist = requireNotNull(artists[name.lowercase()]) {
            "No resource for artist name $name"
        }
        model.addAttribute("startTime", System.currentTimeMillis())
        model.addAttribute("artistName", artist.name)
        /**
         * !!! Only one data-driver variable is allowed to be specified as a model attribute!!!!
         * Otherwise, it causes TemplateProcessingException.
         */
        model.addAttribute("musicBrainz", ReactiveDataDriverContextVariable(artist.monoMusicBrainz.flux(), 1))
        // model.addAttribute("spotify", ReactiveDataDriverContextVariable(artist.pubSpotify, 1))
        // model.addAttribute("apple", ReactiveDataDriverContextVariable(artist.pubApple, 1))
        return "artistReactive"
    }
}
