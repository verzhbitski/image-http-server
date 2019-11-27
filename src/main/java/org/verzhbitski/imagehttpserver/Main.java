package org.verzhbitski.imagehttpserver;

import java.io.IOException;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        int port;

        if (args.length == 0) {
            System.out.println("Using default port 8080...");
            port = 8080;
        } else {
            port = Integer.parseInt(args[0]);
        }

        ImageHttpServer imageHttpServer = new ImageHttpServer(port);

        System.out.println(String.format(Locale.ENGLISH, "Starting server on %d port...", port));

        try {
            imageHttpServer.start();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
