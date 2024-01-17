package pt.isel.genius.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.springframework.boot.SpringApplication.run
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.bindToApplicationContext
import pt.isel.genius.GeniusApplication
import pt.isel.genius.GeniusRepository
import pt.isel.genius.model.Artist
import java.lang.System.lineSeparator
import java.net.URI
import java.time.Duration

private val webTestClient: WebTestClient = bindToApplicationContext(run(GeniusApplication::class.java))
    .configureClient()
    .responseTimeout(Duration.ofMinutes(1))
    .build()

class TestArtistView {

    constructor() {
        Artist.timeout = 10
        GeniusRepository.timeout = 10
    }

    private fun request(path: String): String {
        val arr: ByteArray? = webTestClient
            .get()
            .uri(URI.create(path))
            .accept(MediaType.ALL)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .returnResult()
            .responseBody
        return String(arr!!)
    }

    /*========================================================================*/
    /*---------------------           THYMELEAF          ---------------------*/
    /*========================================================================*/
    @Test
    fun testThymelafArtistBlocking() {
        val html = request("/thymeleaf/blocking/artist/the%20rolling%20stones")
        val expected = "<!DOCTYPE html>${lineSeparator()}$expectedWellFormed"
        assertHtml(expected, html)
    }

    @Test
    fun testThymelafArtistReactive() {
        val html = request("/thymeleaf/reactive/artist/the%20rolling%20stones")
        assertHtml(expectedThymeleafReactive, html)
    }

    @Test
    fun testThymeleafPlaylistReactiveWellFormed() {
        val html = request("/thymeleaf/reactive/playlist")
        assertHtml(expectedPlaylistWellFormed, html)
    }
    /*========================================================================*/
    /*---------------------            KotlinX           ---------------------*/
    /*========================================================================*/
    @Test
    fun testKotlinXArtistBlocking() {
        val html = request("/kotlinx/blocking/artist/the%20rolling%20stones")
        assertHtml(expectedWellFormed, html)
    }
    @Test
    fun testKotlinXArtistReactive() {
        val html = request("/kotlinx/reactive/artist/the%20rolling%20stones")
        assertHtml(expectedKotlinXReactive, html)
    }
    @Test
    fun testKotlinXPlaylistReactiveIllFormed() {
        val html = request("/kotlinx/reactive/playlist")
        assertHtml(expectedPlaylistIllFormed, html)
    }
    /*========================================================================*/
    /*---------------------            Groovy            ---------------------*/
    /*========================================================================*/
    @Test
    fun testGroovyPlaylistReactiveIllFormed() {
        val html = request("/groovy/reactive/playlist")
        assertHtml(expectedPlaylistIllFormed, html)
    }
    /*========================================================================*/
    /*---------------------           HtmlFlow           ---------------------*/
    /*========================================================================*/
    @Test
    fun testHtmlFlowArtistBlocking() {
        val html = request("/htmlflow/blocking/artist/the%20rolling%20stones")
        val expected = "<!DOCTYPE html>${lineSeparator()}$expectedWellFormed"
        assertHtml(expected, html)
    }

    @Test
    fun testHtmlFlowArtistReactive() {
        val html = request("/htmlflow/reactive/artist/the%20rolling%20stones")
        val expected = "<!DOCTYPE html>${lineSeparator()}$expectedWellFormed"
        assertHtml(expected, html)
    }

    @Test
    fun testHtmlFlowArtistAsyncView() {
        val html = request("/htmlflow/asyncview/artist/the%20rolling%20stones")
        val expected = "<!DOCTYPE html>${lineSeparator()}$expectedWellFormed"
        assertHtml(expected, html)
    }

    @Test
    fun testHtmlFlowArtistSuspending() {
        val html = request("/htmlflow/suspending/artist/the%20rolling%20stones")
        val expected = "<!DOCTYPE html>${lineSeparator()}$expectedWellFormed"
        assertHtml(expected, html)
    }

    @Test
    fun testHtmlFlowPlaylistReactiveIllFormed() {
        val html = request("/htmlflow/reactive/playlist")
        val expected = "<!DOCTYPE html>${lineSeparator()}$expectedPlaylistIllFormed"
        assertHtml(expected, html)
    }

    @Test
    fun testHtmlFlowPlaylistReactiveWellFormed() {
        val html = request("/htmlflow/suspending/playlist")
        val expected = "<!DOCTYPE html>${lineSeparator()}$expectedPlaylistWellFormed"
        assertHtml(expected, html)
    }

    private fun assertHtml(expected: String, html: String) {
        val actual= html
            .replace("<".toRegex(), "${lineSeparator()}$0")
            .replace(">".toRegex(), "$0${lineSeparator()}")
            .replace("'", "\"")
            .split(lineSeparator())
            .filter { !it.contains("(response handling time)") } // Skip footer with time in milliseconds
            .map(String::trim)
            .filter { it.isNotEmpty() }
            .iterator()
        expected
            .split(lineSeparator())
            .filter { !it.contains("(response handling time)") } // Skip footer with time in milliseconds
            .map(String::trim)
            .forEach {
//                println(it)
                assertEquals(it, actual.next())
            }
        assertFalse(actual.hasNext())
    }
}

private const val expectedWellFormed = """<html>
<body>
<div>
<h3>
The Rolling Stones
</h3>
<hr>
<h3>
MusicBrainz info:
</h3>
<ul>
<li>
Founded: 1962
</li>
<li>
From: London
</li>
<li>
Genre: rock, blues rock, classic rock, psychedelic rock
</li>
</ul>
<hr>
<b>
Spotify popular tracks:
</b>
<span>
Paint it Black, Start Me Up, Gimme Shelter, Satisfaction, Sympathy For The Devil 
</span>
</div>
<hr>
<footer>
<small>
3177ms (response handling time)
</small>
</footer>
</body>
</html>"""

private const val expectedThymeleafReactive = """<!DOCTYPE html>
<html>
    <body>
		<div>
			<h3>
                The Rolling Stones
            </h3>
			<hr>
			<h3>
                MusicBrainz info:
            </h3>
			<ul>
				<li>
                    Founded: 1962
                </li>
				<li>
                    From: London
                </li>
				<li>
                    Genre: rock, blues rock, classic rock, psychedelic rock
                </li>
			</ul>
			<hr>
			<b>
                Spotify popular tracks:
            </b>
			<hr>
			<b>
                Apple Music top songs:
            </b>
        </div>
        <hr>
		<footer>
			<small>
                3177ms (response handling time)
            </small>
		</footer>
	</body>
</html>"""

private const val expectedKotlinXReactive = """<html>
  <body>
    <div>
      <h3>
        the rolling stones
      </h3>
      <hr>
      <h3>
        MusicBrainz info:
      </h3>
      <ul>
      </ul>
    </div>
  </body>
</html>
<li>
    Founded: 1962
</li>
<li>
    From: London
</li>
<li>
    Genres: rock, blues rock, classic rock, psychedelic rock
</li>
<hr>
<b>
    Spotify popular tracks:
</b>
<span>
</span>
Paint it Black, Start Me Up, Gimme Shelter, Satisfaction, Sympathy For The Devil
<hr>
<footer>
    <small>
        3177ms (response handling time)
    </small>
</footer>"""

private const val expectedPlaylistIllFormed = """<html>
	<head>
		<title>
			Playlist
		</title>
	</head>
	<body>
		<table border="1">
			<tr>
				<th>
					Track name
				</th>
			</tr>
		</table>
	</body>
</html>
		<tr>
			<td>
				Space Oddity
			</td>
		</tr>
		<tr>
			<td>
				Forest
			</td>
		</tr>
		<tr>
			<td>
				Just like honey
			</td>
		</tr>"""

private const val expectedPlaylistWellFormed = """<html>
	<head>
		<title>
			Playlist
		</title>
	</head>
	<body>
		<table border="1">
			<tr>
				<th>
					Track name
				</th>
			</tr>
            <tr>
                <td>
                    Space Oddity
                </td>
            </tr>
            <tr>
                <td>
                    Forest
                </td>
            </tr>
            <tr>
                <td>
                    Just like honey
                </td>
            </tr>
		</table>
	</body>
</html>"""