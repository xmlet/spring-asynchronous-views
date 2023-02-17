package pt.isel.genius

import pt.isel.genius.model.AppleMusicArtist
import pt.isel.genius.model.Artist
import pt.isel.genius.model.AllMusicArtist
import pt.isel.genius.model.SpotifyArtist

val artists : Map<String, Artist> = mapOf(
    "onerepublic" to Artist(
        "OneRepublic",
        AllMusicArtist("OneRepublic", 2002, "Colorado Springs", "Pop Rock"),
        SpotifyArtist("OneRepublic",  48389860, listOf("I Ain't Worried", "Counting Stars", "Sunshine", "West Coast", "Someday"), listOf()),
        AppleMusicArtist("OneRepublic", listOf("I Ain't Worried", "Counting Stars", "Secrets", "I Lived", "Apologize"), listOf())
    ),
    "david bowie" to Artist(
        "David Bowie",
        AllMusicArtist(artist = "David Bowie", 1960, "New York", "Pop Rock"),
        SpotifyArtist("David Bowie",  16428657, listOf("Heroes", "Starman", "Rebel Rebel", "Space Oddity", "Let's dance"), listOf()),
        AppleMusicArtist("David Bowie", listOf("Under Pressure", "Starman", "Space Oddity", "Let's dance", "Fame"), listOf())
    ),
    "the rolling stones" to Artist(
        "The Rolling Stones",
        AllMusicArtist(artist = "The Rolling Stones", 1962, "London", "Pop Rock"),
        SpotifyArtist("The Rolling Stones", 20221093, listOf("Paint it, Black", "Start Me Up", "Gimme Shelter", "Satisfaction", "Sympathy For The Devil"), listOf()),
        AppleMusicArtist("The Rolling Stones", listOf("Gimme Shelter", "Beast of Burden", "Sympathy For The Devil", "Paint it, Black", "Start Me Up"), listOf())
    )
)
