package services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public LocationResult rechercher(String query) throws IOException, InterruptedException {
        if (query == null || query.trim().length() < 3) {
            throw new IOException("Localisation trop courte");
        }

        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String url = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + encodedQuery
                + "&count=1&language=fr&format=json";

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Service de localisation indisponible");
        }

        String json = response.body();
        if (!json.contains("\"results\"")) {
            throw new IOException("Localisation introuvable");
        }

        String name = extractString(json, "name");
        String admin1 = extractString(json, "admin1");
        String country = extractString(json, "country");
        double latitude = extractDouble(json, "latitude");
        double longitude = extractDouble(json, "longitude");

        StringBuilder label = new StringBuilder(name);
        if (!admin1.isEmpty() && !admin1.equalsIgnoreCase(name)) {
            label.append(", ").append(admin1);
        }
        if (!country.isEmpty()) {
            label.append(", ").append(country);
        }

        return new LocationResult(label.toString(), latitude, longitude);
    }

    private String extractString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private double extractDouble(String json, String key) throws IOException {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        throw new IOException("Coordonnees introuvables");
    }

    public static class LocationResult {
        private final String label;
        private final double latitude;
        private final double longitude;

        public LocationResult(String label, double latitude, double longitude) {
            this.label = label;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getLabel() {
            return label;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
