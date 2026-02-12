package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class R07ClientiRepository {

    public List<UiOption> listClientiAttivi(Connection con) throws Exception {
        String sql =
                "SELECT R07_CD_CLIENTE, R07_RAGIONE_SOC " +
                "FROM R07_CLIENTI " +
                "WHERE R07_ST_RECORD = 'V' " +
                "ORDER BY R07_RAGIONE_SOC";
        List<UiOption> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new UiOption(rs.getString(1), rs.getString(2)));
            }
        }
        return out;
    }
}