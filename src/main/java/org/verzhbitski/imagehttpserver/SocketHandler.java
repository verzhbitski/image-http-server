package org.verzhbitski.imagehttpserver;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class SocketHandler {

    private InputStream inputStream;
    private OutputStream outputStream;

    private RequestHandler requestHandler = new RequestHandler();

    public SocketHandler(Socket socket) throws IOException {
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public boolean handle() {
        try {
            Request.Builder requestBuilder = new Request.Builder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String start;

            if (!(start = bufferedReader.readLine()).isEmpty()) {

                StringTokenizer stringTokenizer = new StringTokenizer(start);

                String method = stringTokenizer.nextToken().toUpperCase();
                String path = stringTokenizer.nextToken();
                String version = stringTokenizer.nextToken();

                switch (method) {
                    case "GET":
                        requestBuilder.setMethod(Request.Method.GET);
                        break;
                    case "POST":
                        requestBuilder.setMethod(Request.Method.POST);
                        break;
                    default:
                        throw new IllegalStateException();
                }

                requestBuilder.setPath(path);
                requestBuilder.setVersion(version);
            }

            String line;
            while (!(line = bufferedReader.readLine()).isEmpty()) {
                StringTokenizer stringTokenizer = new StringTokenizer(line, ": ");

                String header = stringTokenizer.nextToken();
                String value = stringTokenizer.nextToken();

                requestBuilder.addHeader(header, value);
            }

            String contentLengthString = requestBuilder.getHeaders().get("Content-Length");

            if (contentLengthString != null) {
                int contentLength = Integer.parseInt(contentLengthString);

                if (contentLength > -1) {
                    List<Integer> body = new ArrayList<>();

                    while (contentLength != 0) {
                        int read = inputStream.read();
                        body.add(read);
                        --contentLength;
                    }

                    requestBuilder.setBody(body);
                }
            }

            Request request = requestBuilder.build();

            System.out.println("\nRequest:");
            System.out.println(request.toString());

            Response response = requestHandler.handle(request);

            String version = request.getVersion();
            Map<String, String> headers = request.getHeaders();

            boolean keepAlive = shouldKeepAlive(headers, version);

            response.setKeepAlive(keepAlive);
            response.write(outputStream);

            return keepAlive;

        } catch (Throwable e) {
            e.printStackTrace();

            Response.Builder responseBuilder = new Response.Builder();

            responseBuilder
                    .setVersion("HTTP/1.1")
                    .setCode(404)
                    .setMessage("Not Found")
                    .addHeader("Server", ImageHttpServer.class.getSimpleName())
                    .addHeader("Date", new Date().toString());

            try {
                responseBuilder.build().write(outputStream);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    private boolean shouldKeepAlive(Map<String, String> headers, String version) {
        boolean headerKeepAlive = headers.getOrDefault("Connection", "").equals("Keep-Alive");
        boolean headerClose = headers.getOrDefault("Connection", "").equals("Close");

        return
                ("HTTP/1.0".equals(version) && headerKeepAlive) ||
                ("HTTP/1.1".equals(version) && headerClose);
    }
}
