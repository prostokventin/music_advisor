package advisor;

import java.util.List;

public class Album extends Item {

    public Album(String title, List<Artist> artists, String link) {
        this.title = title;
        this.artists = artists;
        this.link = link;
    }

    @Override
    public String toString() {
        return
                new StringBuilder()
                        .append(title + "\n")
                        .append(artists + "\n")
                        .append(link + "\n")
                        .toString();
    }
}
