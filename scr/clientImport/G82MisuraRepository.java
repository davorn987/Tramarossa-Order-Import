package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class G82MisuraRepository {

    /**
     * Returns the ordered list of G82_KEY for a given G82_FK_1 (FK_1 from API).
     * Order by G82_SEQUENZA asc.
     */
    public List<String> listMisureKeysByFk1(Connection con, String fk1) throws SQLException {
        if (fk1 == null || fk1.trim().isEmpty()) return new ArrayList<>();
        String sql =
                "SELECT G82_KEY " +
                "FROM MADEVENETO.G82_MISURA " +
                "WHERE G82_FK_1 = ? " +
                "ORDER BY G82_SEQUENZA ASC";

        List<String> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fk1.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getString(1));
                }
            }
        }
        return out;
    }
}