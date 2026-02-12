package clientImport;

import java.util.Locale;

public final class ListinoRules {

    private ListinoRules() {}

    public enum PriceMode {
        /** Usa i prezzi trasmessi (rr.prezzoUnit come arriva dal GF). */
        USE_TRANSMITTED_PRICES,

        /** CAN/DKK: usa prezzo EUR da estrazione API (nel JSON è "prezzo_eur"). */
        USE_API_PREZZO_EUR,

        /** DOLLARO: prezzo flat 70 sempre. */
        FLAT_70
    }

    public static final class Result {
        public final String e01CdValuta;  // EUR / USD / LGS
        public final String e01CdListino; // "57" "58" "59" "60" "61"
        public final PriceMode priceMode;

        public Result(String e01CdValuta, String e01CdListino, PriceMode priceMode) {
            this.e01CdValuta = e01CdValuta;
            this.e01CdListino = e01CdListino;
            this.priceMode = priceMode;
        }

        @Override public String toString() {
            return "ListinoRules.Result{valuta=" + e01CdValuta + ", listino=" + e01CdListino + ", priceMode=" + priceMode + "}";
        }
    }

    /**
     * Resolve rules from GF listino label (field 64, mapped as GF224EntryPreview.listino).
     */
    public static Result resolve(String listinoLabel) {
        String norm = normalize(listinoLabel);

        // Listino Euro ITA
        if (containsAny(norm, "EURO_ITA", "EUR_ITA", "ITALIA", "ITA")) {
            return new Result("EUR", "59", PriceMode.USE_TRANSMITTED_PRICES);
        }

        // Listino CAN
        if (containsAny(norm, "CAN")) {
            return new Result("EUR", "57", PriceMode.USE_API_PREZZO_EUR);
        }

        // Listino DKK
        if (containsAny(norm, "DKK")) {
            return new Result("EUR", "57", PriceMode.USE_API_PREZZO_EUR);
        }

        // Listino DOLLARO
        if (containsAny(norm, "DOLLARO", "DOLLAR", "USD")) {
            return new Result("USD", "60", PriceMode.FLAT_70);
        }

        // Listino CH
        if (containsAny(norm, "LISTINO_CH") || "CH".equals(norm) || containsAny(norm, "_CH", "CHF", "SVIZZ")) {
            return new Result("EUR", "61", PriceMode.USE_TRANSMITTED_PRICES);
        }

        // Listino POUND
        if (containsAny(norm, "POUND", "GBP", "STERLINA", "LGS")) {
            return new Result("LGS", "58", PriceMode.USE_TRANSMITTED_PRICES);
        }

        // Listino Euro
        if (containsAny(norm, "EURO", "EUR")) {
            return new Result("EUR", "57", PriceMode.USE_TRANSMITTED_PRICES);
        }

        // fallback safe
        return new Result("EUR", "57", PriceMode.USE_TRANSMITTED_PRICES);
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String out = s.trim().toUpperCase(Locale.ROOT);
        out = out.replaceAll("\\s+", "_");
        out = out.replaceAll("_+", "_");
        return out;
    }

    private static boolean containsAny(String norm, String... tokens) {
        if (norm == null) return false;
        for (String t : tokens) {
            if (t != null && !t.isEmpty() && norm.contains(t)) return true;
        }
        return false;
    }
}