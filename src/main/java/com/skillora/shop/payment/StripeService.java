package com.skillora.shop.payment;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.concurrent.CompletableFuture;

/**
 * Service gérant les interactions avec l'API Stripe.
 */
public class StripeService {

    public StripeService() {
        Stripe.apiKey = StripeConfig.STRIPE_SECRET_KEY;
    }

    /**
     * Crée une session Stripe Checkout.
     */
    public CompletableFuture<StripeResponse> createPayment(double amountUsd, String orderId) {
        if (StripeConfig.SIMULATION_MODE) {
            System.out.println("[Stripe] MODE SIMULATION ACTIVÉ");
            return CompletableFuture.supplyAsync(() -> {
                return new StripeResponse(true, "SIM_SESSION_" + System.currentTimeMillis(), "SIMULATION");
            });
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("[Stripe] Création de la session pour la commande #" + orderId + " (" + amountUsd + " USD)");
                
                SessionCreateParams params = SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(StripeConfig.SUCCESS_URL)
                        .setCancelUrl(StripeConfig.CANCEL_URL)
                        .setClientReferenceId(orderId)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency(StripeConfig.CURRENCY)
                                                        .setUnitAmount((long) (amountUsd * 100)) // Stripe utilise les cents
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Commande Skillora #" + orderId)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

                Session session = Session.create(params);
                return new StripeResponse(true, session.getId(), session.getUrl());
                
            } catch (Exception e) {
                System.err.println("[Stripe] Erreur creation session : " + e.getMessage());
                return new StripeResponse(false, null, null);
            }
        });
    }

    /**
     * Vérifie le statut d'une session.
     */
    public CompletableFuture<Boolean> verifyPayment(String sessionId) {
        if (StripeConfig.SIMULATION_MODE && sessionId.startsWith("SIM_SESSION_")) {
            return CompletableFuture.completedFuture(true);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                Session session = Session.retrieve(sessionId);
                return "paid".equals(session.getPaymentStatus());
            } catch (Exception e) {
                System.err.println("[Stripe] Erreur verification : " + e.getMessage());
                return false;
            }
        });
    }

    // Classe interne pour la réponse simplifiée
    public static class StripeResponse {
        private final boolean success;
        private final String sessionId;
        private final String url;

        public StripeResponse(boolean success, String sessionId, String url) {
            this.success = success;
            this.sessionId = sessionId;
            this.url = url;
        }

        public boolean isSuccess() { return success; }
        public String getSessionId() { return sessionId; }
        public String getUrl() { return url; }
    }
}
