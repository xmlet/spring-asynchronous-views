package pt.isel.genius.thymeleaf

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
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
}
