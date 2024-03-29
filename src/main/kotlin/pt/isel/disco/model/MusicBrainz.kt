package pt.isel.disco.model

class MusicBrainz(
    val artist: String,
    val year: Int,
    val from: String,
    genresList: List<String>)
{
    val genres = genresList.joinToString(", ")
}
