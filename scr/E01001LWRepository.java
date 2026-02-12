package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class E01001LWRepository {

    public static class Filters {
        /** filter by E01_CD_CLIENTE (code) */
        public String cdCliente;

        /** filter by E01_CD_AGENTE (code) */
        public String cdAgente;

        public Integer anno;     // E01_ANNO_ORDINE
        public Integer daNr;     // E01_NR_ORDINE >= (default 0)
        public Integer aNr;      // E01_NR_ORDINE <= (null => +infinito)

        public Boolean chiuso;   // null=all, true='S', false='N'
        public Annullate annullate = Annullate.TUTTE;

        public boolean soloPrezziZero = false;
        public boolean escludiClienteInterno = false;

        public int limit = 500;
    }

    public enum Annullate {
        TUTTE, SENZA_A, SOLO_A
    }

    public List<E01_001LW_Row> listOrders(Connection con, Filters f) throws Exception {
        if (f == null) f = new Filters();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append("E01_DATA_ORDINE, E01_ANNO_ORDINE, E01_NR_ORDINE, ")
                .append("E01_CD_STAGIONE, E01_CD_DESTINAZIONE, E01_CD_CLIENTE, ")
                .append("DS_UTENTE_BUT, E01_MEMO, ")
                .append("QTA_TOT_ORDINE, QTA_TOT_SPEDITA, ASSEGNATO_ORDINE, ")
                .append("VAL_TOT_ORDINE, IMP_NETTO_ORD, ")
                .append("R07_RAGIONE_SOC, R10_RAGIONE_SOC, C07_RAG_SOC_AGENTE, ")
                .append("E01_CD_OPERATORE, E01_FLAG_CHIUSO, E01_CD_AGENTE, ")
                .append("R02_DS_NAZIONE, R06_DS_PAGAMENTO, ")
                .append("DATA_CONS_CONF, RIGHE_ANNULLATE, R07_CLIENTE_INTERNO ")
                .append("FROM E01_001LW WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // cliente (code)
        if (notBlank(f.cdCliente)) {
            sql.append(" AND E01_CD_CLIENTE = ? ");
            params.add(f.cdCliente.trim());
        }

        // agente (code)
        if (notBlank(f.cdAgente)) {
            sql.append(" AND E01_CD_AGENTE = ? ");
            params.add(f.cdAgente.trim());
        }

        // anno
        if (f.anno != null) {
            sql.append(" AND E01_ANNO_ORDINE = ? ");
            params.add(f.anno);
        }

        // range nr (empty -> 0/+inf handled by caller)
        if (f.daNr != null) {
            sql.append(" AND E01_NR_ORDINE >= ? ");
            params.add(f.daNr);
        }
        if (f.aNr != null) {
            sql.append(" AND E01_NR_ORDINE <= ? ");
            params.add(f.aNr);
        }

        // chiuso flag N/S
        if (f.chiuso != null) {
            sql.append(" AND NVL(E01_FLAG_CHIUSO,'N') = ? ");
            params.add(f.chiuso ? "S" : "N");
        }

        // righe annullate flag N/S
        if (f.annullate != null) {
            switch (f.annullate) {
                case SENZA_A:
                    sql.append(" AND NVL(RIGHE_ANNULLATE,'N') = 'N' ");
                    break;
                case SOLO_A:
                    sql.append(" AND NVL(RIGHE_ANNULLATE,'N') = 'S' ");
                    break;
                case TUTTE:
                default:
                    break;
            }
        }

        if (f.soloPrezziZero) {
            sql.append(" AND (NVL(IMP_NETTO_ORD,0) = 0 OR NVL(VAL_TOT_ORDINE,0) = 0) ");
        }

        if (f.escludiClienteInterno) {
            sql.append(" AND NVL(R07_CLIENTE_INTERNO,'N') = 'N' ");
        }

        sql.append(" ORDER BY E01_DATA_ORDINE DESC, E01_ANNO_ORDINE DESC, E01_NR_ORDINE DESC ");
        sql.append(" FETCH FIRST ? ROWS ONLY ");
        params.add(Math.max(1, Math.min(f.limit, 5000)));

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));

            List<E01_001LW_Row> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    E01_001LW_Row r = new E01_001LW_Row();
                    int c = 1;
                    r.e01DataOrdine = rs.getTimestamp(c++);
                    r.e01AnnoOrdine = getInt(rs, c++);
                    r.e01NrOrdine = getInt(rs, c++);
                    r.e01CdStagione = rs.getString(c++);
                    r.e01CdDestinazione = rs.getString(c++);
                    r.e01CdCliente = rs.getString(c++);

                    r.dsUtenteBut = rs.getString(c++);
                    r.e01Memo = rs.getString(c++);

                    r.qtaTotOrdine = getDouble(rs, c++);
                    r.qtaTotSpedita = getDouble(rs, c++);
                    r.assegnatoOrdine = getDouble(rs, c++);

                    r.valTotOrdine = getDouble(rs, c++);
                    r.impNettoOrd = getDouble(rs, c++);

                    r.r07RagioneSoc = rs.getString(c++);
                    r.r10RagioneSoc = rs.getString(c++);
                    r.c07RagSocAgente = rs.getString(c++);

                    r.e01CdOperatore = rs.getString(c++);
                    r.e01FlagChiuso = rs.getString(c++);
                    r.e01CdAgente = rs.getString(c++);

                    r.r02DsNazione = rs.getString(c++);
                    r.r06DsPagamento = rs.getString(c++);

                    r.dataConsConf = rs.getTimestamp(c++);
                    // now flags are N/S but we keep the raw field in DTO as Integer earlier.
                    // We'll store null here; we'll show raw string in UI from rs directly if you prefer.
                    // For now map to 0/1:
                    String righeAnn = rs.getString(c++);
                    r.righeAnnullate = ("S".equalsIgnoreCase(righeAnn)) ? 1 : 0;

                    r.r07ClienteInterno = rs.getString(c++);

                    out.add(r);
                }
            }
            return out;
        }
    }

    private static boolean notBlank(String s) { return s != null && !s.trim().isEmpty(); }

    private static Integer getInt(ResultSet rs, int idx) {
        try { int v = rs.getInt(idx); return rs.wasNull() ? null : v; } catch (Exception e) { return null; }
    }

    private static Double getDouble(ResultSet rs, int idx) {
        try { double v = rs.getDouble(idx); return rs.wasNull() ? null : v; } catch (Exception e) { return null; }
    }
}