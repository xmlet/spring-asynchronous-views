package pt.isel.genius.model

import reactor.core.publisher.Mono
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

data class Artist(val allMusic: AllMusicArtist, val spotify: SpotifyArtist, val apple: AppleMusicArtist) {
    val monoAllMusicArtist
        get() = Mono.just(allMusic).delayElement(Duration.of(2, SECONDS))

    val monoSpotify
        get() = Mono.just(spotify).delayElement(Duration.of(2, SECONDS))

    val monoApple
        get() = Mono.just(apple).delayElement(Duration.of(2, SECONDS))
}
