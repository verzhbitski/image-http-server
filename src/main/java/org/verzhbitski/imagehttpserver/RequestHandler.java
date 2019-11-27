package org.verzhbitski.imagehttpserver;

import java.util.StringTokenizer;

public class RequestHandler {

    private ImageApi api = new ImageApiImpl();

    public Response handle(Request request) throws Exception {
        StringTokenizer pathTokenizer = new StringTokenizer(request.getPath(), "/");

        switch (request.getMethod()) {
            case GET:
                String method = pathTokenizer.nextToken();
                String name = pathTokenizer.nextToken();

                if (method.equals("image")) {
                    return api.getImage(name);
                }

                throw new IllegalStateException();

            case POST:
                if (pathTokenizer.nextToken().equals("upload")) {
                    return api.uploadImage(request.getBody());
                }

            default:
                throw new IllegalStateException();
        }
    }
}
