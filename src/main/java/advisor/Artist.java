package advisor;

public class Artist {
    public String name;

    public Artist(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
