package clientImport;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class E12OrderLinesService {

    private final OracleE12Repository e12Repo = new OracleE12Repository();
    private final G82MisuraRepository g82Repo = new G82MisuraRepository();

    public int insertLines(Connection con, E01TestataOrdineCli e01Header, GF224EntryPreview gf, ListinoRules.PriceMode priceMode) throws Exception {
        Timestamp consegna = parseDeliveryDateToMidnight(gf.deliveryDate);

        List<GFRepeaterRow> allRows = new ArrayList<>();
        allRows.addAll(GFRepeaterParser.parseRows(gf.repeater73Json));
        allRows.addAll(GFRepeaterParser.parseRows(gf.repeater74Json));
        allRows.addAll(GFRepeaterParser.parseRows(gf.repeater77Json)); // NEW

        int riga = 1;
        int inserted = 0;

        for (GFRepeaterRow rr : allRows) {

            // filter out not-confirmed rows (match PDF behaviour)
            if (!rr.confirmed) continue;

            applyPriceMode(rr, priceMode);

            // IMPORTANT: if FK_1 missing, we cannot resolve sizes, but we still try inserting with empty CODxx.
            List<String> g82Keys = g82Repo.listMisureKeysByFk1(con, rr.fk1);

            E12RigaDettOrdCli e12 = E12Builder.build(e01Header, rr, riga, g82Keys);
            e12.dataConsConfermata = consegna;

            int ins = e12Repo.insert(con, e12);
            if (ins != 1) throw new RuntimeException("E12 insert affected " + ins + " rows at riga=" + riga);

            inserted++;
            riga++;
        }

        return inserted;
    }

    private void applyPriceMode(GFRepeaterRow rr, ListinoRules.PriceMode mode) {
        if (rr == null || mode == null) return;

        switch (mode) {
            case FLAT_70:
                rr.prezzoUnit = "70,00";
                rr.prezzoTotale = computeTotale(rr.prezzoUnit, rr.totalePezzi);
                break;

            case USE_API_PREZZO_EUR:
                // CAN/DKK: force unit price from prezzo_eur if provided
                if (rr.prezzoEur != null && !rr.prezzoEur.trim().isEmpty()) {
                    rr.prezzoUnit = rr.prezzoEur;
                    rr.prezzoTotale = computeTotale(rr.prezzoUnit, rr.totalePezzi);
                }
                break;

            case USE_TRANSMITTED_PRICES:
            default:
                // do nothing
                break;
        }
    }

    private String computeTotale(String prezzoUnit, int totalePezzi) {
        if (totalePezzi <= 0) return null;
        double unit = GFRepeaterParser.parseCommaPrice(prezzoUnit);
        double tot = unit * totalePezzi;
        return GFRepeaterParser.formatComma2(tot);
    }

    private Timestamp parseDeliveryDateToMidnight(String raw) {
        Timestamp ts = GFDateParser.parseToTimestamp(raw);
        if (ts != null) return E01Builder.toMidnight(ts);

        if (raw != null) {
            String t = raw.trim();
            if (t.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                Timestamp t0 = GFDateParser.parseToTimestamp(t + " 00:00:00.000");
                if (t0 != null) return t0;
            }
        }
        return null;
    }
}