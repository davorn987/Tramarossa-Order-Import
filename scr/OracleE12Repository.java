package clientImport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Inserts rows into MADEVENETO.E12_RIGHE_DETT_ORD_CLI (subset + arrays 1..41).
 */
public class OracleE12Repository {

    public int insert(Connection con, E12RigaDettOrdCli r) throws SQLException {
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();

        // fixed columns
        add(cols, vals, "E12_CD_AZIENDA");
        add(cols, vals, "E12_ANNO_ORDINE");
        add(cols, vals, "E12_NR_ORDINE");
        add(cols, vals, "E12_RIGA");
        add(cols, vals, "E12_ST_RECORD");
        add(cols, vals, "E12_ST_MODIFICA");
        add(cols, vals, "E12_CD_IVA");
        add(cols, vals, "E12_DATA_RIGA_ORDINE");
        add(cols, vals, "E12_DATA_CONS_CONFERMATA");
        add(cols, vals, "E12_FLAG_TIPO_RIGA");
        add(cols, vals, "E12_ID_LISTINO");
        add(cols, vals, "E12_ID_ACCORDO");
        add(cols, vals, "E12_PERC_ABBIN");
        add(cols, vals, "E12_CD_TIPO_ARTICOLO");
        add(cols, vals, "E12_FK_1");
        add(cols, vals, "E12_FK_2");
        add(cols, vals, "E12_FK_3");
        add(cols, vals, "E12_FK_6");
        add(cols, vals, "E12_NOTE");

        // COD01..COD41
        for (int i = 1; i <= 41; i++) add(cols, vals, String.format("E12_COD%02d", i));
        // QTAOR01..41
        for (int i = 1; i <= 41; i++) add(cols, vals, String.format("E12_QTAOR%02d", i));
        // QTAPR01..41
        for (int i = 1; i <= 41; i++) add(cols, vals, String.format("E12_QTAPR%02d", i));
        // QTASP01..41
        for (int i = 1; i <= 41; i++) add(cols, vals, String.format("E12_QTASP%02d", i));
        // PRUV01..41
        for (int i = 1; i <= 41; i++) add(cols, vals, String.format("E12_PRUV%02d", i));
        // PRUE01..41
        for (int i = 1; i <= 41; i++) add(cols, vals, String.format("E12_PRUE%02d", i));
        // FLGC01..41
        for (int i = 1; i <= 41; i++) add(cols, vals, String.format("E12_FLGC%02d", i));

        // tail flags
        add(cols, vals, "E12_FLAG_PRODUZIONE");
        add(cols, vals, "E12_FLAG_CHIUSO");
        add(cols, vals, "E12_SCONTO");

        String sql = "INSERT INTO MADEVENETO.E12_RIGHE_DETT_ORD_CLI (" + cols + ") VALUES (" + vals + ")";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int p = 1;

            ps.setString(p++, r.cdAzienda);
            ps.setInt(p++, r.annoOrdine);
            ps.setInt(p++, r.nrOrdine);
            ps.setInt(p++, r.riga);
            ps.setString(p++, r.stRecord);
            ps.setInt(p++, r.stModifica);
            ps.setString(p++, r.cdIva);
            ps.setTimestamp(p++, r.dataRigaOrdine);
            ps.setTimestamp(p++, r.dataConsConfermata);
            ps.setString(p++, r.flagTipoRiga);
            ps.setString(p++, r.idListino);
            ps.setString(p++, r.idAccordo);

            if (r.percAbbin == null) ps.setNull(p++, java.sql.Types.NUMERIC);
            else ps.setDouble(p++, r.percAbbin);

            ps.setString(p++, r.cdTipoArticolo);
            ps.setString(p++, r.fk1);
            ps.setString(p++, r.fk2);
            ps.setString(p++, r.fk3);
            ps.setString(p++, r.fk6);
            ps.setString(p++, r.note);

            // COD
            for (int i = 0; i < 41; i++) ps.setString(p++, r.cod[i]);
            // QTAOR
            for (int i = 0; i < 41; i++) ps.setInt(p++, r.qtaOr[i]);
            // QTAPR
            for (int i = 0; i < 41; i++) ps.setInt(p++, r.qtaPr[i]);
            // QTASP
            for (int i = 0; i < 41; i++) ps.setInt(p++, r.qtaSp[i]);
            // PRUV
            for (int i = 0; i < 41; i++) ps.setDouble(p++, r.prUv[i]);
            // PRUE
            for (int i = 0; i < 41; i++) ps.setDouble(p++, r.prUe[i]);
            // FLGC
            for (int i = 0; i < 41; i++) ps.setString(p++, r.flgC[i]);

            ps.setString(p++, r.flagProduzione);
            ps.setString(p++, r.flagChiuso);

            if (r.sconto == null) ps.setNull(p++, java.sql.Types.NUMERIC);
            else ps.setDouble(p++, r.sconto);

            return ps.executeUpdate();
        }
    }

    private void add(StringBuilder cols, StringBuilder vals, String col) {
        if (cols.length() > 0) {
            cols.append(",");
            vals.append(",");
        }
        cols.append(col);
        vals.append("?");
    }
}