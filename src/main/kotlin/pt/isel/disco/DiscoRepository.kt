package pt.isel.disco

import io.reactivex.rxjava3.core.Observable
import pt.isel.disco.model.*
import java.util.concurrent.TimeUnit

object DiscoRepository {
    var timeout: Long = 2000
}

val artists : Map<String, Artist> = mapOf(
    "onerepublic" to Artist(
        "OneRepublic",
        MusicBrainz("OneRepublic", 2002, "Colorado Springs", listOf("pop rock")),
        SpotifyArtist("OneRepublic",  48389860, listOf("I Ain't Worried", "Counting Stars", "Sunshine", "West Coast", "Someday"), listOf()),
        AppleMusicArtist("OneRepublic", listOf("I Ain't Worried", "Counting Stars", "Secrets", "I Lived", "Apologize"), listOf())
    ),
    "david bowie" to Artist(
        "David Bowie",
        MusicBrainz(artist = "David Bowie", 1960, "New York", listOf("Pop Rock")),
        SpotifyArtist("David Bowie",  16428657, listOf("Heroes", "Starman", "Rebel Rebel", "Space Oddity", "Let's dance"), listOf()),
        AppleMusicArtist("David Bowie", listOf("Under Pressure", "Starman", "Space Oddity", "Let's dance", "Fame"), listOf())
    ),
    "the rolling stones" to Artist(
        "The Rolling Stones",
        MusicBrainz(artist = "The Rolling Stones", 1962, "London", listOf("rock", "blues rock", "classic rock", "psychedelic rock")),
        SpotifyArtist("The Rolling Stones", 20221093, listOf("Paint it Black", "Start Me Up", "Gimme Shelter", "Satisfaction", "Sympathy For The Devil"), listOf()),
        AppleMusicArtist("The Rolling Stones", listOf("Gimme Shelter", "Beast of Burden", "Sympathy For The Devil", "Paint it Black", "Start Me Up"), listOf())
    )
)

val tracks: Observable<Track> = Observable.fromArray(
    Track("Space Oddity"),
    Track("Forest"),
    Track("Just like honey")
).concatMap { Observable.just(it).delay(DiscoRepository.timeout, TimeUnit.MILLISECONDS) }

