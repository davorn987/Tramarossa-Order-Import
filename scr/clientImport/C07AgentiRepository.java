package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class C07AgentiRepository {

    public List<UiOption> listAgentiAttivi(Connection con) throws Exception {
        String sql =
                "SELECT C07_CD_AGENTE, C07_RAG_SOC_AGENTE " +
                "FROM C07_AGENTI " +
                "WHERE C07_ST_RECORD = 'V' " +
                "ORDER BY C07_RAG_SOC_AGENTE";
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