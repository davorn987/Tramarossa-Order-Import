package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Reads R04_CAMBI for exchange rates.
 *
 * Rule:
 * - match currency: R04_CD_VALUTA = <currency>
 * - pick record with R04_DATA_CAMBIO nearest to "today" (we implement as: max date <= today)
 *
 * If no date <= today exists, we fallback to: min date >= today (closest future).
 */
public class OracleExchangeRepository {

    public R04CambioRecord findCambioNearestToToday(Connection con, String cdValuta) throws SQLException {
        if (cdValuta == null || cdValuta.trim().isEmpty()) return null;
        String valuta = cdValuta.trim();

        // 1) prefer latest past rate (max date <= today)
        String sqlPast =
                "SELECT R04_CD_VALUTA, R04_DATA_CAMBIO, R04_IMPORTO_CAMBIO, R04_IMPORTO_CAMBIO_EURO " +
                "FROM MADEVENETO.R04_CAMBI " +
                "WHERE R04_CD_VALUTA = ? AND R04_DATA_CAMBIO <= TRUNC(SYSDATE) " +
                "ORDER BY R04_DATA_CAMBIO DESC";

        try (PreparedStatement ps = con.prepareStatement(sqlPast)) {
            ps.setString(1, valuta);
            ps.setFetchSize(1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }

        // 2) fallback: closest future (min date >= today)
        String sqlFuture =
                "SELECT R04_CD_VALUTA, R04_DATA_CAMBIO, R04_IMPORTO_CAMBIO, R04_IMPORTO_CAMBIO_EURO " +
                "FROM MADEVENETO.R04_CAMBI " +
                "WHERE R04_CD_VALUTA = ? AND R04_DATA_CAMBIO >= TRUNC(SYSDATE) " +
                "ORDER BY R04_DATA_CAMBIO ASC";

        try (PreparedStatement ps = con.prepareStatement(sqlFuture)) {
            ps.setString(1, valuta);
            ps.setFetchSize(1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }

        return null;
    }

    private R04CambioRecord map(ResultSet rs) throws SQLException {
        R04CambioRecord r = new R04CambioRecord();
        r.cdValuta = rs.getString(1);
        r.dataCambio = rs.getTimestamp(2);
        r.importoCambio = getDoubleOrNull(rs, 3);
        r.importoCambioEuro = getDoubleOrNull(rs, 4);
        return r;
    }

    private Double getDoubleOrNull(ResultSet rs, int col) throws SQLException {
        double v = rs.getDouble(col);
        if (rs.wasNull()) return null;
        return v;
    }
}