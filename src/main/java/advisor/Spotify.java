package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Spotify {

    private final String CLIENT_ID = "77cba4defa9c4b858554988385a439e6";
    private final String CLIENT_SECRET = "1b6129d81f5d457eb1e5ed357959228b";
    private final String REDIRECT_URI = "http://localhost:8080";
    private final String NEW_REALESES_URL_PART = "/v1/browse/new-releases";
    private final String CATEGORIES_URL_PART = "/v1/browse/categories";
    private final String FEATURED_URL_PART = "/v1/browse/featured-playlists";
    private final String CATEGORY_PLAYLISTS_URL_PART = "/v1/browse/categories/%s/playlists";

    private HttpServer server;

    private String accessLink;
    private String resourceLink;
    private String accessCode;
    private String accessToken;

    private static Spotify instance;

    public boolean isAccessCodeReceived = false;

    private Spotify(String accessLink, String resourceLink) {
        this.accessLink = accessLink;
        this.resourceLink = resourceLink;
    }

    public static Spotify getInstance(String accessLink, String resourceLink) {
        if (instance == null) {
            instance = new Spotify(accessLink, resourceLink);
        }
        return instance;
    }

    public String getAccessCodeLink() {
        return
                accessLink +
                        "/authorize?client_id=" +
                        CLIENT_ID + "&redirect_uri=" +
                        REDIRECT_URI + "" +
                        "&response_type=code";
    }

    private String getHeaderAuthorization() {
        return
                "Basic " +
                        Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
    }


    public void startServer() throws IOException, InterruptedException {

        server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String responseQuery = exchange.getRequestURI().getQuery();
                String response = "Authorization code not found. Try again.";
                if (responseQuery != null && responseQuery.contains("code")) {
                    isAccessCodeReceived = true;
                    accessCode = responseQuery.split("=")[1];
                    response = "Got the code. Return back to your program.";
                }
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            }
        });

        server.start();

    }

    public void stopServer() {
        server.stop(5);
    }

    public void getAccessToken() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-type", "application/x-www-form-urlencoded")
                .header("Authorization", getHeaderAuthorization())
                .uri(URI.create(accessLink + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        String.format(
                                "code=%s&grant_type=authorization_code&redirect_uri=%s",
                                accessCode,
                                REDIRECT_URI
                        )))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
        accessToken = jo.get("access_token").getAsString();
//        System.out.println(accessCode);
//        System.out.println(accessToken);
    }

    private HttpResponse<String> spotifyHttpRequest(String urlPart) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(resourceLink + urlPart))
                .GET()
                .build();

        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
//        return JsonParser.parseString(response.body()).getAsJsonObject();
        return response;
    }

    public List<Item> getNewReleases() throws IOException, InterruptedException {
        HttpResponse<String> response = spotifyHttpRequest(NEW_REALESES_URL_PART);
        JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject albums = jo.get("albums").getAsJsonObject();
        List<Item> items = new ArrayList<>();
        for (JsonElement item : albums.getAsJsonArray("items")) {
            String name = item.getAsJsonObject().get("name").getAsString();
            String link = item.getAsJsonObject()
                    .get("external_urls")
                    .getAsJsonObject()
                    .get("spotify").getAsString();
            List<Artist> artists = new ArrayList<>();
            for (JsonElement artist : item.getAsJsonObject().getAsJsonArray("artists")) {
                Artist newArtist = new Artist(artist.getAsJsonObject().get("name").getAsString());
                artists.add(newArtist);
            }
            Album album = new Album(name, artists, link);
            items.add(album);
        }
        return items;
    }

    public List<Item> getCategories() throws IOException, InterruptedException {
        HttpResponse<String> response = spotifyHttpRequest(CATEGORIES_URL_PART);
        JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject categories = jo.get("categories").getAsJsonObject();
        List<Item> items = new ArrayList<>();
        for (JsonElement item : categories.getAsJsonArray("items")) {
            String id = item.getAsJsonObject().get("id").getAsString();
            String name = item.getAsJsonObject().get("name").getAsString();
            Category category = new Category(id, name);
            items.add(category);
        }
        return items;
    }

    public List<Item> getFeaturedPlaylists() throws IOException, InterruptedException {
        HttpResponse<String> response = spotifyHttpRequest(FEATURED_URL_PART);
        JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject playlists = jo.get("playlists").getAsJsonObject();
        List<Item> items = new ArrayList<>();
        for (JsonElement item : playlists.getAsJsonArray("items")) {
            String name = item.getAsJsonObject().get("name").getAsString();
            String link = item.getAsJsonObject()
                    .get("external_urls")
                    .getAsJsonObject()
                    .get("spotify").getAsString();
            Playlist playlist = new Playlist(name, link);
            items.add(playlist);
        }
        return items;
    }

    public List<Item> getCategoryPlaylists(String categoryName) throws IOException, InterruptedException {

        String categoryId = "";
        List<Item> categories = this.getCategories();
        for (Item item : categories) {
            System.out.println(item.toString());
            if (item.name.equals(categoryName)) {
                System.out.println("Нашли");
                System.out.println(item.id);
                categoryId = item.id;
                break;
            }
//            throw new IOException("Unknown category name.");
        }

        if (categoryId == null) {
            String message = "Unknown category name.";
            System.out.println(message);
            return null;
        }

        HttpResponse<String> response = spotifyHttpRequest(String.format(CATEGORY_PLAYLISTS_URL_PART, categoryId));
        JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();
        if (jo.toString().contains("\"status\":404")) {
            String message = jo.get("error").getAsJsonObject().get("message").getAsString();
            System.out.println(message);
            return null;
        }
        System.out.println(jo.toString());
        JsonObject playlists = jo.get("playlists").getAsJsonObject();
        List<Item> items = new ArrayList<>();
        for (JsonElement item : playlists.getAsJsonArray("items")) {
            String name = item.getAsJsonObject().get("name").getAsString();
            String link = item.getAsJsonObject()
                    .get("external_urls")
                    .getAsJsonObject()
                    .get("spotify").getAsString();
            Playlist playlist = new Playlist(name, link);
            items.add(playlist);
        }
        return items;
    }


}
