package advisor;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        String access = "https://accounts.spotify.com";
        String resource = "https://api.spotify.com";
        int page = 5;

        for (int i = 0; i < args.length; i++) {
            if ("-access".equals(args[i])) {
                access = args[i + 1];
            }
            if ("-resource".equals(args[i])) {
                resource = args[i + 1];
            }
            if ("-page".equals(args[i])) {
                try {
                    page = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    e.getMessage();
                }
            }
        }

        Scanner scanner = new Scanner(System.in);
        Menu menu = new Menu(Spotify.getInstance(access, resource), scanner, page);

        while (true) {
            String userRequest = scanner.nextLine();
            if (userRequest.startsWith("auth")) {
                menu.Auth();
            }
            if ("new".equals(userRequest)) {
                menu.showNew();
            }
            if ("featured".equals(userRequest)) {
                menu.showFeatured();
            }
            if ("categories".equals(userRequest)) {
                menu.showCategories();
            }
            if (userRequest.startsWith("playlists ")) {
                String inputCategory = userRequest.split("playlists ")[1];
                try {
                    System.out.println("inputCategory = " + inputCategory);
                    menu.showPlaylists(inputCategory);
                } catch (IOException e) {
                    e.getMessage();
                }
            }
            if ("next".equals(userRequest)) {
                menu.printNextPage();
            }
            if ("prev".equals(userRequest)) {
                menu.printPrevPage();
            }
            if ("exit".equals(userRequest)) {
                menu.showExit();
                return;
            }
        }

    }
}
