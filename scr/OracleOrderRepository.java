package clientImport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Inserts into E01_TESTATA_ORDINI_CLI and updates BPR_PROGRESSIVI.
 */
public class OracleOrderRepository {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    private final BprProgressiviRepository progressiviRepo = new BprProgressiviRepository();
    private final OracleExchangeRepository exchangeRepo = new OracleExchangeRepository();

    public OracleOrderRepository(String dbUrl, String dbUser, String dbPass) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    private Connection open() throws Exception {
        try { Class.forName("oracle.jdbc.driver.OracleDriver"); } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    /**
     * Creates a new E01 order header:
     * - allocates E01_NR_ORDINE using BPR_PROGRESSIVI (CD_TABELLA_BPR='E01', CHIAVE_PARZIALE_BPR=year)
     * - fills exchange rates from R04_CAMBI based on currency (E01_CD_VALUTA)
     * - inserts E01 record
     * - updates BPR_PROGRESSIVI to the new value
     *
     * Returns assigned E01_NR_ORDINE.
     */
    public int insertTestataWithProgressivo(E01TestataOrdineCli e01) throws Exception {
        if (e01 == null) throw new IllegalArgumentException("e01 is null");
        if (e01.getE01AnnoOrdine() == null) throw new IllegalArgumentException("E01_ANNO_ORDINE is null");

        String annoKey = String.valueOf(e01.getE01AnnoOrdine());

        try (Connection con = open()) {
            con.setAutoCommit(false);
            try {
                // allocate progressive
                int nextNr = progressiviRepo.nextProgressivo(con, "E01", annoKey);
                e01.setE01NrOrdine(nextNr);

                // exchange rate (if currency exists)
                if (e01.getE01CdValuta() != null && !e01.getE01CdValuta().trim().isEmpty()) {
                    R04CambioRecord cambio = exchangeRepo.findCambioNearestToToday(con, e01.getE01CdValuta());
                    if (cambio != null) {
                        e01.setE01ImportoCambio(cambio.importoCambio);
                        e01.setE01ImportoCambioEuro(cambio.importoCambioEuro);
                    }
                }

                // insert E01
                int ins = insertTestata(con, e01);
                if (ins != 1) throw new SQLException("Insert E01 affected " + ins + " rows");

                // update progressive
                int upd = progressiviRepo.updateProgressivo(con, "E01", annoKey, nextNr);
                if (upd != 1) throw new SQLException("Update BPR_PROGRESSIVI affected " + upd + " rows");

                con.commit();
                return nextNr;
            } catch (Exception ex) {
                try { con.rollback(); } catch (Exception ignore) {}
                throw ex;
            }
        }
    }

    private int insertTestata(Connection con, E01TestataOrdineCli e01) throws SQLException {
        String sql =
                "INSERT INTO MADEVENETO.E01_TESTATA_ORDINI_CLI (" +
                        "E01_ANNO_ORDINE, E01_NR_ORDINE, E01_DATA_ORDINE, E01_DATA_IMMISSIONE, " +
                        "E01_CD_CLIENTE, E01_CD_CLIENTE_SPED, E01_RIFER_CLIENTE, E01_DT_RIFER_CLIENTE, " +
                        "E01_CD_AGENTE, E01_CD_IVA, " +
                        "E01_CD_ABI, E01_CD_CAB, E01_CD_ZONA, " +
                        "E01_CD_PAGAMENTO, E01_CD_VALUTA, " +
                        "E01_CD_SPEDIZIONIERE, E01_CD_VETTORE, E01_CD_TRASPORTO, E01_CD_LINGUA, " +
                        "E01_CD_OPERATORE, " +
                        "E01_IMPORTO_CAMBIO, E01_IMPORTO_CAMBIO_EURO, " +
                        "E01_CD_MAGAZZINO, E01_MEMO, " +
                        "E01_FLAG_ACCUM_BOLLE, E01_FLAG_MIT_DEST_VETT, E01_FLAG_STAMPA, E01_FLAG_CHIUSO, " +
                        "E01_ORD_INTERNO, E01_FLAG_CLAV, E01_CD_DESTINAZIONE, E01_FLAG_IMPOSTA, " +
                        "E01_CD_STAGIONE, " +
                        "E01_PERC_AGE1, E01_PERC_AGE2, E01_PERC_AGE3, E01_PERC_AGE4, " +
                        "E01_TIPO_ORDINE, " +
                        "E01_CD_AZIENDA, E01_FLG_FLASH, E01_ID_LISTINO, E01_ACCURATEZZA, E01_ACCUR_VAR, " +
                        "SD_DT_INS, E01_CD_UTENTE, E01_FLAG_DA_CONFERMARE, E01_FLAG_ORD_PROD" +
                ") VALUES (" +
                        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                ")";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;

            ps.setInt(i++, e01.getE01AnnoOrdine());
            ps.setInt(i++, e01.getE01NrOrdine());
            ps.setTimestamp(i++, e01.getE01DataOrdine());
            ps.setTimestamp(i++, e01.getE01DataImmissione()); // NULL ok

            ps.setString(i++, e01.getE01CdCliente());
            ps.setString(i++, e01.getE01CdClienteSped()); // NULL ok
            ps.setString(i++, e01.getE01RiferCliente());  // NULL ok
            ps.setTimestamp(i++, e01.getE01DtRiferCliente());

            ps.setString(i++, e01.getE01CdAgente());
            ps.setString(i++, e01.getE01CdIva());

            ps.setString(i++, e01.getE01CdAbi());
            ps.setString(i++, e01.getE01CdCab());
            ps.setString(i++, e01.getE01CdZona());

            ps.setString(i++, e01.getE01CdPagamento());
            ps.setString(i++, e01.getE01CdValuta());

            ps.setString(i++, e01.getE01CdSpedizioniere());
            ps.setString(i++, e01.getE01CdVettore());
            ps.setString(i++, e01.getE01CdTrasporto());
            ps.setString(i++, e01.getE01CdLingua());

            ps.setString(i++, e01.getE01CdOperatore());

            // cambio
            if (e01.getE01ImportoCambio() != null) ps.setDouble(i++, e01.getE01ImportoCambio());
            else ps.setNull(i++, java.sql.Types.NUMERIC);

            if (e01.getE01ImportoCambioEuro() != null) ps.setDouble(i++, e01.getE01ImportoCambioEuro());
            else ps.setNull(i++, java.sql.Types.NUMERIC);

            ps.setString(i++, e01.getE01CdMagazzino());
            ps.setString(i++, e01.getE01Memo());

            ps.setString(i++, e01.getE01FlagAccumBolle());
            ps.setString(i++, e01.getE01FlagMitDestVett());
            ps.setString(i++, e01.getE01FlagStampa());
            ps.setString(i++, e01.getE01FlagChiuso());

            ps.setString(i++, e01.getE01OrdInterno());
            ps.setString(i++, e01.getE01FlagClav());
            ps.setString(i++, e01.getE01CdDestinazione());
            ps.setString(i++, e01.getE01FlagImposta());

            ps.setString(i++, e01.getE01CdStagione());

            ps.setDouble(i++, e01.getE01PercAge1() == null ? 0.0 : e01.getE01PercAge1());
            ps.setDouble(i++, e01.getE01PercAge2() == null ? 0.0 : e01.getE01PercAge2());
            ps.setDouble(i++, e01.getE01PercAge3() == null ? 0.0 : e01.getE01PercAge3());
            ps.setDouble(i++, e01.getE01PercAge4() == null ? 0.0 : e01.getE01PercAge4());

            ps.setString(i++, e01.getE01TipoOrdine());

            ps.setString(i++, e01.getE01CdAzienda());
            ps.setString(i++, e01.getE01FlgFlash());
            ps.setString(i++, e01.getE01IdListino());
            ps.setString(i++, e01.getE01Accuratezza());
            ps.setString(i++, e01.getE01AccurVar());

            ps.setTimestamp(i++, e01.getSdDtIns());
            ps.setString(i++, e01.getE01CdUtente());
            ps.setString(i++, e01.getE01FlagDaConfermare());
            ps.setString(i++, e01.getE01FlagOrdProd());

            return ps.executeUpdate();
        }
    }
}