package clientImport;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * DAO for:
 * - MADEVENETO.R07_CLIENTI
 * - MADEVENETO.R10_DESTINAZIONI_CLIENTE
 */
public class OracleClientRepository {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    public OracleClientRepository(String dbUrl, String dbUser, String dbPass) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    private Connection open() throws Exception {
        try { Class.forName("oracle.jdbc.driver.OracleDriver"); } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    public R07ClienteRecord findClienteById(String griffeIdCliente) throws Exception {
        if (griffeIdCliente == null || griffeIdCliente.trim().isEmpty()) return null;
        String id = griffeIdCliente.trim();

        String sql =
                "SELECT " +
                        // identity/anagraphics
                        "R07_CD_CLIENTE, R07_RAGIONE_SOC, R07_INDIRIZZO, R07_CAP, R07_CITTA, R07_PROVINCIA, R07_STATO, " +
                        "R07_PARTITA_IVA, R07_TELEFONO, " +
                        "R07_NOME, R07_COGNOME, R07_COD_INTERNO_CLIENTE, R07_EMAIL_ADDDRES, " +
                        // mapping fields
                        "R07_CD_PAGAMENTO, R07_CD_NAZIONE, R07_CD_LINGUA, R07_CD_ZONA, R07_HTTP, R07_CD_AGENTE, " +
                        "R07_CD_VALUTA, R07_CD_MOD_CONSEGNA, R07_CD_VETTORE, R07_CD_IVA, R07_CD_ABI, R07_CD_CAB, " +
                        "R07_CONTO_CORRENTE, R07_CD_TRASPORTO, R07_CD_IMBALLO, " +
                        // discount code
                        "R07_CD_SCONTO_MAGGIORAZ, " +
                        // listino group key
                        "R07_KEY_C32 " +
                        "FROM MADEVENETO.R07_CLIENTI " +
                        "WHERE R07_CD_CLIENTE = ?";

        try (Connection con = open();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                R07ClienteRecord r = new R07ClienteRecord();
                int i = 1;

                r.cdCliente = rs.getString(i++);
                r.ragioneSoc = rs.getString(i++);
                r.indirizzo = rs.getString(i++);
                r.cap = rs.getString(i++);
                r.citta = rs.getString(i++);
                r.provincia = rs.getString(i++);
                r.stato = rs.getString(i++);
                r.partitaIva = rs.getString(i++);
                r.telefono = rs.getString(i++);

                r.nome = rs.getString(i++);
                r.cognome = rs.getString(i++);
                r.codInterno = rs.getString(i++);
                r.email = rs.getString(i++);

                r.cdPagamento = rs.getString(i++);
                r.cdNazione = rs.getString(i++);
                r.cdLingua = rs.getString(i++);
                r.cdZona = rs.getString(i++);
                r.http = rs.getString(i++);
                r.cdAgente = rs.getString(i++);

                r.cdValuta = rs.getString(i++);
                r.cdModConsegna = rs.getString(i++);
                r.cdVettore = rs.getString(i++);
                r.cdIva = rs.getString(i++);
                r.cdAbi = rs.getString(i++);
                r.cdCab = rs.getString(i++);

                r.contoCorrente = rs.getString(i++);
                r.cdTrasporto = rs.getString(i++);
                r.cdImballo = rs.getString(i++);

                r.cdScontoMaggioraz = rs.getString(i++);
                r.keyC32 = rs.getString(i++);

                return r;
            }
        }
    }

    public R10DestinazioneRecord findDestinazione(String griffeIdCliente, String griffeIdSpedizione) throws Exception {
        if (griffeIdCliente == null || griffeIdCliente.trim().isEmpty()) return null;
        if (griffeIdSpedizione == null || griffeIdSpedizione.trim().isEmpty()) return null;

        String cliente = griffeIdCliente.trim();
        String dest = griffeIdSpedizione.trim();

        String sql =
                "SELECT " +
                        "R10_CD_CLIENTE, R10_CD_DESTINAZIONE, R10_RAGIONE_SOC, R10_INDIRIZZO, R10_CAP, R10_CITTA, " +
                        "R10_STATO, R10_TELEFONO, R10_EMAIL_ADDDRES, R10_CD_NAZIONE, R10_CD_AZIENDA " +
                        "FROM MADEVENETO.R10_DESTINAZIONI_CLIENTE " +
                        "WHERE R10_CD_CLIENTE = ? AND R10_CD_DESTINAZIONE = ?";

        try (Connection con = open();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente);
            ps.setString(2, dest);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                R10DestinazioneRecord r = new R10DestinazioneRecord();
                int i = 1;
                r.cdCliente = rs.getString(i++);
                r.cdDestinazione = rs.getString(i++);
                r.ragioneSoc = rs.getString(i++);
                r.indirizzo = rs.getString(i++);
                r.cap = rs.getString(i++);
                r.citta = rs.getString(i++);
                r.stato = rs.getString(i++);
                r.telefono = rs.getString(i++);
                r.email = rs.getString(i++);
                r.cdNazione = rs.getString(i++);
                r.cdAzienda = rs.getString(i++);

                return r;
            }
        }
    }
}