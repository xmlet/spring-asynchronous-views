package pt.isel.genius.model

data class SpotifyArtist(val artist: String, val monthlyListeners: Int, val popularSongs: List<String>, val albums: List<String>)
