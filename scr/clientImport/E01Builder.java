package clientImport;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Builds E01_TESTATA_ORDINI_CLI header from Gravity Forms preview + R07 client.
 * Contains small normalization helpers (e.g. destinazione to 4 digits).
 */
public class E01Builder {

    public static E01TestataOrdineCli build(GF224EntryPreview gf, R07ClienteRecord r07) {
        if (gf == null) throw new IllegalArgumentException("gf is null");

        E01TestataOrdineCli e = new E01TestataOrdineCli();

        // E01_ANNO_ORDINE = current year
        int year = Calendar.getInstance().get(Calendar.YEAR);
        e.setE01AnnoOrdine(year);

        // E01_NR_ORDINE is assigned by BPR_PROGRESSIVI (repository)
        e.setE01NrOrdine(null);

        // DATA_ORDINE / SD_DT_INS / DT_RIFER_CLIENTE must be set by caller once GF submission date is parsed

        // cliente
        e.setE01CdCliente(nullSafeTrim(gf.griffeIdCliente));
        e.setE01CdClienteSped(null);
        e.setE01RiferCliente(null);

        // REQUIRED hidden status fields (fix for "Valore non ammesso" on E01_ST_RECORD)
        e.setE01StRecord("V");
        e.setE01StModifica(0);

        // R07-driven fields
        if (r07 != null) {
            e.setE01CdAgente(nullSafeTrim(r07.cdAgente)); // E01_CD_AGENTE
            e.setE01CdIva(nullSafeTrim(r07.cdIva));       // E01_CD_IVA
            // (others are currently NULL by your rules, but available in r07 when you decide)
        } else {
            e.setE01CdAgente(null);
            e.setE01CdIva(null);
        }

        // fixed / null by your rules
        e.setE01DataImmissione(null);

        e.setE01CdAbi(null);
        e.setE01CdCab(null);
        e.setE01CdZona(null);
        e.setE01CdPagamento(null);
        e.setE01CdValuta(null);

        e.setE01CdSpedizioniere(null);
        e.setE01CdVettore(null);
        e.setE01CdTrasporto(null);
        e.setE01CdLingua(null);

        e.setE01CdOperatore("000018");

        e.setE01ImportoCambio(1.0);
        e.setE01ImportoCambioEuro(1.0);

        e.setE01CdMagazzino(null);

        // memo = note ordine (NOTE: memo formatting "B2B - #id ..." is applied in E01Simulator)
        e.setE01Memo(nullSafeTrim(gf.note));

        // flags
        e.setE01FlagAccumBolle("N");
        e.setE01FlagMitDestVett("N");
        e.setE01FlagStampa("N");
        e.setE01FlagChiuso("N");

        e.setE01CdAgente2(null);
        e.setE01CdAgente3(null);
        e.setE01CdAgente4(null);

        e.setE01OrdInterno("N");
        e.setE01PercExtracosto(null);
        e.setE01CostoTrasporto(null);
        e.setE01Abbinamento(null);

        e.setE01FlagClav("N");

        // destinazione: normalize to 4 digits, NULL if empty
        e.setE01CdDestinazione(normalizeDestinazione4(gf.griffeIdSpedizione));

        e.setE01FlagImposta("N");
        e.setE01CdStagione("AI26");

        e.setE01CdLineaCli(null);
        e.setE01Sconto01(null);
        e.setE01Sconto02(null);
        e.setE01CdDivisione(null);

        e.setE01PercAge1(0.0);
        e.setE01PercAge2(0.0);
        e.setE01PercAge3(0.0);
        e.setE01PercAge4(0.0);

        e.setE01TipoOrdine("D");

        e.setE01CdListino(null);
        e.setE01DataConsConfermata(null);
        e.setE01PathImgOrdine(null);

        e.setE01CdAzienda("AZ1");
        e.setE01FlgFlash("N");

        // per ora
        e.setE01IdListino("57");

        e.setE01CdBancaPag(null);
        e.setE01Accuratezza("P");
        e.setE01AccurVar("100");

        e.setE01DataRichiesta(null);
        e.setE01CdUtente("DAVIDEN");
        e.setE01FlagDaConfermare("S");
        e.setE01FlagOrdProd("N");

        return e;
    }

    /**
     * Convert a timestamp to the same day at 00:00:00.000.
     */
    public static Timestamp toMidnight(Timestamp ts) {
        if (ts == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTimeInMillis());
    }

    /**
     * Normalize destinazione to numeric 4 digits (left pad with zeros).
     * - null/empty -> null
     * - already 4 digits -> unchanged
     * - numeric shorter -> left pad
     * - if contains non-digits -> return trimmed as-is (to avoid breaking special codes)
     */
    public static String normalizeDestinazione4(String raw) {
        String s = nullSafeTrim(raw);
        if (s == null) return null;
        if (s.isEmpty()) return null;

        // all digits?
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                // keep as-is (e.g. "A50")
                return s;
            }
        }

        if (s.length() >= 4) {
            // if longer than 4 but numeric, keep as-is (or you can decide to trim left)
            return s;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (4 - s.length()); i++) sb.append('0');
        sb.append(s);
        return sb.toString();
    }

    private static String nullSafeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}