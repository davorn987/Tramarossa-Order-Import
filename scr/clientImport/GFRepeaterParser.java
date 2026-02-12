package clientImport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class GFRepeaterParser {
    private static final ObjectMapper om = new ObjectMapper();

    public static List<GFRepeaterRow> parseRows(String rawJson) {
        List<GFRepeaterRow> out = new ArrayList<>();
        if (rawJson == null) return out;

        String s = rawJson.trim();
        if (s.isEmpty()) return out;

        try {
            JsonNode root = om.readTree(s);
            if (root == null || !root.isArray()) return out;

            for (JsonNode n : root) {
                GFRepeaterRow r = new GFRepeaterRow();
                r.modello = asText(n, "modello");
                r.tessuto = asText(n, "tessuto");
                r.trattamento = asText(n, "trattamento");
                r.colore = asText(n, "colore");

                // sizes
                r.size1 = asText(n, "size_1");
                r.size2 = asText(n, "size_2");
                r.size3 = asText(n, "size_3");
                r.size4 = asText(n, "size_4");
                r.size5 = asText(n, "size_5");
                r.size6 = asText(n, "size_6");
                r.size7 = asText(n, "size_7");
                r.size8 = asText(n, "size_8");
                r.size9 = asText(n, "size_9");
                r.size10 = asText(n, "size_10");
                r.size11 = asText(n, "size_11");
                r.size12 = asText(n, "size_12");
                r.size13 = asText(n, "size_13");

                r.totalePezzi = asIntSafe(n, "totale");

                // prices
                r.prezzoUnit = asText(n, "prezzo");
                r.prezzoTotale = asText(n, "prezzo_totale");

                // NEW: EUR reference
                r.prezzoEur = asText(n, "prezzo_eur");

                r.tessutoProduzione = asText(n, "tessuto_produzione");
                r.fk1 = asText(n, "FK_1");
                r.fk2 = asText(n, "FK_2");
                r.fk3 = asText(n, "FK_3");
                r.fk6 = asText(n, "FK_6");

                r.confirmed = asBoolSafe(n, "_confirmed");

                out.add(r);
            }
        } catch (Exception ignored) {
            // return empty list on parse errors
        }
        return out;
    }

    public static int sumPezzi(List<GFRepeaterRow> rows) {
        int sum = 0;
        for (GFRepeaterRow r : rows) sum += Math.max(0, r.totalePezzi);
        return sum;
    }

    public static double sumPrezzo(List<GFRepeaterRow> rows) {
        double sum = 0;
        for (GFRepeaterRow r : rows) sum += parseCommaPrice(r.prezzoTotale);
        return sum;
    }

    public static double parseCommaPrice(String s) {
        if (s == null) return 0;
        String x = s.trim();
        if (x.isEmpty()) return 0;
        x = x.replace(".", "").replace(",", ".");
        try { return Double.parseDouble(x); } catch (Exception e) { return 0; }
    }

    public static String formatComma2(double n) {
        String x = String.format(java.util.Locale.US, "%.2f", n);
        return x.replace(".", ",");
    }

    private static String asText(JsonNode n, String k) {
        JsonNode v = n.get(k);
        return (v == null || v.isNull()) ? "" : v.asText("");
    }

    private static int asIntSafe(JsonNode n, String k) {
        String s = asText(n, k);
        if (s == null) return 0;
        s = s.trim();
        if (s.isEmpty()) return 0;
        try {
            // accept "13" or "13,0"
            double d = Double.parseDouble(s.replace(",", "."));
            return (int) Math.round(d);
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean asBoolSafe(JsonNode n, String k) {
        JsonNode v = n.get(k);
        if (v == null || v.isNull()) return false;
        if (v.isBoolean()) return v.asBoolean(false);
        String s = v.asText("");
        return "1".equals(s) || "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s);
    }
}