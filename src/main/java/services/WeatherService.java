package services;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public WeatherForecast getForecast(String location, LocalDate date) throws IOException, InterruptedException {
        if (location == null || location.trim().length() < 3 || isInternalVenue(location)) {
            throw new IOException("Lieu non reconnu par l'API meteo");
        }

        GeoPoint geoPoint = geocode(location);
        String forecastUrl = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + geoPoint.latitude
                + "&longitude=" + geoPoint.longitude
                + "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"
                + "&timezone=auto"
                + "&start_date=" + date
                + "&end_date=" + date;

        String json = get(forecastUrl);
        double min = extractDouble(json, "temperature_2m_min");
        double max = extractDouble(json, "temperature_2m_max");
        int rainProbability = (int) Math.round(extractDouble(json, "precipitation_probability_max"));
        int weatherCode = (int) Math.round(extractDouble(json, "weather_code"));

        return new WeatherForecast(geoPoint.displayName, date, min, max, rainProbability, describeWeather(weatherCode));
    }

    private boolean isInternalVenue(String location) {
        String normalizedLocation = location.trim().toLowerCase();
        return normalizedLocation.startsWith("salle")
                || normalizedLocation.startsWith("amphi")
                || normalizedLocation.startsWith("lab")
                || normalizedLocation.contains("bibliotheque")
                || normalizedLocation.contains("informatique");
    }

    private GeoPoint geocode(String location) throws IOException, InterruptedException {
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String geocodeUrl = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + encodedLocation
                + "&count=1&language=fr&format=json";

        String json = get(geocodeUrl);
        if (!json.contains("\"results\"")) {
            throw new IOException("Lieu introuvable");
        }

        double latitude = extractObjectDouble(json, "latitude");
        double longitude = extractObjectDouble(json, "longitude");
        String name = extractString(json, "name");
        String country = extractString(json, "country");
        String displayName = country.isEmpty() ? name : name + ", " + country;

        return new GeoPoint(latitude, longitude, displayName);
    }

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("API meteo indisponible");
        }
        return response.body();
    }

    private double extractDouble(String json, String key) throws IOException {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[?(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        throw new IOException("Donnee meteo manquante: " + key);
    }

    private double extractObjectDouble(String json, String key) throws IOException {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        throw new IOException("Coordonnee manquante: " + key);
    }

    private String extractString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String describeWeather(int code) {
        if (code == 0) {
            return "Ciel degage";
        }
        if (code <= 3) {
            return "Partiellement nuageux";
        }
        if (code == 45 || code == 48) {
            return "Brouillard";
        }
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) {
            return "Pluie";
        }
        if (code >= 71 && code <= 77) {
            return "Neige";
        }
        if (code >= 95) {
            return "Orage";
        }
        return "Conditions variables";
    }

    private static class GeoPoint {
        private final double latitude;
        private final double longitude;
        private final String displayName;

        private GeoPoint(double latitude, double longitude, String displayName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayName = displayName;
        }
    }

    public static class WeatherForecast {
        private final String location;
        private final LocalDate date;
        private final double minTemperature;
        private final double maxTemperature;
        private final int rainProbability;
        private final String description;

        public WeatherForecast(String location, LocalDate date, double minTemperature, double maxTemperature,
                               int rainProbability, String description) {
            this.location = location;
            this.date = date;
            this.minTemperature = minTemperature;
            this.maxTemperature = maxTemperature;
            this.rainProbability = rainProbability;
            this.description = description;
        }

        public String getLocation() {
            return location;
        }

        public LocalDate getDate() {
            return date;
        }

        public double getMinTemperature() {
            return minTemperature;
        }

        public double getMaxTemperature() {
            return maxTemperature;
        }

        public int getRainProbability() {
            return rainProbability;
        }

        public String getDescription() {
            return description;
        }
    }
}
