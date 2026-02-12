package clientImport;

import java.sql.Timestamp;
import java.util.List;

public class E12Builder {

    /**
     * Build one E12 line from one repeater row.
     *
     * @param e01Header already inserted/allocated header (anno/nr/iva/data)
     * @param r repeating row from form (parsed)
     * @param riga progressive 1..N
     * @param g82Keys ordered list of G82_KEY (by sequenza)
     */
    public static E12RigaDettOrdCli build(E01TestataOrdineCli e01Header,
                                          GFRepeaterRow r,
                                          int riga,
                                          List<String> g82Keys) {
        E12RigaDettOrdCli e = new E12RigaDettOrdCli();

        e.cdAzienda = "AZ1";
        e.annoOrdine = e01Header.getE01AnnoOrdine();
        e.nrOrdine = e01Header.getE01NrOrdine();
        e.riga = riga;

        e.stRecord = "V";
        e.stModifica = 0;

        e.cdIva = e01Header.getE01CdIva();
        e.dataRigaOrdine = e01Header.getE01DataOrdine();

        // delivery date from GF header field 6 (already mapped as string in GF224EntryPreview.deliveryDate)
        // We'll set this outside if you prefer; for now keep it null and set in service using GF date parsing.
        e.dataConsConfermata = null;

        e.flagTipoRiga = "P";
        e.idListino = null;
        e.idAccordo = null;
        e.percAbbin = null;

        e.cdTipoArticolo = "PF";

        e.fk1 = r.fk1;
        e.fk2 = r.fk2;
        e.fk3 = r.fk3;
        e.fk6 = r.fk6;

        e.note = null;

        // COD01.. from g82Keys ordered by sequenza
        int max = Math.min(41, g82Keys == null ? 0 : g82Keys.size());
        for (int i = 0; i < max; i++) {
            e.cod[i] = g82Keys.get(i);
        }

        // quantities:
        // your repeater has size_1..size_13. We'll map them to QTAOR01..QTAOR13 (rest 0).
        // If later you add more size fields, we extend here.
        int[] sizes = extractSizeQty13(r);
        for (int i = 0; i < sizes.length && i < 41; i++) {
            e.qtaOr[i] = sizes[i];
        }

        // QTA PR/SP always 0
        // FLGC always N already defaulted in ctor

        // price: your export shows same price repeated on many columns
        double unit = GFRepeaterParser.parseCommaPrice(r.prezzoUnit);
        for (int i = 0; i < 41; i++) {
            e.prUv[i] = unit;
            e.prUe[i] = unit;
        }

        e.flagProduzione = "S";
        e.flagChiuso = "N";
        e.sconto = null;

        return e;
    }

    /**
     * Converts size_1..size_13 strings to int[13] quantities.
     */
    private static int[] extractSizeQty13(GFRepeaterRow r) {
        int[] out = new int[13];
        out[0]  = parseIntSafe(r.size1);
        out[1]  = parseIntSafe(r.size2);
        out[2]  = parseIntSafe(r.size3);
        out[3]  = parseIntSafe(r.size4);
        out[4]  = parseIntSafe(r.size5);
        out[5]  = parseIntSafe(r.size6);
        out[6]  = parseIntSafe(r.size7);
        out[7]  = parseIntSafe(r.size8);
        out[8]  = parseIntSafe(r.size9);
        out[9]  = parseIntSafe(r.size10);
        out[10] = parseIntSafe(r.size11);
        out[11] = parseIntSafe(r.size12);
        out[12] = parseIntSafe(r.size13);
        return out;
    }

    private static int parseIntSafe(String s) {
        if (s == null) return 0;
        String t = s.trim();
        if (t.isEmpty()) return 0;
        try { return Integer.parseInt(t); } catch (Exception e) { return 0; }
    }

    /**
     * Helper to set delivery date once parsed.
     */
    public static Timestamp toMidnight(Timestamp ts) {
        return E01Builder.toMidnight(ts);
    }
}