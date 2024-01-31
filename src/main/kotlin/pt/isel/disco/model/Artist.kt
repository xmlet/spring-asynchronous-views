package pt.isel.disco.model

import reactor.core.publisher.Mono
import java.time.Duration
import java.time.temporal.ChronoUnit.MILLIS

data class Artist(
    val name: String,
    private val musicBrainz: MusicBrainz,
    private val spotify: SpotifyArtist,
    private val apple: AppleMusicArtist
) {
    companion object {
        var timeout: Long = 2000
    }

    fun monoMusicBrainz() = Mono
        .fromSupplier { musicBrainz }
        .delayElement(Duration.of(timeout, MILLIS))
        .toFuture()


    fun monoSpotify() = Mono
        .fromSupplier { spotify }
        .delayElement(Duration.of(timeout + 1000, MILLIS))
        .toFuture()

    fun monoApple() = Mono
        .fromSupplier { apple }
        .delayElement(Duration.of(timeout + 2000, MILLIS))
        .toFuture()

}
