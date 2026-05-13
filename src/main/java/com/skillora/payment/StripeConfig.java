package com.skillora.payment;

/**
 * Configuration centralisée pour l'API Stripe.
 */
public class StripeConfig {
    /**
     * Mode SIMULATION : 
     * - true  : Simule un paiement réussi sans appeler l'API réelle.
     * - false : Utilise l'API Stripe réelle.
     */
    public static final boolean SIMULATION_MODE = false;

    /**
     * Votre Clé Publique Stripe
     */
    public static final String STRIPE_PUBLIC_KEY = "pk_test_51TWiHzHHFTqyiT2S635kj6W2txZcYt7dhyAiw06oMoSzTo4amTz2rWjAfsjfVYktkRemvSDy08ghne3HedQdssCO00EdUwaBOl";

    /**
     * Votre Clé Secrète Stripe
     */
    public static final String STRIPE_SECRET_KEY = "sk_test_51TWiHzHHFTqyiT2SmRO8FtUys6E04FCT2RSShW6PROyrlLyH5wbH9fvmGm2SQ9gsejzz33lrVbdQNajSg18rhrER00M3ZDWiCj";

    // URLs de redirection après le paiement Stripe Checkout
    public static final String SUCCESS_URL = "https://skillora.com/payment/success?session_id={CHECKOUT_SESSION_ID}";
    public static final String CANCEL_URL = "https://skillora.com/payment/cancel";

    public static final String CURRENCY = "usd";
}
