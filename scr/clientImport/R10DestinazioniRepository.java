package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists destinations (R10) for a client for UI selection.
 */
public class R10DestinazioniRepository {

    public List<UiOption> listDestinazioniByCliente(Connection con, String cdCliente) throws Exception {
        if (cdCliente == null || cdCliente.trim().isEmpty()) return new ArrayList<>();
        String sql =
                "SELECT R10_CD_DESTINAZIONE, R10_RAGIONE_SOC " +
                "FROM MADEVENETO.R10_DESTINAZIONI_CLIENTE " +
                "WHERE R10_CD_CLIENTE = ? " +
                "ORDER BY R10_CD_DESTINAZIONE";

        List<UiOption> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cdCliente.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new UiOption(rs.getString(1), rs.getString(2)));
                }
            }
        }
        return out;
    }
}