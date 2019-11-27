package org.verzhbitski.imagehttpserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    public enum Method { GET, POST }

    private Method method;
    private String path;
    private String version;

    private Map<String, String> headers = new HashMap<>();

    private List<Integer> body;

    public static class Builder {

        private Request request = new Request();

        public Builder setMethod(Method method) {
            request.method = method;
            return this;
        }

        public Builder setPath(String path) {
            request.path = path;
            return this;
        }

        public Builder setVersion(String version) {
            request.version = version;
            return this;
        }

        public Builder addHeader(String header, String value) {
            request.headers.put(header, value);
            return this;
        }

        public Map<String, String> getHeaders() {
            return Collections.unmodifiableMap(request.headers);
        }

        public Builder setBody(List<Integer> body) {
            request.body = body;
            return this;
        }

        public Request build() {
            return request;
        }
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public List<Integer> getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(method.toString())
                .append(" ")
                .append(path)
                .append(" ")
                .append(version)
                .append("\n");

        for (String header : headers.keySet()) {
            stringBuilder
                    .append(header)
                    .append(": ")
                    .append(headers.get(header))
                    .append("\n");
        }

        return stringBuilder.toString();
    }
}
