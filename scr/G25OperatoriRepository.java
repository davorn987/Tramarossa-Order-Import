package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class G25OperatoriRepository {

    public List<G25OperatoreRecord> listActiveOperatori(Connection con) throws Exception {
        String sql =
                "SELECT G25_CD_OPERATORE, G25_ST_RECORD, G25_DS_OPERATORE " +
                "FROM G25_OPERATORI " +
                "WHERE G25_ST_RECORD = 'V' " +
                "ORDER BY G25_DS_OPERATORE";

        List<G25OperatoreRecord> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    G25OperatoreRecord r = new G25OperatoreRecord();
                    r.cdOperatore = rs.getString(1);
                    r.stRecord = rs.getString(2);
                    r.dsOperatore = rs.getString(3);
                    out.add(r);
                }
            }
        }
        return out;
    }
}