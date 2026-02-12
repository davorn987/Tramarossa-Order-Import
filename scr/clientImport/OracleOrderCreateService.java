package clientImport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class OracleOrderCreateService {

    public static final String FALLBACK_CLIENTE = "000000";

    /** Business default listino when nothing is found */
    public static final int FALLBACK_ID_LISTINO = 57;

    /** Current season used to resolve listino */
    public static final String CURRENT_STAGIONE = "AI26";

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    private final BprProgressiviRepository progressiviRepo = new BprProgressiviRepository();
    private final OracleExchangeRepository exchangeRepo = new OracleExchangeRepository();
    private final OracleE01Repository e01Repo = new OracleE01Repository();
    private final E12OrderLinesService e12Service = new E12OrderLinesService();
    private final C23ScontiMaggiorazioniRepository c23Repo = new C23ScontiMaggiorazioniRepository();

    private final OracleClientRepository clientRepo;
    private final OracleListinoRepository listinoRepo = new OracleListinoRepository();

    public OracleOrderCreateService(String dbUrl, String dbUser, String dbPass) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        this.clientRepo = new OracleClientRepository(dbUrl, dbUser, dbPass);
    }

    private Connection open() throws Exception {
        try { Class.forName("oracle.jdbc.driver.OracleDriver"); } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    public CreateOrderResult createOrderFromGF(E01TestataOrdineCli e01, GF224EntryPreview gf, R07ClienteRecord r07FromCaller) throws Exception {
        if (e01 == null) throw new IllegalArgumentException("e01 is null");
        if (gf == null) throw new IllegalArgumentException("gf is null");
        if (e01.getE01AnnoOrdine() == null) throw new IllegalArgumentException("E01_ANNO_ORDINE is null");

        String annoKey = String.valueOf(e01.getE01AnnoOrdine());

        // Keep currency/listino codes based on GF selection (optional; ID_LISTINO is resolved from C30/C32 below)
        ListinoRules.Result lr = ListinoRules.resolve(gf.listino);
        e01.setE01CdValuta(lr.e01CdValuta);
        e01.setE01CdListino(lr.e01CdListino);

        // Ensure season is set (used for listino resolution)
        if (isBlank(e01.getE01CdStagione())) {
            e01.setE01CdStagione(CURRENT_STAGIONE);
        }

        // Resolve customer/destination and apply customer fields (IVA + others)
        ResolvedCustomer rc = resolveCustomerAndDestinationAndApply(e01, r07FromCaller);
        e01.setE01CdCliente(rc.cdClienteFinal);
        e01.setE01CdDestinazione(rc.cdDestinazioneFinal);

        try (Connection con = open()) {
            con.setAutoCommit(false);
            try {
                // Allocate progressive
                int nextNr = progressiviRepo.nextProgressivo(con, "E01", annoKey);
                e01.setE01NrOrdine(nextNr);

                // Resolve E01_ID_LISTINO by: R07_KEY_C32 + season (AI26) + C30_ST_RECORD='V', fallback 57
                resolveAndSetIdListino(con, e01, rc);

                // Exchange rates from R04 based on E01_CD_VALUTA
                if (!isBlank(e01.getE01CdValuta())) {
                    R04CambioRecord cambio = exchangeRepo.findCambioNearestToToday(con, e01.getE01CdValuta());
                    if (cambio != null) {
                        e01.setE01ImportoCambio(cambio.importoCambio);
                        e01.setE01ImportoCambioEuro(cambio.importoCambioEuro);
                    }
                }

                // Discount only if client exists in R07
                if (rc.r07Effective != null && !isBlank(rc.r07Effective.cdScontoMaggioraz)) {
                    Double perc = c23Repo.findPercentualeByCodice(con, rc.r07Effective.cdScontoMaggioraz);
                    e01.setE01Sconto01(perc);
                } else {
                    e01.setE01Sconto01(null);
                }

                // Insert header
                int insE01 = e01Repo.insert(con, e01);
                if (insE01 != 1) throw new SQLException("Insert E01 affected " + insE01 + " rows");

                // Insert lines with listino-dependent price mode
                int insE12 = e12Service.insertLines(con, e01, gf, lr.priceMode);

                // Update progressive
                int upd = progressiviRepo.updateProgressivo(con, "E01", annoKey, nextNr);
                if (upd != 1) throw new SQLException("Update BPR_PROGRESSIVI affected " + upd + " rows");

                con.commit();

                CreateOrderResult res = new CreateOrderResult();
                res.annoOrdine = e01.getE01AnnoOrdine();
                res.nrOrdine = e01.getE01NrOrdine();
                res.insertedE12Lines = insE12;
                res.sconto01 = e01.getE01Sconto01();

                res.usedFallbackClient000000 = FALLBACK_CLIENTE.equals(rc.cdClienteFinal);
                res.destinationDropped = isBlank(rc.cdDestinazioneFinal);
                res.r07Found = (rc.r07Effective != null);
                res.r10Found = rc.r10Found;

                // debug: group/listini
                res.keyC32 = (rc.r07Effective == null) ? null : safeTrim(rc.r07Effective.keyC32);
                res.idListinoResolved = safeParseIntOrNull(e01.getE01IdListino());
                res.idListinoFallbackUsed = (res.idListinoResolved != null && res.idListinoResolved == FALLBACK_ID_LISTINO);
                res.allListiniByGroup = rc.allListiniByGroup;

                // debug: final E01 mapped fields (customer-driven)
                res.e01CdIvaFinal = e01.getE01CdIva();
                res.e01CdAgenteFinal = e01.getE01CdAgente();
                res.e01CdPagamentoFinal = e01.getE01CdPagamento();
                res.e01CdValutaFinal = e01.getE01CdValuta();
                res.e01CdAbiFinal = e01.getE01CdAbi();
                res.e01CdCabFinal = e01.getE01CdCab();
                res.e01CdTrasportoFinal = e01.getE01CdTrasporto();
                res.e01CdVettoreFinal = e01.getE01CdVettore();
                res.e01CdSpedizioniereFinal = e01.getE01CdSpedizioniere();
                res.e01CdLinguaFinal = e01.getE01CdLingua();
                res.e01CdZonaFinal = e01.getE01CdZona();

                return res;
            } catch (Exception ex) {
                try { con.rollback(); } catch (Exception ignore) {}
                throw ex;
            }
        }
    }

    private void resolveAndSetIdListino(Connection con, E01TestataOrdineCli e01, ResolvedCustomer rc) throws Exception {
        int resolved = FALLBACK_ID_LISTINO;

        if (rc != null && rc.r07Effective != null) {
            String stagione = safeTrim(e01.getE01CdStagione());
            if (stagione == null) stagione = CURRENT_STAGIONE;

            String keyC32 = safeTrim(rc.r07Effective.keyC32);

            if (keyC32 != null) {
                rc.allListiniByGroup = listinoRepo.listAllListiniByGroup(con, keyC32);
            }

            Integer id = null;
            if (keyC32 != null && stagione != null) {
                id = listinoRepo.findListinoIdByGroupAndSeason(con, keyC32, stagione);
            }
            if (id != null) resolved = id;
        }

        e01.setE01IdListino(String.valueOf(resolved));
    }

    /**
     * Resolves R07 (client) and R10 (destination existence) and applies:
     * - fallback client rules (000000 + IVA ".")
     * - for real clients, copies all relevant customer fields into E01 header
     */
    private ResolvedCustomer resolveCustomerAndDestinationAndApply(E01TestataOrdineCli e01, R07ClienteRecord r07FromCaller) {
        ResolvedCustomer out = new ResolvedCustomer();

        String clientId = safeTrim(e01.getE01CdCliente());
        String destId = safeTrim(e01.getE01CdDestinazione());
        destId = E01Builder.normalizeDestinazione4(destId);

        // 1) ensure client exists
        R07ClienteRecord r07 = r07FromCaller;
        if (r07 == null && clientId != null) {
            try { r07 = clientRepo.findClienteById(clientId); } catch (Exception ignored) { r07 = null; }
        }

        if (r07 == null) {
            // fallback client (NOT found)
            out.cdClienteFinal = FALLBACK_CLIENTE;
            out.cdDestinazioneFinal = null;
            out.r07Effective = null;
            out.r10Found = false;

            // placeholder IVA ONLY for fallback
            e01.setE01CdIva(".");

            // clear customer-derived fields to avoid invalid codes
            e01.setE01CdAgente(null);
            e01.setE01CdPagamento(null);
            e01.setE01CdValuta(null);
            e01.setE01CdAbi(null);
            e01.setE01CdCab(null);
            e01.setE01CdTrasporto(null);
            e01.setE01CdVettore(null);
            e01.setE01CdSpedizioniere(null);
            e01.setE01CdLingua(null);
            e01.setE01CdZona(null);

            return out;
        }

        // real client
        out.r07Effective = r07;
        out.cdClienteFinal = clientId;

        // copy all relevant customer fields into E01 header
        applyR07ToHeader(e01, r07);

        // 2) destination logic: keep dest only if it exists
        if (destId == null || destId.isEmpty()) {
            out.cdDestinazioneFinal = null;
            out.r10Found = false;
            return out;
        }

        try {
            R10DestinazioneRecord r10 = clientRepo.findDestinazione(clientId, destId);
            if (r10 == null) {
                out.cdDestinazioneFinal = null;
                out.r10Found = false;
            } else {
                out.cdDestinazioneFinal = destId;
                out.r10Found = true;
            }
        } catch (Exception ex) {
            out.cdDestinazioneFinal = null;
            out.r10Found = false;
        }

        return out;
    }

    /**
     * Copies customer master data fields into E01 header.
     * These are the fields you typically want coherent in the order header.
     */
    private void applyR07ToHeader(E01TestataOrdineCli e01, R07ClienteRecord r07) {
        if (e01 == null || r07 == null) return;

        // Required / core
        e01.setE01CdIva(safeTrim(r07.cdIva));
        e01.setE01CdAgente(safeTrim(r07.cdAgente));

        // Common useful mappings (enable now as requested)
        e01.setE01CdPagamento(safeTrim(r07.cdPagamento));
        // NOTE: cdValuta is also affected by ListinoRules.resolve(gf.listino) earlier.
        // If you want DB to always win over GF, keep this line AFTER resolve(...) (it is, because we call here later).
        e01.setE01CdValuta(safeTrim(r07.cdValuta));

        e01.setE01CdAbi(safeTrim(r07.cdAbi));
        e01.setE01CdCab(safeTrim(r07.cdCab));

        e01.setE01CdTrasporto(safeTrim(r07.cdTrasporto));
        e01.setE01CdVettore(safeTrim(r07.cdVettore));

        // Not present on R07 in your DTO: cdSpedizioniere. If you want, map from another table/rule.
        // For now keep whatever E01Builder set (usually null).
        // e01.setE01CdSpedizioniere(...);

        e01.setE01CdLingua(safeTrim(r07.cdLingua));
        e01.setE01CdZona(safeTrim(r07.cdZona));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Integer safeParseIntOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try { return Integer.parseInt(t); } catch (Exception ex) { return null; }
    }

    private static class ResolvedCustomer {
        String cdClienteFinal;
        String cdDestinazioneFinal;
        R07ClienteRecord r07Effective;
        boolean r10Found;

        Map<String, Integer> allListiniByGroup;
    }

    public static class CreateOrderResult {
        public int annoOrdine;
        public int nrOrdine;
        public int insertedE12Lines;
        public Double sconto01;

        public boolean usedFallbackClient000000;
        public boolean destinationDropped;
        public boolean r07Found;
        public boolean r10Found;

        // listino debug
        public String keyC32;
        public Integer idListinoResolved;
        public boolean idListinoFallbackUsed;
        public Map<String, Integer> allListiniByGroup;

        // E01 final mapped fields (customer-driven)
        public String e01CdIvaFinal;
        public String e01CdAgenteFinal;
        public String e01CdPagamentoFinal;
        public String e01CdValutaFinal;
        public String e01CdAbiFinal;
        public String e01CdCabFinal;
        public String e01CdTrasportoFinal;
        public String e01CdVettoreFinal;
        public String e01CdSpedizioniereFinal;
        public String e01CdLinguaFinal;
        public String e01CdZonaFinal;
    }
}