package pt.isel.genius.model

import reactor.core.publisher.Mono
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

data class Artist(
    val name: String,
    private val allMusic: AllMusicArtist,
    private val spotify: SpotifyArtist,
    private val apple: AppleMusicArtist
) {
    val cfAllMusicArtist
        get() = Mono.fromSupplier { allMusic }.delayElement(Duration.of(2, SECONDS))

    val cfSpotify
        get() = Mono.fromSupplier { spotify }.delayElement(Duration.of(2, SECONDS))

    val cfApple
        get() = Mono.fromSupplier { apple }.delayElement(Duration.of(2, SECONDS))

    val pubAllMusicArtist
        get() = cfAllMusicArtist.flux()

    val pubSpotify
        get() = cfSpotify.flux()

    val pubApple
        get() = cfApple.flux()
}
