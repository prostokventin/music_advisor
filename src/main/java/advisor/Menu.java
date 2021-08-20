package advisor;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Menu {

    private Spotify spotify;
    private Scanner scanner;
    private int page;
    private boolean accessed = false;

    private List<Item> items = null;
    private int pageNumber = 0;
    private int pageCount = 0;


    public Menu(Spotify spotify, Scanner scanner, int page) {
        this.spotify = spotify;
        this.scanner = scanner;
        this.page = page;
    }

    public void showFeatured() throws IOException, InterruptedException {
        if (!accessed) {
            showAuthMessage();
            return;
        }
        items = spotify.getFeaturedPlaylists();
        pageNumber = 0;
        pageCount = items.size() / page;
        printNextPage();
    }

    public void showNew() throws IOException, InterruptedException {
        if (!accessed) {
            showAuthMessage();
            return;
        }
        items = spotify.getNewReleases();
        pageNumber = 0;
        pageCount = items.size() / page;
        printNextPage();

    }
    public void showCategories() throws IOException, InterruptedException {
        if (!accessed) {
            showAuthMessage();
            return;
        }
        items = spotify.getCategories();
        pageNumber = 0;
        pageCount = items.size() / page;
        printNextPage();
    }

    public void showPlaylists(String categoryName) throws IOException, InterruptedException {
        if (!accessed) {
            showAuthMessage();
            return;
        }
        items = spotify.getCategoryPlaylists(categoryName);
        if (items == null) {
            return;
        }
        pageNumber = 0;
        pageCount = items.size() / page;
        printNextPage();
    }

    private void showAuthMessage() {
        System.out.println("Please, provide access for application.");
    }

    public void Auth() throws IOException, InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.println(spotify.getAccessCodeLink());
        spotify.startServer();
        System.out.println("waiting for code...");
        while(!spotify.isAccessCodeReceived) {
            Thread.sleep(1000);
        }
        System.out.println("code received");
        System.out.println("making http request for access_token...");
        spotify.getAccessToken();
        System.out.println("success!");
        spotify.stopServer();
        this.accessed = true;
    }

    public void showExit() throws IOException {
        System.out.println("---GOODBYE!---");
    }

    public void printNextPage() {
        if (!accessed && items == null) {
            return;
        }
        if (pageNumber == pageCount) {
            System.out.println("No more pages.");
            return;
        }
        int fromIndex = pageNumber * page;
        int toIndex = pageNumber * page + page;
        for (Item item : items.subList(fromIndex, toIndex)) {
            System.out.println(item.toString());
        }
        pageNumber++;
        System.out.println(String.format("---PAGE %d OF %d---", pageNumber, pageCount));
    }

    public void printPrevPage() {
        if (!accessed && items == null) {
            return;
        }
        if (pageNumber == 1) {
            System.out.println("No more pages.");
            return;
        }
        pageNumber--;
        int fromIndex = (pageNumber - 1) * page;
        int toIndex = (pageNumber - 1) * page + page;
        for (Item item : items.subList(fromIndex, toIndex)) {
            System.out.println(item.toString());
        }
        System.out.println(String.format("---PAGE %d OF %d---", pageNumber, pageCount));
    }


}
