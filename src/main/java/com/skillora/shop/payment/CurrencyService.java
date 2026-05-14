package com.skillora.shop.payment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour la conversion de devises (TND vers USD).
 */
public class CurrencyService {
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/TND";
    private final HttpClient httpClient;

    public CurrencyService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Récupère le taux de conversion actuel de TND vers USD.
     * @return CompletableFuture avec le taux (ex: 0.32)
     */
    public CompletableFuture<Double> getTndToUsdRate() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        return json.getAsJsonObject("rates").get("USD").getAsDouble();
                    } else {
                        System.err.println("Erreur API Conversion : " + response.statusCode());
                        return 0.32; // Valeur par défaut approximative si l'API échoue
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Exception Conversion : " + ex.getMessage());
                    return 0.32; // Valeur de repli
                });
    }

    /**
     * Convertit un montant de TND vers USD.
     */
    public CompletableFuture<Double> convertTndToUsd(double amountTnd) {
        return getTndToUsdRate().thenApply(rate -> amountTnd * rate);
    }
}
