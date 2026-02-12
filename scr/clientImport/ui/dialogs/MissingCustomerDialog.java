package clientImport.ui.dialogs;

import clientImport.*;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * Dialog to select customer (mandatory) and destination (optional).
 *
 * Can be used in 2 modes:
 * - Customer missing: customer selection is mandatory (normal mode)
 * - Destination missing: customer is preselected + locked, destination optional
 *
 * Labels show names, value passed is numeric code.
 */
public class MissingCustomerDialog extends JDialog {

    public boolean confirmed = false;

    /** selected customer code (mandatory when confirmed=true) */
    public String selectedCdCliente;

    /** selected destination code (optional, can be null) */
    public String selectedCdDestinazione;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    private final String preselectedClienteCode;
    private final boolean lockClienteSelection;

    private JComboBox<UiOption> cmbCliente;
    private JComboBox<UiOption> cmbDest;
    private JButton btnOk;

    private JTextArea infoArea;

    public MissingCustomerDialog(JFrame owner,
                                 String dbUrl, String dbUser, String dbPass,
                                 String gfEntryId, String gfCompanyName,
                                 String preselectedClienteCode,
                                 boolean lockClienteSelection,
                                 String message) {
        super(owner, "Seleziona Cliente / Destinazione", true);
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;

        this.preselectedClienteCode = trimToNull(preselectedClienteCode);
        this.lockClienteSelection = lockClienteSelection;

        setSize(760, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        infoArea = new JTextArea(4, 80);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setText(
                "Entry GF #" + safe(gfEntryId) + "\n" +
                "Azienda: " + safe(gfCompanyName) + "\n\n" +
                (message == null ? "" : message)
        );
        top.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        cmbCliente = new JComboBox<>();
        cmbCliente.setPreferredSize(new Dimension(520, 28));
        cmbDest = new JComboBox<>();
        cmbDest.setPreferredSize(new Dimension(520, 28));

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        center.add(new JLabel("Cliente"), c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1.0;
        center.add(cmbCliente, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        center.add(new JLabel("Destinazione (opzionale)"), c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1.0;
        center.add(cmbDest, c);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
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
            btnOk.setEnabled(sel != null && sel.code != null && !sel.code.trim().isEmpty());
            loadDestinationsAsync(sel == null ? null : sel.code);
        });

        loadCustomersAsync();
    }

    private void onOk() {
        UiOption c = (UiOption) cmbCliente.getSelectedItem();
        if (c == null || c.code == null || c.code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleziona un cliente.", "Cliente richiesto", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UiOption d = (UiOption) cmbDest.getSelectedItem();
        String destCode = (d == null) ? null : trimToNull(d.code);

        this.selectedCdCliente = c.code.trim();
        this.selectedCdDestinazione = destCode; // can be null
        this.confirmed = true;
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
                    if (list != null) {
                        for (UiOption o : list) m.addElement(o);
                    }
                    cmbCliente.setModel(m);
                    cmbCliente.setEnabled(!lockClienteSelection);

                    // preselect customer if requested
                    if (preselectedClienteCode != null) {
                        selectClienteInCombo(preselectedClienteCode);
                    } else if (cmbCliente.getItemCount() > 0) {
                        cmbCliente.setSelectedIndex(0);
                    }

                    UiOption sel = (UiOption) cmbCliente.getSelectedItem();
                    btnOk.setEnabled(sel != null && sel.code != null && !sel.code.trim().isEmpty());

                    loadDestinationsAsync(sel == null ? null : sel.code);

                } catch (Exception ex) {
                    showError("Load customers failed", ex);
                }
            }
        };

        w.execute();
    }

    private void selectClienteInCombo(String cdCliente) {
        if (cdCliente == null) return;
        ComboBoxModel<UiOption> m = cmbCliente.getModel();
        if (m == null) return;

        for (int i = 0; i < m.getSize(); i++) {
            UiOption o = m.getElementAt(i);
            if (o != null && cdCliente.equalsIgnoreCase(trimToNull(o.code))) {
                cmbCliente.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadDestinationsAsync(String cdCliente) {
        cmbDest.setEnabled(false);

        SwingWorker<List<UiOption>, Void> w = new SwingWorker<List<UiOption>, Void>() {
            @Override
            protected List<UiOption> doInBackground() throws Exception {
                if (cdCliente == null || cdCliente.trim().isEmpty()) return java.util.Collections.emptyList();
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

                    if (list != null) {
                        for (UiOption o : list) m.addElement(o);
                    }

                    cmbDest.setModel(m);
                    cmbDest.setSelectedIndex(0);
                    cmbDest.setEnabled(true);

                } catch (Exception ex) {
                    showError("Load destinations failed", ex);
                }
            }
        };

        w.execute();
    }

    private void showError(String title, Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        JTextArea ta = new JTextArea(sw.toString(), 22, 100);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), title, JOptionPane.ERROR_MESSAGE);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.trim();
    }
}