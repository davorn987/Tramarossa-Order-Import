package clientImport.ui.dialogs;

import javax.swing.*;

import clientImport.E01Builder;
import clientImport.OracleClientRepository;
import clientImport.OracleListinoRepository;
import clientImport.OracleOrderCreateService;
import clientImport.R07ClienteRecord;
import clientImport.R10DestinazioneRecord;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

/**
 * Small GUI tool to inspect customer data coming from Oracle (R07 + optional R10),
 * including listino group (R07_KEY_C32) and listini associated (C30) for AI26 and all seasons.
 */
public class CustomerDebugDialog extends JDialog {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    private JTextField txtCliente;
    private JTextField txtDest;
    private JButton btnLoad;
    private JTextArea out;

    public CustomerDebugDialog(JFrame owner, String dbUrl, String dbUser, String dbPass) {
        super(owner, "Customer Debug (R07/R10/Listino)", true);
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;

        setSize(980, 650);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("Cd Cliente:"));
        txtCliente = new JTextField(12);
        top.add(txtCliente);

        top.add(new JLabel("Dest (opt):"));
        txtDest = new JTextField(6);
        top.add(txtDest);

        btnLoad = new JButton("Load");
        top.add(btnLoad);

        add(top, BorderLayout.NORTH);

        out = new JTextArea(30, 140);
        out.setEditable(false);
        out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(out), BorderLayout.CENTER);

        btnLoad.addActionListener(e -> onLoad());
    }

    private Connection open() throws Exception {
        try { Class.forName("oracle.jdbc.driver.OracleDriver"); } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    private void onLoad() {
        String cd = txtCliente.getText() == null ? "" : txtCliente.getText().trim();
        String dest = txtDest.getText() == null ? "" : txtDest.getText().trim();
        if (cd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci un codice cliente.", "Customer Debug", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnLoad.setEnabled(false);
        out.setText("");

        SwingWorker<Void, Void> w = new SwingWorker<Void, Void>() {
            R07ClienteRecord r07;
            R10DestinazioneRecord r10;
            Integer idListinoAI26;
            Map<String, Integer> allListini;
            Exception error;

            @Override
            protected Void doInBackground() {
                try (Connection con = open()) {
                    OracleClientRepository repo = new OracleClientRepository(dbUrl, dbUser, dbPass);
                    OracleListinoRepository listRepo = new OracleListinoRepository();

                    r07 = repo.findClienteById(cd);

                    if (r07 != null && r07.keyC32 != null && !r07.keyC32.trim().isEmpty()) {
                        idListinoAI26 = listRepo.findListinoIdByGroupAndSeason(con, r07.keyC32, OracleOrderCreateService.CURRENT_STAGIONE);
                        allListini = listRepo.listAllListiniByGroup(con, r07.keyC32);
                    }

                    if (!dest.isEmpty()) {
                        String d = E01Builder.normalizeDestinazione4(dest);
                        r10 = repo.findDestinazione(cd, d);
                    }
                } catch (Exception ex) {
                    error = ex;
                }
                return null;
            }

            @Override
            protected void done() {
                btnLoad.setEnabled(true);

                if (error != null) {
                    out.setText(stackTraceToString(error));
                    return;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("INPUT\n");
                sb.append("  cdCliente: ").append(cd).append("\n");
                sb.append("  dest:     ").append(dest.isEmpty() ? "(none)" : dest).append("\n\n");

                sb.append("R07_CLIENTI\n");
                if (r07 == null) {
                    sb.append("  NOT FOUND\n");
                } else {
                    sb.append("  cdCliente:          ").append(n(r07.cdCliente)).append("\n");
                    sb.append("  ragioneSoc:         ").append(n(r07.ragioneSoc)).append("\n");
                    sb.append("  cdIva:              ").append(n(r07.cdIva)).append("\n");
                    sb.append("  cdAgente:           ").append(n(r07.cdAgente)).append("\n");
                    sb.append("  cdPagamento:        ").append(n(r07.cdPagamento)).append("\n");
                    sb.append("  cdValuta:           ").append(n(r07.cdValuta)).append("\n");
                    sb.append("  cdLingua:           ").append(n(r07.cdLingua)).append("\n");
                    sb.append("  cdZona:             ").append(n(r07.cdZona)).append("\n");
                    sb.append("  cdAbi:              ").append(n(r07.cdAbi)).append("\n");
                    sb.append("  cdCab:              ").append(n(r07.cdCab)).append("\n");
                    sb.append("  cdTrasporto:        ").append(n(r07.cdTrasporto)).append("\n");
                    sb.append("  cdVettore:          ").append(n(r07.cdVettore)).append("\n");
                    sb.append("  cdModConsegna:      ").append(n(r07.cdModConsegna)).append("\n");
                    sb.append("  cdImballo:          ").append(n(r07.cdImballo)).append("\n");
                    sb.append("  cdScontoMaggioraz:  ").append(n(r07.cdScontoMaggioraz)).append("\n");
                    sb.append("  keyC32 (grp list):  ").append(n(r07.keyC32)).append("\n");
                }

                sb.append("\nLISTINO (C30_TEST_LIST_CLI)\n");
                if (r07 == null || r07.keyC32 == null || r07.keyC32.trim().isEmpty()) {
                    sb.append("  group keyC32 not available\n");
                } else {
                    sb.append("  Group keyC32: ").append(n(r07.keyC32)).append("\n");
                    sb.append("  Season ").append(OracleOrderCreateService.CURRENT_STAGIONE).append(" => C30_ID_LISTINO: ")
                            .append(idListinoAI26 == null ? "(NOT FOUND)" : idListinoAI26).append("\n");
                    sb.append("  Fallback if NOT FOUND => ").append(OracleOrderCreateService.FALLBACK_ID_LISTINO).append("\n\n");
                    sb.append("  All active listini for group (C30_ST_RECORD='V'):\n");
                    if (allListini == null || allListini.isEmpty()) {
                        sb.append("    (none)\n");
                    } else {
                        for (Map.Entry<String, Integer> e : allListini.entrySet()) {
                            sb.append("    ").append(String.format("%-6s", e.getKey())).append(" -> ").append(e.getValue()).append("\n");
                        }
                    }
                }

                sb.append("\nR10_DESTINAZIONI_CLIENTE\n");
                if (dest.isEmpty()) {
                    sb.append("  (not requested)\n");
                } else if (r10 == null) {
                    sb.append("  NOT FOUND\n");
                } else {
                    sb.append("  cdCliente:       ").append(n(r10.cdCliente)).append("\n");
                    sb.append("  cdDestinazione:  ").append(n(r10.cdDestinazione)).append("\n");
                    sb.append("  ragioneSoc:      ").append(n(r10.ragioneSoc)).append("\n");
                    sb.append("  indirizzo:       ").append(n(r10.indirizzo)).append("\n");
                    sb.append("  cap:             ").append(n(r10.cap)).append("\n");
                    sb.append("  citta:           ").append(n(r10.citta)).append("\n");
                    sb.append("  stato:           ").append(n(r10.stato)).append("\n");
                    sb.append("  telefono:        ").append(n(r10.telefono)).append("\n");
                    sb.append("  email:           ").append(n(r10.email)).append("\n");
                    sb.append("  cdNazione:       ").append(n(r10.cdNazione)).append("\n");
                    sb.append("  cdAzienda:       ").append(n(r10.cdAzienda)).append("\n");
                }

                out.setText(sb.toString());
                out.setCaretPosition(0);
            }
        };

        w.execute();
    }

    private static String n(String s) {
        if (s == null) return "(null)";
        String t = s.trim();
        return t.isEmpty() ? "(empty)" : t;
    }

    private static String stackTraceToString(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}