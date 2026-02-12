package clientImport.ui.dialogs;

import clientImport.*;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * Always-shown pre-import dialog that lets the operator CONFIRM or CHANGE:
 * - Customer (mandatory)
 * - Destination (optional)
 *
 * Behaviour:
 * - If GF provides a customer code, we preselect it (when found in active list).
 * - If GF provides a destination code, we try to preselect it (when found for the chosen customer).
 * - If GF provides nothing, user must choose at least a customer before OK is enabled.
 * - At the top we show the "nominativo" coming from GF (companyName) very clearly.
 */
public class CustomerConfirmDialog extends JDialog {

    public boolean confirmed = false;

    public String selectedCdCliente;       // mandatory when confirmed=true
    public String selectedCdDestinazione;  // optional (can be null)

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    private final String gfCdCliente;
    private final String gfCdDest;
    private final String gfCompanyName;
    private final String gfEntryId;

    private JComboBox<UiOption> cmbCliente;
    private JComboBox<UiOption> cmbDest;
    private JButton btnOk;

    private JTextArea header;

    public CustomerConfirmDialog(JFrame owner,
                                 String dbUrl, String dbUser, String dbPass,
                                 String gfEntryId,
                                 String gfCompanyName,
                                 String gfCdCliente,
                                 String gfCdDest) {
        super(owner, "Conferma Cliente / Destinazione", true);

        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;

        this.gfEntryId = gfEntryId;
        this.gfCompanyName = gfCompanyName;
        this.gfCdCliente = gfCdCliente;
        this.gfCdDest = gfCdDest;

        setSize(820, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        header = new JTextArea(6, 100);
        header.setEditable(false);
        header.setLineWrap(true);
        header.setWrapStyleWord(true);
        header.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        header.setBackground(new Color(250, 250, 250));
        header.setText(buildHeaderText());
        add(new JScrollPane(header), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        cmbCliente = new JComboBox<>();
        cmbCliente.setPreferredSize(new Dimension(560, 28));
        cmbDest = new JComboBox<>();
        cmbDest.setPreferredSize(new Dimension(560, 28));

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        center.add(new JLabel("Cliente (obbligatorio)"), c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        center.add(cmbCliente, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        center.add(new JLabel("Destinazione (opzionale)"), c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        center.add(cmbDest, c);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnCancel = new JButton("Cancel");
        btnOk = new JButton("OK");
        btnOk.setEnabled(false);
        bottom.add(btnCancel);
        bottom.add(btnOk);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> onOk());

        cmbCliente.addActionListener(e -> {
            UiOption sel = (UiOption) cmbCliente.getSelectedItem();
            btnOk.setEnabled(sel != null && !isBlank(sel.code));
            loadDestinationsAsync(sel == null ? null : sel.code, /*tryPreselectGfDest*/ true);
        });

        loadCustomersAsync();
    }

    private String buildHeaderText() {
        StringBuilder sb = new StringBuilder();
        sb.append("ENTRY GF #").append(nullToEmpty(gfEntryId)).append("\n");
        sb.append("NOMINATIVO ARRIVO ORDINE:\n");
        sb.append(nullToEmpty(gfCompanyName)).append("\n\n");
        sb.append("Valori ricevuti da GF:\n");
        sb.append("  Cd Cliente: ").append(nullToEmpty(trimToEmpty(gfCdCliente))).append("\n");
        sb.append("  Dest:       ").append(nullToEmpty(trimToEmpty(gfCdDest))).append("\n");
        return sb.toString();
    }

    private void onOk() {
        UiOption c = (UiOption) cmbCliente.getSelectedItem();
        if (c == null || isBlank(c.code)) {
            JOptionPane.showMessageDialog(this, "Seleziona un cliente.", "Cliente richiesto", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UiOption d = (UiOption) cmbDest.getSelectedItem();
        String destCode = (d == null) ? null : trimToNull(d.code);

        selectedCdCliente = c.code.trim();
        selectedCdDestinazione = destCode; // can be null
        confirmed = true;
        dispose();
    }

    private void loadCustomersAsync() {
        cmbCliente.setEnabled(false);
        cmbDest.setEnabled(false);
        btnOk.setEnabled(false);

        SwingWorker<List<UiOption>, Void> w = new SwingWorker<List<UiOption>, Void>() {
            @Override
            protected List<UiOption> doInBackground() throws Exception {
                try { Class.forName("oracle.jdbc.driver.OracleDriver"); } catch (ClassNotFoundException ignored) {}
                try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                    return new R07ClientiRepository().listClientiAttivi(con);
                }
            }

            @Override
            protected void done() {
                try {
                    List<UiOption> list = get();
                    DefaultComboBoxModel<UiOption> m = new DefaultComboBoxModel<>();
                    if (list != null) for (UiOption o : list) m.addElement(o);

                    cmbCliente.setModel(m);
                    cmbCliente.setEnabled(true);

                    // preselect GF customer if provided
                    String gfClient = trimToNull(gfCdCliente);
                    if (gfClient != null) {
                        if (!selectByCode(cmbCliente, gfClient)) {
                            // if not found, leave first item (if any)
                            if (cmbCliente.getItemCount() > 0) cmbCliente.setSelectedIndex(0);
                        }
                    } else {
                        // GF empty -> user must pick; keep unselected if possible
                        if (cmbCliente.getItemCount() > 0) cmbCliente.setSelectedIndex(-1);
                    }

                    UiOption sel = (UiOption) cmbCliente.getSelectedItem();
                    btnOk.setEnabled(sel != null && !isBlank(sel.code));

                    // destinations depend on selected customer
                    loadDestinationsAsync(sel == null ? null : sel.code, /*tryPreselectGfDest*/ true);

                } catch (Exception ex) {
                    showError("Load customers failed", ex);
                }
            }
        };

        w.execute();
    }

    private void loadDestinationsAsync(String cdCliente, boolean tryPreselectGfDest) {
        cmbDest.setEnabled(false);

        SwingWorker<List<UiOption>, Void> w = new SwingWorker<List<UiOption>, Void>() {
            @Override
            protected List<UiOption> doInBackground() throws Exception {
                if (isBlank(cdCliente)) return java.util.Collections.emptyList();
                try { Class.forName("oracle.jdbc.driver.OracleDriver"); } catch (ClassNotFoundException ignored) {}
                try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                    return new R10DestinazioniRepository().listDestinazioniByCliente(con, cdCliente);
                }
            }

            @Override
            protected void done() {
                try {
                    List<UiOption> list = get();

                    DefaultComboBoxModel<UiOption> m = new DefaultComboBoxModel<>();
                    m.addElement(new UiOption("", "(nessuna destinazione)"));

                    if (list != null) for (UiOption o : list) m.addElement(o);

                    cmbDest.setModel(m);

                    // preselect GF destination if provided and present in list
                    String gfD = trimToNull(gfCdDest);
                    if (tryPreselectGfDest && gfD != null) {
                        boolean found = selectByCode(cmbDest, gfD);
                        if (!found) cmbDest.setSelectedIndex(0);
                    } else {
                        cmbDest.setSelectedIndex(0);
                    }

                    cmbDest.setEnabled(true);

                } catch (Exception ex) {
                    showError("Load destinations failed", ex);
                }
            }
        };

        w.execute();
    }

    private static boolean selectByCode(JComboBox<UiOption> combo, String code) {
        if (combo == null || code == null) return false;
        ComboBoxModel<UiOption> m = combo.getModel();
        if (m == null) return false;
        for (int i = 0; i < m.getSize(); i++) {
            UiOption o = m.getElementAt(i);
            if (o != null && code.equalsIgnoreCase(trimToNull(o.code))) {
                combo.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    private void showError(String title, Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        JTextArea ta = new JTextArea(sw.toString(), 22, 100);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), title, JOptionPane.ERROR_MESSAGE);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String trimToEmpty(String s) {
        String t = (s == null) ? "" : s.trim();
        return t;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
