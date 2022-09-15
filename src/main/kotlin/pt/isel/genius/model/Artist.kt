package pt.isel.genius.model

import java.util.concurrent.CompletableFuture

data class Artist(private val allMusic: AllMusicArtist, private val spotify: SpotifyArtist, private val apple: AppleMusicArtist) {
    val cfAllMusicArtist
        get() = CompletableFuture.completedFuture(allMusic).delay(2000)

    val cfSpotify
        get() = CompletableFuture.completedFuture(spotify).delay(2000)

    val cfApple
        get() = CompletableFuture.completedFuture(apple).delay(2000)
}


private fun <T> CompletableFuture<T>.delay(ms: Long): CompletableFuture<T> {
    return this.thenCompose { v ->
        CompletableFuture.supplyAsync {
            Thread.sleep(ms)
            v
        }
    }
}
