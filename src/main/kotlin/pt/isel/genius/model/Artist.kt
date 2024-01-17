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
    companion object {
        var timeout: Long = 2
    }

    fun monoMusicBrainz() = Mono
        .fromSupplier { musicBrainz }
        .delayElement(Duration.of(timeout, SECONDS))
        .toFuture()


    fun monoSpotify() = Mono
        .fromSupplier { spotify }
        .delayElement(Duration.of(timeout + 1, SECONDS))
        .toFuture()

    fun monoApple() = Mono
        .fromSupplier { apple }
        .delayElement(Duration.of(timeout + 2, SECONDS))
        .toFuture()

}
