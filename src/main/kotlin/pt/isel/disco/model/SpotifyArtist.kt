package pt.isel.disco.model

class SpotifyArtist(
    val artist: String,
    val monthlyListeners: Int,
    val popularSongs: List<String>,
    val albums: List<String>)
