package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Low-level insert for MADEVENETO.E01_TESTATA_ORDINI_CLI.
 *
 * NOTE: column order aligned to your required start:
 * E01_ANNO_ORDINE, E01_NR_ORDINE, E01_ST_RECORD, E01_ST_MODIFICA, E01_DATA_ORDINE, ...
 */
public class OracleE01Repository {

    public int insert(Connection con, E01TestataOrdineCli e01) throws SQLException {
        String sql =
                "INSERT INTO MADEVENETO.E01_TESTATA_ORDINI_CLI (" +
                        // REQUIRED initial order
                        "E01_ANNO_ORDINE, E01_NR_ORDINE, E01_ST_RECORD, E01_ST_MODIFICA, E01_DATA_ORDINE, " +

                        // rest (unchanged)
                        "E01_DATA_IMMISSIONE, " +
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
                        "E01_SCONTO_01, " +
                        "E01_PERC_AGE1, E01_PERC_AGE2, E01_PERC_AGE3, E01_PERC_AGE4, " +
                        "E01_TIPO_ORDINE, " +
                        "E01_CD_AZIENDA, E01_FLG_FLASH, E01_ID_LISTINO, E01_ACCURATEZZA, E01_ACCUR_VAR, " +
                        "SD_DT_INS, E01_CD_UTENTE, E01_FLAG_DA_CONFERMARE, E01_FLAG_ORD_PROD" +
                ") VALUES (" +
                        // total placeholders = total columns (same as before)
                        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                ")";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;

            // REQUIRED initial order
            ps.setInt(i++, e01.getE01AnnoOrdine());
            ps.setInt(i++, e01.getE01NrOrdine());
            ps.setString(i++, e01.getE01StRecord() == null ? "V" : e01.getE01StRecord());
            ps.setInt(i++, e01.getE01StModifica() == null ? 0 : e01.getE01StModifica());
            ps.setTimestamp(i++, e01.getE01DataOrdine());

            // rest (same values as before, just shifted by +2)
            ps.setTimestamp(i++, e01.getE01DataImmissione());

            ps.setString(i++, e01.getE01CdCliente());
            ps.setString(i++, e01.getE01CdClienteSped());
            ps.setString(i++, e01.getE01RiferCliente());
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

            if (e01.getE01Sconto01() != null) ps.setDouble(i++, e01.getE01Sconto01());
            else ps.setNull(i++, java.sql.Types.NUMERIC);

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