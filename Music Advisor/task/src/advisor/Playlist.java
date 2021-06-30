package advisor;

public class Playlist extends Item {


    public Playlist(String name, String link) {
        this.name = name;
        this.link = link;
    }

    @Override
    public String toString() {
        return
                new StringBuilder()
                        .append(name + "\n")
                        .append(link + "\n")
                        .toString();
    }
}
