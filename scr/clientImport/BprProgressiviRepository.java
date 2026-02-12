package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles BPR_PROGRESSIVI for progressive order numbering.
 *
 * You specified:
 * - CD_TABELLA_BPR = 'E01'
 * - CHIAVE_PARZIALE_BPR = <anno corrente>
 * - PROGRESSIVO_BPR = last used
 *
 * We will:
 * - read current progressive
 * - next = current + 1
 * - update to next (after successful insert)
 */
public class BprProgressiviRepository {

    public int getProgressivo(Connection con, String cdTabella, String chiaveParziale) throws SQLException {
        String sql = "SELECT PROGRESSIVO_BPR " +
                     "FROM MADEVENETO.BPR_PROGRESSIVI " +
                     "WHERE CD_TABELLA_BPR = ? AND CHIAVE_PARZIALE_BPR = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cdTabella);
            ps.setString(2, chiaveParziale);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("BPR_PROGRESSIVI not found for CD_TABELLA_BPR=" + cdTabella +
                            " CHIAVE_PARZIALE_BPR=" + chiaveParziale);
                }
                return rs.getInt(1);
            }
        }
    }

    public int nextProgressivo(Connection con, String cdTabella, String chiaveParziale) throws SQLException {
        int cur = getProgressivo(con, cdTabella, chiaveParziale);
        return cur + 1;
    }

    public int updateProgressivo(Connection con, String cdTabella, String chiaveParziale, int newProgressivo) throws SQLException {
        String sql = "UPDATE MADEVENETO.BPR_PROGRESSIVI " +
                     "SET PROGRESSIVO_BPR = ? " +
                     "WHERE CD_TABELLA_BPR = ? AND CHIAVE_PARZIALE_BPR = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newProgressivo);
            ps.setString(2, cdTabella);
            ps.setString(3, chiaveParziale);
            return ps.executeUpdate();
        }
    }
}
