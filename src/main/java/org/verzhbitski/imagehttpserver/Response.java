package org.verzhbitski.imagehttpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Response {

    private String version;
    private int code;
    private String message;

    private Map<String, String> headers = new HashMap<>();

    private List<Integer> body;

    public static class Builder {

        private Response response = new Response();

        public Builder setVersion(String version) {
            response.version = version;
            return this;
        }

        public Builder setCode(int code) {
            response.code = code;
            return this;
        }

        public Builder setMessage(String message) {
            response.message = message;
            return this;
        }

        public Builder addHeader(String header, String value) {
            response.headers.put(header, value);
            return this;
        }

        public Builder setBody(List<Integer> body) {
            response.body = body;
            return this;
        }

        public Response build() {
            return response;
        }
    }

    public void setKeepAlive(boolean keepAlive) {
        if (keepAlive && "HTTP/1.0".equals(version))
            headers.put("Connection", "Keep-Alive");
        else if (!keepAlive && "HTTP/1.1".equals(version))
            headers.put("Connection", "Close");
    }

    public void write(OutputStream outputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(version)
                .append(" ")
                .append(code)
                .append(" ")
                .append(message)
                .append("\r\n");

        for (String header : headers.keySet()) {
            String value = headers.get(header);
            stringBuilder
                    .append(header)
                    .append(": ")
                    .append(value)
                    .append("\r\n");
        }

        stringBuilder.append("\r\n");

        outputStream.write(stringBuilder.toString().getBytes());
        outputStream.flush();

        for (Integer integer : body) {
            outputStream.write(integer);
        }

        outputStream.flush();
        outputStream.close();
    }
}
