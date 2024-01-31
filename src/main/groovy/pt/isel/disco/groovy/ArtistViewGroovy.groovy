package pt.isel.disco.groovy

import groovy.xml.MarkupBuilder
import io.reactivex.rxjava3.core.Observable
import pt.isel.disco.model.Track

static void playlistView(Writer out, Observable<Track> tracks) {
    def html = new MarkupBuilder(new PrintWriter(out))
    html.html {
        head { title("Playlist") }
        body {
            table(border: "1") {
                tr { th("Track name") }
                tracks
                    .doOnNext { track ->
                        tr { td(track.name) }
                    }
                    .doOnComplete { out.close() }
                    .subscribe()
            } // table
        } // body
    } // html
}