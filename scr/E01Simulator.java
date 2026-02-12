package clientImport;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Builds an E01 header preview (simulation) from:
 * - GF224EntryPreview (includes dateCreatedRaw, note, entryId)
 * - R07ClienteRecord (many mapping fields)
 *
 * NOTE formatting rule:
 * E01_MEMO = "B2B - #<entryId>" + (notes present ? " - <notes>" : "")
 */
public class E01Simulator {

    public static E01TestataOrdineCli simulateHeader(GF224EntryPreview gfPreview, R07ClienteRecord r07) {
        if (gfPreview == null) throw new IllegalArgumentException("gfPreview is null");

        E01TestataOrdineCli e01 = E01Builder.build(gfPreview, r07);

        // dates from GF submission (date_created)
        Timestamp ts = GFDateParser.parseToTimestamp(gfPreview.dateCreatedRaw);
        if (ts == null) ts = new Timestamp(System.currentTimeMillis());

        e01.setE01DataOrdine(ts);
        e01.setSdDtIns(ts);
        e01.setE01DtRiferCliente(E01Builder.toMidnight(ts));

        // current year
        int year = Calendar.getInstance().get(Calendar.YEAR);
        e01.setE01AnnoOrdine(year);

        // --- Adjustments requested: E01 fields from R07 ---
        if (r07 != null) {
            e01.setE01CdIva(r07.cdIva);
            e01.setE01CdAbi(r07.cdAbi);
            e01.setE01CdCab(r07.cdCab);
            e01.setE01CdPagamento(r07.cdPagamento);
            e01.setE01CdValuta(r07.cdValuta);
            e01.setE01CdSpedizioniere(r07.cdModConsegna);
            e01.setE01CdVettore(r07.cdVettore);
            e01.setE01CdTrasporto(r07.cdTrasporto);
        }

        // MEMO rule: "B2B - #entryId - note" (last " - " only if note present)
        e01.setE01Memo(buildMemo(gfPreview.entryId, gfPreview.note));

        return e01;
    }

    public static String prettyPrint(E01TestataOrdineCli e) {
        StringBuilder sb = new StringBuilder();
        sb.append("E01_TESTATA_ORDINI_CLI (SIMULATION)\n");
        sb.append("E01_ANNO_ORDINE: ").append(e.getE01AnnoOrdine()).append("\n");
        sb.append("E01_NR_ORDINE: ").append(e.getE01NrOrdine()).append(" (assigned only on INSERT via BPR_PROGRESSIVI)\n");
        sb.append("E01_DATA_ORDINE: ").append(e.getE01DataOrdine()).append("\n");
        sb.append("E01_DT_RIFER_CLIENTE: ").append(e.getE01DtRiferCliente()).append("\n");
        sb.append("E01_CD_CLIENTE: ").append(e.getE01CdCliente()).append("\n");
        sb.append("E01_CD_DESTINAZIONE: ").append(e.getE01CdDestinazione()).append("\n");
        sb.append("E01_CD_AGENTE: ").append(e.getE01CdAgente()).append("\n");
        sb.append("E01_CD_IVA: ").append(e.getE01CdIva()).append("\n");
        sb.append("E01_CD_ABI: ").append(e.getE01CdAbi()).append("\n");
        sb.append("E01_CD_CAB: ").append(e.getE01CdCab()).append("\n");
        sb.append("E01_CD_PAGAMENTO: ").append(e.getE01CdPagamento()).append("\n");
        sb.append("E01_CD_VALUTA: ").append(e.getE01CdValuta()).append("\n");
        sb.append("E01_CD_SPEDIZIONIERE: ").append(e.getE01CdSpedizioniere()).append("\n");
        sb.append("E01_CD_VETTORE: ").append(e.getE01CdVettore()).append("\n");
        sb.append("E01_CD_TRASPORTO: ").append(e.getE01CdTrasporto()).append("\n");
        sb.append("E01_IMPORTO_CAMBIO: ").append(e.getE01ImportoCambio()).append(" (filled on INSERT)\n");
        sb.append("E01_IMPORTO_CAMBIO_EURO: ").append(e.getE01ImportoCambioEuro()).append(" (filled on INSERT)\n");
        sb.append("E01_CD_OPERATORE: ").append(e.getE01CdOperatore()).append("\n");
        sb.append("E01_MEMO: ").append(e.getE01Memo()).append("\n");
        sb.append("E01_CD_STAGIONE: ").append(e.getE01CdStagione()).append("\n");
        sb.append("E01_TIPO_ORDINE: ").append(e.getE01TipoOrdine()).append("\n");
        sb.append("E01_CD_AZIENDA: ").append(e.getE01CdAzienda()).append("\n");
        sb.append("E01_FLAG_DA_CONFERMARE: ").append(e.getE01FlagDaConfermare()).append("\n");
        sb.append("E01_FLAG_ORD_PROD: ").append(e.getE01FlagOrdProd()).append("\n");
        return sb.toString();
    }

    private static String buildMemo(String entryId, String note) {
        String id = safeTrim(entryId);
        String n = safeTrim(note);

        String base = "B2B";
        if (id != null) base += " - #" + id;

        if (n != null) {
            base += " - " + n;
        }
        return base;
    }

    private static String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}