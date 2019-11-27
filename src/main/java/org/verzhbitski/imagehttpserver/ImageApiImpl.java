package org.verzhbitski.imagehttpserver;

import java.io.*;
import java.util.*;

public class ImageApiImpl implements ImageApi {

    @Override
    public Response getImage(String imageName) throws IOException {

        Response.Builder responseBuilder = new Response.Builder();

        File file = new File("storage/" + imageName);

        try (FileInputStream fileInputStream = new FileInputStream(file)) {

            List<Integer> body = new ArrayList<>();
            int read;
            while ((read = fileInputStream.read()) != -1) {
                body.add(read);
            }

            responseBuilder
                    .setVersion("HTTP/1.1")
                    .setCode(200)
                    .setMessage("OK")
                    .addHeader("Server", ImageHttpServer.class.getSimpleName())
                    .addHeader("Date", new Date().toString())
                    .addHeader("Content-Type", "image/*")
                    .addHeader("Content-Length", String.valueOf(file.length()))
                    .setBody(body);
        }

        return responseBuilder.build();
    }

    @Override
    public Response uploadImage(List<Integer> body) throws IOException {

        String name = UUID.randomUUID().toString();
        File directory = new File("storage/");
        directory.mkdirs();
        File file = new File("storage/" + name);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

            for (int i = 0; i < body.size(); ++i) {
                fileOutputStream.write(body.get(i));
            }

            Response.Builder responseBuilder = new Response.Builder();

            String response = (String.format(Locale.ENGLISH, "{\"name\": \"%s\"}", name));
            List<Integer> responseBytes = new ArrayList<>();

            for (byte b : response.getBytes()) responseBytes.add((int) b);

            responseBuilder
                    .setVersion("HTTP/1.1")
                    .setCode(200)
                    .setMessage("OK")
                    .addHeader("Server", ImageHttpServer.class.getSimpleName())
                    .addHeader("Date", new Date().toString())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Content-Length", String.valueOf(response.length()))
                    .setBody(responseBytes);

            return responseBuilder.build();
        }
    }
}
