package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resolves listino id from client listino group (C32) + season (C30).
 *
 * Tables involved:
 * - MADEVENETO.C30_TEST_LIST_CLI: C30_KEY_C32, C30_CD_STAGIONE, C30_ST_RECORD, C30_ID_LISTINO
 */
public class OracleListinoRepository {

    /**
     * Find the listino id for a given group and season (only active records C30_ST_RECORD='V').
     *
     * @return C30_ID_LISTINO or null if not found
     */
    public Integer findListinoIdByGroupAndSeason(Connection con, String keyC32, String stagione) throws Exception {
        if (con == null) throw new IllegalArgumentException("con is null");
        if (keyC32 == null || keyC32.trim().isEmpty()) return null;
        if (stagione == null || stagione.trim().isEmpty()) return null;

        String sql =
                "SELECT C30_ID_LISTINO " +
                "FROM MADEVENETO.C30_TEST_LIST_CLI " +
                "WHERE C30_ST_RECORD = 'V' " +
                "  AND C30_CD_STAGIONE = ? " +
                "  AND C30_KEY_C32 = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, stagione.trim());
            ps.setString(2, keyC32.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int v = rs.getInt(1);
                    if (rs.wasNull()) return null;
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * Debug helper: list all listini for a given group (all seasons), only active records C30_ST_RECORD='V'.
     * Returns a map season -> listinoId preserving DB order (season asc).
     */
    public Map<String, Integer> listAllListiniByGroup(Connection con, String keyC32) throws Exception {
        if (con == null) throw new IllegalArgumentException("con is null");
        if (keyC32 == null || keyC32.trim().isEmpty()) return new LinkedHashMap<>();

        String sql =
                "SELECT C30_CD_STAGIONE, C30_ID_LISTINO " +
                "FROM MADEVENETO.C30_TEST_LIST_CLI " +
                "WHERE C30_ST_RECORD = 'V' " +
                "  AND C30_KEY_C32 = ? " +
                "ORDER BY C30_CD_STAGIONE";

        Map<String, Integer> out = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, keyC32.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String stagione = rs.getString(1);
                    int id = rs.getInt(2);
                    Integer idBox = rs.wasNull() ? null : id;
                    out.put(stagione, idBox);
                }
            }
        }
        return out;
    }
}