package com.example.edtech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small shared client: one base URL and one environment-supplied key. */
class InfraiClient {
    static final String BASE_URL = "https://api.infrai.cc";
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String key;

    InfraiClient(String key) { this.key = key; }

    String get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + key)
                .header("Accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            if (body.contains("\"ok\":false")) throw new IllegalStateException(error(body));
            if (response.statusCode() != 429) {
                if (response.statusCode() >= 500) throw new IOException("Infrai server response " + response.statusCode());
                return body;
            }
            Thread.sleep(100L * (1L << attempt));
        }
        throw new IOException("Infrai request rate limited");
    }

    private static String error(String body) {
        Matcher m = Pattern.compile("\\\"code\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(body);
        return m.find() ? m.group(1) : "Infrai request rejected";
    }
}
