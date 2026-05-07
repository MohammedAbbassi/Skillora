package com.skillora;

/** Affichage des montants en dinars tunisiens (TND). */
public final class MoneyFormat {

    private MoneyFormat() {
    }

    public static String amount(double value) {
        return String.format("%.2f TND", value);
    }
}
