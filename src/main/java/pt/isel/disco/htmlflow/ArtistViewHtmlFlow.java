package pt.isel.disco.htmlflow;

import htmlflow.HtmlFlow;
import htmlflow.HtmlView;
import org.xmlet.htmlapifaster.EnumBorderType;
import pt.isel.disco.model.AppleMusicArtist;
import pt.isel.disco.model.MusicBrainz;
import pt.isel.disco.model.SpotifyArtist;
import pt.isel.disco.model.Track;

import java.util.List;


public class ArtistViewHtmlFlow {

    void playlistView(Appendable out, List<Track> tracks) {
        HtmlFlow
            .doc(out)
            .html()
                .head()
                .title( ).text("Playlist").__()
                .__()
            .body()
                .table().attrBorder(EnumBorderType._1)
                    .tr().th().text("Track name").__().__()
                    .of(table -> tracks
                            .forEach(track -> table
                                    .tr()
                                    .td().text(track.getName()).__()
                                    .__()
                            )
                    )
                .__() // table
            .__() // body
        .__(); // html
    }


    private static final HtmlView artistView = HtmlFlow.view(page -> page
  .html()
    .body()
      .div()
        .h3().<Artist>dynamic((h3, m) -> h3.text(m.artistName)).__()
        .hr().__()
        .h3().text("MusicBrainz info:").__()
        .ul()
          .<Artist>dynamic((ul, m) -> ul
            .li().text("Founded: " + m.musicBrainz.getYear()).__()
            .li().text("From: " + m.musicBrainz.getFrom()).__()
            .li().text("Genre: " + m.musicBrainz.getGenres()).__()
          )
        .__() // ul
        .hr().__()
        .b().text("Spotify popular tracks:").__()
        .span()
          .<Artist>dynamic((span, m) -> span
            .text(String.join(", ", m.spotify.getPopularSongs()))
          )
        .__() // span
      .__() // div
    .__() // body
  .__() // html
);

    private static class Artist {
        public final String artistName;
        public final MusicBrainz musicBrainz;
        public final SpotifyArtist spotify;
        public final AppleMusicArtist apple;
        public long startTime;

        private Artist(String artistName, MusicBrainz musicBrainz, SpotifyArtist spotify, AppleMusicArtist apple) {
            this.artistName = artistName;
            this.musicBrainz = musicBrainz;
            this.spotify = spotify;
            this.apple = apple;
        }
    }
}
