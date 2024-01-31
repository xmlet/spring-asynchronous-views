package pt.isel.disco.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pt.isel.disco.DiscoApplication;
import pt.isel.disco.DiscoRepository;
import pt.isel.disco.model.Artist;

import java.net.URI;
import java.time.Duration;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestArtistView {

    private static final WebTestClient webTestClient = WebTestClient
            .bindToApplicationContext(SpringApplication.run(DiscoApplication.class))
            .configureClient()
            .responseTimeout(Duration.ofMinutes(1))
            .build();

    public TestArtistView() {
        Artist.Companion.setTimeout(10);
        DiscoRepository.INSTANCE.setTimeout(10);
    }

    private static String request(String path) {
        byte[] arr = webTestClient
                .get()
                .uri(URI.create(path))
                .accept(MediaType.ALL)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .returnResult()
                .getResponseBody();
        return new String(arr);
    }

    /*========================================================================*/
    /*---------------------           THYMELEAF          ---------------------*/
    /*========================================================================*/
    @Test
    public void testThymelafArtistBlocking() {
        final var html = request("/thymeleaf/blocking/artist/the%20rolling%20stones");
        final var expected = "<!DOCTYPE html>" + lineSeparator() + expectedWellFormed();
        assertHtml(expected, html);
    }

    @Test
    public void testThymelafArtistReactive() {
        final var html = request("/thymeleaf/reactive/artist/the%20rolling%20stones");
        assertHtml(expectedThymeleafReactive(), html);
    }

    @Test
    public void testThymeleafPlaylistReactiveWellFormed() {
        final var html = request("/thymeleaf/reactive/playlist");
        assertHtml(expectedPlaylistWellFormed(), html);
    }
    /*========================================================================*/
    /*---------------------            KotlinX           ---------------------*/
    /*========================================================================*/
    @Test
    public void testKotlinXArtistBlocking() {
        final var html = request("/kotlinx/blocking/artist/the%20rolling%20stones");
        assertHtml(expectedWellFormed(), html);
    }
    @Test
    public void testKotlinXArtistReactive() {
        final var html = request("/kotlinx/reactive/artist/the%20rolling%20stones");
        assertHtml(expectedKotlinXReactive(), html);
    }
    @Test
    public void testKotlinXPlaylistReactiveIllFormed() {
        final var html = request("/kotlinx/reactive/playlist");
        assertHtml(expectedPlaylistIllFormed(), html);
    }
    /*========================================================================*/
    /*---------------------            Groovy            ---------------------*/
    /*========================================================================*/
    @Test
    public void testGroovyPlaylistReactiveIllFormed() {
        final var html = request("/groovy/reactive/playlist");
        assertHtml(expectedPlaylistIllFormed(), html);
    }
    /*========================================================================*/
    /*---------------------           HtmlFlow           ---------------------*/
    /*========================================================================*/
    @Test
    public void testHtmlFlowArtistBlocking() {
        final var html = request("/htmlflow/blocking/artist/the%20rolling%20stones");
        final var expected = "<!DOCTYPE html>" + lineSeparator() + expectedWellFormed();
        assertHtml(expected, html);
    }

    @Test
    public void testHtmlFlowArtistReactive() {
        final var html = request("/htmlflow/reactive/artist/the%20rolling%20stones");
        final var expected = "<!DOCTYPE html>" + lineSeparator() + expectedWellFormed();
        assertHtml(expected, html);
    }

    @Test
    public void testHtmlFlowArtistAsyncView() {
        final var html = request("/htmlflow/asyncview/artist/the%20rolling%20stones");
        final var expected = "<!DOCTYPE html>" + lineSeparator() + expectedWellFormed();
        assertHtml(expected, html);
    }

    @Test
    public void testHtmlFlowArtistSuspending() {
        final var html = request("/htmlflow/suspending/artist/the%20rolling%20stones");
        final var expected = "<!DOCTYPE html>" + lineSeparator() + expectedWellFormed();
        assertHtml(expected, html);
    }

    @Test
    public void testHtmlFlowPlaylistReactiveIllFormed() {
        final var html = request("/htmlflow/reactive/playlist");
        final var expected = "<!DOCTYPE html>" + lineSeparator() + expectedPlaylistIllFormed();
        assertHtml(expected, html);
    }

    @Test
    public void testHtmlFlowPlaylistReactiveWellFormed() {
        final var html = request("/htmlflow/suspending/playlist");
        final var expected = "<!DOCTYPE html>" + lineSeparator() + expectedPlaylistWellFormed();
        assertHtml(expected, html);
    }

    private void assertHtml(String expected, String html) {
        String[] actual = stream(html
                .replace("<", lineSeparator() + "<")
                .replace(">", ">" + lineSeparator())
                .replace("'", "\"")
                .split(lineSeparator()))
                .filter(s -> !s.contains("(response handling time)"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        String[] expectedLines = stream(expected
                .split(lineSeparator()))
                .filter(s -> !s.contains("(response handling time)"))
                .map(String::trim)
                .toArray(String[]::new);

        for (int i = 0; i < expectedLines.length; i++) {
            assertEquals(expectedLines[i], actual[i]);
        }

        assertFalse(actual.length > expectedLines.length);
    }

    private static final String expectedWellFormed() {
        return """
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
           </html>""";
    }

    private static final String expectedThymeleafReactive() {
        return """
                <!DOCTYPE html>
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
                </html>""";
    }

    private static final String expectedKotlinXReactive() {
        return """
                <html>
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
                </footer>""";
    }

    private static final String expectedPlaylistIllFormed() {
        return """
            <html>
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
                    </tr>""";
    }

    private static final String expectedPlaylistWellFormed() {
        return """
               <html>
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
               </html>""";
    }
}
