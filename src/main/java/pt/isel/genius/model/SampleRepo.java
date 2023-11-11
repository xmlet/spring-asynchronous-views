package pt.isel.genius.model;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.*;

public class SampleRepo {

    public static void resolve() {
        List<Track> tracks = asList(
                new Track("Space Oddity"),
                new Track("Forest"),
                new Track("Just like honey")
        );

    }
}
