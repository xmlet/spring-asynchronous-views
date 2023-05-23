package pt.isel.genius.model

import reactor.core.publisher.Mono
import java.time.Duration
import java.time.temporal.ChronoUnit.SECONDS

data class Artist(
    val name: String,
    private val musicBrainz: MusicBrainz,
    private val spotify: SpotifyArtist,
    private val apple: AppleMusicArtist
) {
    val monoMusicBrainz
        get() = Mono.fromSupplier { musicBrainz }.delayElement(Duration.of(2, SECONDS))

    val monoSpotify
        get() = Mono.fromSupplier { spotify }.delayElement(Duration.of(3, SECONDS))

    val monoApple
        get() = Mono.fromSupplier { apple }.delayElement(Duration.of(4, SECONDS))

    val pubMusicBrainz
        get() = monoMusicBrainz.flux()

    val pubSpotify
        get() = monoSpotify.flux()

    val pubApple
        get() = monoApple.flux()
}
