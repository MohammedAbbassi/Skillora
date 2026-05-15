package utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CountryService {
    private static final String API_URL = "https://restcountries.com/v3.1/all?fields=name,cca2,flags";
    private static CountryService instance;
    private final HttpClient httpClient;

    private volatile List<String> cachedCountryNames;


    private CountryService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public static synchronized CountryService getInstance() {
        if (instance == null) {
            instance = new CountryService();
        }
        return instance;
    }

    /**
     * Fetches all country names from RestCountries API asynchronously.
     * @return A CompletableFuture containing a list of country names.
     */
    public CompletableFuture<List<String>> getAllCountryNames() {
        if (cachedCountryNames != null) {
            return CompletableFuture.completedFuture(cachedCountryNames);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        cachedCountryNames = parseCountryNames(response.body());
                        return cachedCountryNames;
                    } else {
                        System.err.println("Failed to fetch countries: " + response.statusCode());
                        return Collections.<String>emptyList();
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Error fetching countries: " + ex.getMessage());
                    return Collections.<String>emptyList();
                });
    }

    private List<String> parseCountryNames(String jsonBody) {
        List<String> countries = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonBody);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject countryObj = jsonArray.getJSONObject(i);
            JSONObject nameObj = countryObj.getJSONObject("name");

            String countryName = nameObj.getString("common");
            String flag = countryObj.optString("flag", "");
            
            if (!flag.isEmpty()) {
                countries.add(countryName + " " + flag);
            } else {
                countries.add(countryName);
            }

        }
        Collections.sort(countries);
        return countries;
    }
}
