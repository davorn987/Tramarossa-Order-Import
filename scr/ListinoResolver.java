package clientImport;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ListinoResolver
 *
 * Implements the rules you specified for mapping "listino" labels to:
 * - whether to use CSV price or override price
 * - E01_CD_VALUTA, E01_CD_LISTINO
 * - E01_IMPORTO_CAMBIO, E01_IMPORTO_CAMBIO_EURO (via exchange rate lookup)
 *
 * Usage:
 *   Map<String,String> csvRow = ...; // keys like "prezzo_EUR", "prezzo_EUR_ITA", ...
 *   ResolverResult r = ListinoResolver.resolve("Listino Euro ITA", csvRow);
 */
public class ListinoResolver {

    private static final ObjectMapper OM = new ObjectMapper();

    // simple in-memory cache for exchange rates with TTL (ms)
    private static final Map<String, RateCacheEntry> RATE_CACHE = new ConcurrentHashMap<>();
    private static final long RATE_TTL_MS = 30 * 60 * 1000L; // 30 minutes

    public static class ResolverResult {
        public final boolean useCsvPrice; // true if should use CSV (and price=null means pick from CSV)
        public final String price;        // explicit price to use (string) or null
        public final String e01CdValuta;
        public final int e01CdListino;
        public final Double e01ImportoCambio;
        public final Double e01ImportoCambioEuro;

        public ResolverResult(boolean useCsvPrice, String price, String e01CdValuta, int e01CdListino, Double e01ImportoCambio, Double e01ImportoCambioEuro) {
            this.useCsvPrice = useCsvPrice;
            this.price = price;
            this.e01CdValuta = e01CdValuta;
            this.e01CdListino = e01CdListino;
            this.e01ImportoCambio = e01ImportoCambio;
            this.e01ImportoCambioEuro = e01ImportoCambioEuro;
        }
    }

    private static class RateCacheEntry {
        final double rate;
        final long ts;
        RateCacheEntry(double rate, long ts) { this.rate = rate; this.ts = ts; }
    }

    /**
     * Resolve rules as per your specification.
     *
     * @param listinoLabel label selected in the form (e.g. "Listino Euro ITA")
     * @param csvRow optional CSV row map (keys are column headers)
     * @return ResolverResult
     */
    public static ResolverResult resolve(String listinoLabel, Map<String, String> csvRow) {
        String raw = listinoLabel == null ? "" : listinoLabel.trim();
        String norm = normalizeListino(raw);

        boolean useCsvPrice = true;
        String price = null;
        String e01CdValuta = "EUR";
        int e01CdListino = 57;
        Double importoCambio = null;
        Double importoCambioEuro = null;

        try {
            if (containsAny(norm, "EUR_ITA", "LISTINO_EUR_ITA", "LISTINO_EURO_ITA")) {
                useCsvPrice = true;
                e01CdValuta = "EUR";
                e01CdListino = 59;
            } else if (containsAny(norm, "CAN", "LISTINO_CAN")) {
                e01CdValuta = "EUR";
                e01CdListino = 57;
                String val = findCsvPrice(csvRow, Arrays.asList("prezzo_EUR"));
                if (val != null) { price = val; useCsvPrice = false; }
            } else if (containsAny(norm, "DKK", "LISTINO_DKK")) {
                e01CdValuta = "EUR";
                e01CdListino = 57;
                String val = findCsvPrice(csvRow, Arrays.asList("prezzo_EUR"));
                if (val != null) { price = val; useCsvPrice = false; }
            } else if (containsAny(norm, "DOLLAR", "USD", "LISTINO_DOLLAR", "LISTINO_DOLLARO")) {
                useCsvPrice = false;
                price = "70";
                e01CdValuta = "USD";
                e01CdListino = 60;
                Double r = fetchRateToEur("USD");
                if (r != null) { importoCambio = r; importoCambioEuro = r; }
            } else if (containsAny(norm, "LISTINO_EURO") || norm.equals("EUR") || containsAny(norm, "EURO")) {
                useCsvPrice = true;
                e01CdValuta = "EUR";
                e01CdListino = 57;
            } else if (containsAny(norm, "CH", "CHF", "LISTINO_CH", "LISTINO_EUR_CH")) {
                useCsvPrice = true;
                e01CdValuta = "EUR";
                e01CdListino = 61;
            } else if (containsAny(norm, "POUND", "GBP", "LISTINO_POUND", "STERLINA")) {
                useCsvPrice = true;
                e01CdValuta = "LGS";
                e01CdListino = 58;
                Double r = fetchRateToEur("GBP");
                if (r != null) { importoCambio = r; importoCambioEuro = r; }
            } else {
                useCsvPrice = true;
                e01CdValuta = "EUR";
                e01CdListino = 57;
            }

            if (useCsvPrice && price == null && csvRow != null) {
                List<String> prefer = new ArrayList<>();
                if (norm.contains("EUR_ITA")) prefer.add("prezzo_EUR_ITA");
                if (norm.contains("EUR_CH") || norm.contains("CH")) prefer.add("prezzo_EUR_CH");
                if (norm.contains("DOLLAR")) prefer.add("prezzo_DOLLAR");
                if (norm.contains("POUND")) prefer.add("prezzo_POUND");
                if (norm.contains("CAN")) prefer.add("prezzo_CAN");
                if (norm.contains("DKK")) prefer.add("prezzo_DKK");
                prefer.add("prezzo_EUR");
                prefer.add("prezzo");
                String val = findCsvPrice(csvRow, prefer);
                if (val != null) { price = val; useCsvPrice = false; }
            }

            if (price != null) {
                price = price.replace(',', '.').trim();
            }
        } catch (Exception ex) {
            // best-effort, swallow
        }

        return new ResolverResult(useCsvPrice, price, e01CdValuta, e01CdListino, importoCambio, importoCambioEuro);
    }

    // ------------------------
    // Helpers
    // ------------------------

    private static boolean containsAny(String norm, String... candidates) {
        if (norm == null) return false;
        for (String c : candidates) {
            if (c != null && norm.contains(c)) return true;
        }
        return false;
    }

    private static String normalizeListino(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toUpperCase();
        s = s.replaceAll("[\\s\\-]+", "_");
        s = s.replaceAll("[^A-Z0-9_]", "");
        s = s.replaceFirst("^LISTINO_", "");
        s = s.replaceFirst("^EURO(_|$)", "EUR$1");
        s = s.replace("_EURO_", "_EUR_");
        s = s.replaceFirst("^DOLLARO(_|$)", "DOLLAR$1");
        return s;
    }

    /**
     * Find a price in csvRow using preferred keys (case-insensitive), fallback to any prezzo_*.
     */
    private static String findCsvPrice(Map<String, String> csvRow, List<String> preferredKeys) {
        if (csvRow == null || csvRow.isEmpty()) return null;
        for (String k : preferredKeys) {
            if (k == null) continue;
            for (String prop : csvRow.keySet()) {
                if (prop.equals(k) && csvRow.get(prop) != null && !csvRow.get(prop).trim().isEmpty()) {
                    return csvRow.get(prop);
                }
            }
        }
        Map<String, String> lower = new HashMap<>();
        for (Map.Entry<String,String> e : csvRow.entrySet()) lower.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue());
        for (String k : preferredKeys) {
            if (k == null) continue;
            String kk = k.toLowerCase(Locale.ROOT);
            if (lower.containsKey(kk) && lower.get(kk) != null && !lower.get(kk).trim().isEmpty()) return lower.get(kk);
        }
        for (Map.Entry<String,String> e : csvRow.entrySet()) {
            if (e.getKey().toLowerCase(Locale.ROOT).startsWith("prezzo_") && e.getValue() != null && !e.getValue().trim().isEmpty()) {
                return e.getValue();
            }
        }
        return null;
    }

    /**
     * Fetch latest 1 <currency> -> EUR rate. Uses simple caching and exchangerate.host public API.
     */
    private static Double fetchRateToEur(String currency) {
        if (currency == null) return null;
        currency = currency.trim().toUpperCase(Locale.ROOT);
        if (currency.equals("EUR")) return 1.0;

        RateCacheEntry ce = RATE_CACHE.get(currency);
        long now = System.currentTimeMillis();
        if (ce != null && (now - ce.ts) < RATE_TTL_MS) {
            return ce.rate;
        }

        String api = "https://api.exchangerate.host/latest?base=" + urlEncode(currency) + "&symbols=EUR";
        try {
            String resp = clientImport.HttpUtils.httpGet(api);
            if (resp == null || resp.trim().isEmpty()) return null;
            Map<?,?> json = OM.readValue(resp, Map.class);
            if (json == null) return null;
            Object ratesObj = json.get("rates");
            if (!(ratesObj instanceof Map)) return null;
            Object eurObj = ((Map<?,?>) ratesObj).get("EUR");
            double rate = Double.parseDouble(String.valueOf(eurObj));
            RATE_CACHE.put(currency, new RateCacheEntry(rate, now));
            return rate;
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }

    private static String urlEncode(String s) {
        try { return java.net.URLEncoder.encode(s, "UTF-8"); } catch (Exception ex) { return s; }
    }
}