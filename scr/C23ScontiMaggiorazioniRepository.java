package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Resolves customer discount code -> percentage.
 *
 * Rule:
 * - R07 has R07_CD_SCONTO_MAGGIORAZ
 * - lookup MADEVENETO.C23_SCONTI_MAGGIORAZIONI where C23_CD_SCONTO_MAGGIORAZ = code
 * - return C23_PERCENTUALE to store in E01_SCONTO_01
 */
public class C23ScontiMaggiorazioniRepository {

    public Double findPercentualeByCodice(Connection con, String cdScontoMaggioraz) throws SQLException {
        if (cdScontoMaggioraz == null || cdScontoMaggioraz.trim().isEmpty()) return null;

        String sql =
                "SELECT C23_PERCENTUALE " +
                "FROM MADEVENETO.C23_SCONTI_MAGGIORAZIONI " +
                "WHERE C23_CD_SCONTO_MAGGIORAZ = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cdScontoMaggioraz.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                double v = rs.getDouble(1);
                if (rs.wasNull()) return null;
                return v;
            }
        }
    }
}