package clientImport;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ClientImportApp (Java 8) updated to:
 * - prefer assigning 8xxxx codes to Woo customers (first free in 80000..89999)
 * - fallback to global max+1 only if the 8xxxx range is exhausted
 *
 * Requirements: same as before
 */
public class ClientImportApp extends JFrame {
    private JTextField txtDbUrl;
    private JTextField txtDbUser;
    private JPasswordField txtDbPassword;

    private JTextField txtWooApiBase; // e.g. https://example.com/wp-json/wc/v3
    private JTextField txtWooKey;
    private JTextField txtWooSecret;
    private JButton btnLoadWoo;

    private JTextField txtCsvFile;
    private JButton btnBrowse;
    private JButton btnLoadCsv;

    private JButton btnImportAll;
    private JButton btnImportSelected;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private File currentCsvFile;
    private List<Cliente_R07> clients = new ArrayList<>();
    private FileWriter logFileWriter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Config cfg = Config.get();

    public ClientImportApp() {
        super("Client Importer (WooCommerce - Java8)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 720);
        initComponents();
        loadConfigToUi();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        txtDbUrl = new JTextField(36);
        txtDbUser = new JTextField(12);
        txtDbPassword = new JPasswordField(12);

        txtWooApiBase = new JTextField(36);
        txtWooKey = new JTextField(18);
        txtWooSecret = new JTextField(18);
        btnLoadWoo = new JButton("Load from WooCommerce (orders:processing)");

        txtCsvFile = new JTextField(28);
        btnBrowse = new JButton("Browse...");
        btnLoadCsv = new JButton("Load CSV");

        btnImportAll = new JButton("Import All");
        btnImportSelected = new JButton("Import Selected");

        c.insets = new Insets(2,2,2,2);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        topPanel.add(new JLabel("DB URL:"), c);
        c.gridx = 1; c.gridwidth = 3;
        topPanel.add(txtDbUrl, c);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 1;
        topPanel.add(new JLabel("User:"), c);
        c.gridx = 1;
        topPanel.add(txtDbUser, c);
        c.gridx = 2;
        topPanel.add(new JLabel("Password:"), c);
        c.gridx = 3;
        topPanel.add(txtDbPassword, c);

        // WooCommerce row
        c.gridx = 0; c.gridy = 2; c.gridwidth = 1;
        topPanel.add(new JLabel("Woo API Base:"), c);
        c.gridx = 1; c.gridwidth = 3;
        topPanel.add(txtWooApiBase, c);

        c.gridx = 0; c.gridy = 3; c.gridwidth = 1;
        topPanel.add(new JLabel("Consumer Key:"), c);
        c.gridx = 1;
        topPanel.add(txtWooKey, c);
        c.gridx = 2;
        topPanel.add(new JLabel("Consumer Secret:"), c);
        c.gridx = 3;
        topPanel.add(txtWooSecret, c);

        c.gridx = 3; c.gridy = 4; c.gridwidth = 1;
        topPanel.add(btnLoadWoo, c);

        // CSV row (optional)
        c.gridx = 0; c.gridy = 5; c.gridwidth = 1;
        topPanel.add(new JLabel("CSV File (optional):"), c);
        c.gridx = 1; c.gridwidth = 2;
        topPanel.add(txtCsvFile, c);
        c.gridx = 3; c.gridwidth = 1;
        JPanel csvButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        csvButtons.add(btnBrowse);
        csvButtons.add(btnLoadCsv);
        topPanel.add(csvButtons, c);

        c.gridx = 0; c.gridy = 6; c.gridwidth = 1;
        topPanel.add(btnImportAll, c);
        c.gridx = 1;
        topPanel.add(btnImportSelected, c);

        getContentPane().add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[] {
                "Codice", "Ragione Sociale", "Nome FAT", "Cognome FAT", "UserID", "Email", "Tel", "Citta FAT", "CAP FAT", "Provincia"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);
        getContentPane().add(tableScroll, BorderLayout.CENTER);

        logArea = new JTextArea(10, 80);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        getContentPane().add(logScroll, BorderLayout.SOUTH);

        btnBrowse.addActionListener(e -> onBrowse());
        btnLoadCsv.addActionListener(e -> onLoadCsv());
        btnLoadWoo.addActionListener(e -> onLoadWoo());
        btnImportAll.addActionListener(e -> onImport(true));
        btnImportSelected.addActionListener(e -> onImport(false));

        // open log file in user's home directory
        try {
            File logFile = new File(System.getProperty("user.home"), "client_import_log.txt");
            logFileWriter = new FileWriter(logFile, true);
            log("Log file: " + logFile.getAbsolutePath());
        } catch (IOException ex) {
            log("Unable to open log file: " + ex.getMessage());
        }
    }

    private void loadConfigToUi() {
        txtWooApiBase.setText(cfg.getWooApiBase());
        txtWooKey.setText(cfg.getWooKey());
        txtWooSecret.setText(cfg.getWooSecret());
        txtDbUrl.setText(cfg.getDbUrl());
        txtDbUser.setText(cfg.getDbUser());
        txtDbPassword.setText(cfg.getDbPassword());
    }

    private void saveUiToConfig() {
        cfg.setWooApiBase(txtWooApiBase.getText().trim());
        cfg.setWooKey(txtWooKey.getText().trim());
        cfg.setWooSecret(txtWooSecret.getText().trim());
        cfg.setDbUrl(txtDbUrl.getText().trim());
        cfg.setDbUser(txtDbUser.getText().trim());
        cfg.setDbPassword(new String(txtDbPassword.getPassword()));
        try {
            cfg.save();
            log("Configuration saved to " + System.getProperty("user.home") + "/.clientimport.properties");
        } catch (IOException e) {
            log("Unable to save configuration: " + e.getMessage());
        }
    }

    private void onBrowse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv", "txt"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            currentCsvFile = chooser.getSelectedFile();
            txtCsvFile.setText(currentCsvFile.getAbsolutePath());
        }
    }

    private void onLoadCsv() {
        String path = txtCsvFile.getText().trim();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a CSV file first", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        currentCsvFile = new File(path);
        if (!currentCsvFile.exists()) {
            JOptionPane.showMessageDialog(this, "CSV file not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<String[]> rows = CSVUtils.readAll(currentCsvFile, "UTF-8");
            if (rows.isEmpty()) {
                log("CSV empty");
                return;
            }
            int startIndex = rows.size() > 0 ? 1 : 0; // skip header
            clients.clear();
            tableModel.setRowCount(0);
            for (int i = startIndex; i < rows.size(); i++) {
                String[] cols = rows.get(i);
                String[] safe = Arrays.copyOf(cols, Math.max(cols.length, 19));
                for (int k = 0; k < safe.length; k++) if (safe[k] == null) safe[k] = "";
                Cliente_R07 c = new Cliente_R07(
                        String.valueOf(0),
                        safe[0], safe[1], safe[2], safe[3], safe[4],
                        safe[5], safe[6], safe[7], safe[8], safe[9], safe[10],
                        safe[11], safe[12], safe[13], safe[14], safe[15], safe[16], safe[18], safe[17]
                );
                clients.add(c);
                tableModel.addRow(new Object[] {
                        c.getCodiceGriffe(), c.getRagioneSociale(), c.getNomeFAT(), c.getCognomeFAT(), c.getUserID(), c.getEmail(), c.getTelFAT(), c.getCittaFAT(), c.getCapFAT(), c.getProvinciaForDB()
                });
            }
            log("Loaded " + clients.size() + " clients from CSV.");
        } catch (Exception ex) {
            log("Error reading CSV: " + ex.getMessage());
            showFullException("Error reading CSV", ex);
        }
    }

    private void onLoadWoo() {
        String base = txtWooApiBase.getText().trim();
        String key = txtWooKey.getText().trim();
        String secret = txtWooSecret.getText().trim();
        if (base.isEmpty() || key.isEmpty() || secret.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide WooCommerce API base, consumer key and secret", "Missing data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // save config so values persist
        saveUiToConfig();

        LoadWooWorker worker = new LoadWooWorker(base, key, secret);
        worker.execute();
    }

    private void onImport(boolean all) {
        int[] selected = table.getSelectedRows();
        if (!all && (selected == null || selected.length == 0)) {
            JOptionPane.showMessageDialog(this, "Select rows to import or press Import All", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String url = txtDbUrl.getText().trim();
        String user = txtDbUser.getText().trim();
        String pass = new String(txtDbPassword.getPassword());

        List<Cliente_R07> toImport = new ArrayList<>();
        if (all) toImport.addAll(clients);
        else {
            for (int r : selected) {
                if (r >= 0 && r < clients.size()) toImport.add(clients.get(r));
            }
        }

        ImportWorker worker = new ImportWorker(url, user, pass, toImport);
        worker.execute();
    }

    private void log(String msg) {
        String full = "[" + new Date() + "] " + msg + "\n";
        SwingUtilities.invokeLater(() -> logArea.append(full));
        if (logFileWriter != null) {
            try {
                logFileWriter.write(full);
                logFileWriter.flush();
            } catch (IOException e) {
                // ignore
            }
        }
        System.out.print(full);
    }

    private void showFullException(String title, Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        JTextArea ta = new JTextArea(sw.toString(), 30, 80);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), title, JOptionPane.ERROR_MESSAGE);
    }

    // helper to count placeholders in SQL
    private static int countPlaceholders(String sql) {
        if (sql == null) return 0;
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') count++;
        }
        return count;
    }

    // SwingWorker to import to DB with update/create logic as requested
    private class ImportWorker extends SwingWorker<Integer, String> {
        private final String url;
        private final String user;
        private final String pass;
        private final List<Cliente_R07> toImport;

        ImportWorker(String url, String user, String pass, List<Cliente_R07> toImport) {
            this.url = url;
            this.user = user;
            this.pass = pass;
            this.toImport = toImport;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            int processed = 0;
            publish("Starting import of " + toImport.size() + " clients...");

            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                publish("Oracle driver class loaded");
            } catch (ClassNotFoundException e) {
                publish("Oracle driver class not found via Class.forName; make sure ojdbc jar is on classpath.");
            }

            try (Connection con = DriverManager.getConnection(url, user, pass)) {
                con.setAutoCommit(false);

                // compute global next code (max+1) for non-8xxxx fallback
                int maxCode = 0;
                try (Statement s = con.createStatement();
                     ResultSet rs = s.executeQuery("SELECT MAX(R07_CD_CLIENTE) FROM MADEVENETO.R07_CLIENTI")) {
                    if (rs.next()) {
                        String v = rs.getString(1);
                        if (v != null && !v.trim().isEmpty()) {
                            try { maxCode = Integer.parseInt(v.trim()); } catch (NumberFormatException nfe) { maxCode = 0; }
                        }
                    }
                }
                int nextCodeGlobal = maxCode + 1;
                publish("Next global code start: " + nextCodeGlobal);

                // Prepared statements
                String checkByUserSql = "SELECT R07_CD_CLIENTE FROM MADEVENETO.R07_CLIENTI WHERE R07_COD_INTERNO_CLIENTE = ?";
                String checkByNameSql = "SELECT R07_CD_CLIENTE FROM MADEVENETO.R07_CLIENTI WHERE R07_NOME = ? AND R07_COGNOME = ?";

                // NOTE: this INSERT must have exactly 39 placeholders (one for each parameter set below)
                String insertClientSql =
                        "INSERT INTO MADEVENETO.R07_CLIENTI (" +
                                "R07_ST_RECORD,R07_ST_MODIFICA,R07_CD_CLIENTE," +
                                "R07_RAGIONE_SOC,R07_INDIRIZZO,R07_CAP,R07_CITTA,R07_PROVINCIA,R07_STATO," +
                                "R07_PARTITA_IVA,R07_TELEFONO,R07_TELEX,R07_CD_PAGAMENTO,R07_CD_NAZIONE," +
                                "R07_CD_VALUTA,R07_CD_IVA,R07_FLAG_FITTIZIO,R07_CONTO_CONTABILE,R07_CD_OPERATORE," +
                                "R07_FLAG_FATT_IMMED,R07_FLAG_TIMBRATORE_LOGO,R07_CD_AZIENDA,R07_FLAG_CREA_FILE," +
                                "R07_FLINVIOOPIVA,R07_FLAG_DISMESSA,R07_FE_CODDEST,R07_NOME,R07_COGNOME," +
                                "R07_ADDEBITO_BOLLO,R07_PUBBLICA_AMM,R07_COD_INTERNO_CLIENTE,R07_PROVVIGIONE_AG," +
                                "R07_PROVVIGIONE_AG2,R07_PROVVIGIONE_AG3,R07_PROVVIGIONE_AG4," +
                                "R07_FLAG_BLOCCO_AMMINISTRATIVO,R07_FLAG_NO_SP_TRASPORTO,R07_KEY_C32,R07_EMAIL_ADDDRES" +
                                ") VALUES (" +
                                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                // the above line contains 39 '?' placeholders

                String updateClientSql =
                        "UPDATE MADEVENETO.R07_CLIENTI SET " +
                                "R07_RAGIONE_SOC=?, R07_INDIRIZZO=?, R07_CAP=?, R07_CITTA=?, R07_PROVINCIA=?, R07_STATO=?, " +
                                "R07_PARTITA_IVA=?, R07_TELEFONO=?, R07_TELEX=?, R07_CD_PAGAMENTO=?, R07_CD_NAZIONE=?, R07_CD_IVA=?, " +
                                "R07_NOME=?, R07_COGNOME=?, R07_COD_INTERNO_CLIENTE=?, R07_EMAIL_ADDDRES=? " +
                                "WHERE R07_CD_CLIENTE=?";

                String checkDestSql = "SELECT 1 FROM MADEVENETO.R10_DESTINAZIONI_CLIENTE WHERE R10_CD_CLIENTE = ? AND R10_CD_DESTINAZIONE='0001'";
                String insertDestSql =
                        "INSERT INTO MADEVENETO.R10_DESTINAZIONI_CLIENTE (" +
                                "R10_CD_CLIENTE,R10_CD_DESTINAZIONE,R10_ST_RECORD,R10_ST_MODIFICA,R10_RAGIONE_SOC," +
                                "R10_INDIRIZZO,R10_CAP,R10_CITTA,R10_STATO,R10_TELEFONO,R10_TELEX,R10_EMAIL_ADDDRES,R10_CD_NAZIONE,R10_CD_AZIENDA" +
                                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                String updateDestSql =
                        "UPDATE MADEVENETO.R10_DESTINAZIONI_CLIENTE SET R10_RAGIONE_SOC=?, R10_INDIRIZZO=?, R10_CAP=?, R10_CITTA=?, R10_STATO=?, R10_TELEFONO=?, R10_TELEX=?, R10_EMAIL_ADDDRES=?, R10_CD_NAZIONE=? WHERE R10_CD_CLIENTE=? AND R10_CD_DESTINAZIONE='0001'";

                try (PreparedStatement psCheckUser = con.prepareStatement(checkByUserSql);
                     PreparedStatement psCheckName = con.prepareStatement(checkByNameSql);
                     PreparedStatement psInsertClient = con.prepareStatement(insertClientSql);
                     PreparedStatement psUpdateClient = con.prepareStatement(updateClientSql);
                     PreparedStatement psCheckDest = con.prepareStatement(checkDestSql);
                     PreparedStatement psInsertDest = con.prepareStatement(insertDestSql);
                     PreparedStatement psUpdateDest = con.prepareStatement(updateDestSql)) {

                    // verify placeholder count for insertClientSql matches expected parameter count (39)
                    int placeholders = countPlaceholders(insertClientSql);
                    int expectedParams = 39; // we set 39 parameters below for each insert
                    if (placeholders != expectedParams) {
                        throw new SQLException("INSERT R07_CLIENTI placeholder count mismatch: found " + placeholders + " '?' but code expects " + expectedParams + " parameters. Please align SQL and parameters.");
                    }

                    for (Cliente_R07 c : toImport) {
                        if (isCancelled()) break;
                        publish("Processing: " + c.getRagioneSociale() + " / " + c.getUserID());

                        String existingCode = null;
                        boolean isGuest = "Ospite".equalsIgnoreCase(c.getUserID());

                        if (!isGuest && c.getUserID() != null && !c.getUserID().isEmpty()) {
                            // check by user id
                            psCheckUser.setString(1, c.getUserID());
                            try (ResultSet rs = psCheckUser.executeQuery()) {
                                if (rs.next()) existingCode = rs.getString(1);
                            }
                        } else {
                            // guest: check by nome & cognome
                            psCheckName.setString(1, c.getNomeFAT());
                            psCheckName.setString(2, c.getCognomeFAT());
                            try (ResultSet rs = psCheckName.executeQuery()) {
                                if (rs.next()) existingCode = rs.getString(1);
                            }
                        }

                        if (existingCode != null) {
                            // Update existing client
                            int idx = 1;
                            psUpdateClient.setString(idx++, c.getRagioneSociale()); // R07_RAGIONE_SOC
                            psUpdateClient.setString(idx++, c.getIndirizzoFAT());  // R07_INDIRIZZO
                            psUpdateClient.setString(idx++, c.getCapFAT());       // R07_CAP
                            psUpdateClient.setString(idx++, c.getCittaFAT());     // R07_CITTA
                            psUpdateClient.setString(idx++, c.getProvinciaForDB()); // R07_PROVINCIA
                            psUpdateClient.setString(idx++, c.getStatoFAT());     // R07_STATO

                            psUpdateClient.setString(idx++, c.getCodiceFiscale()); // R07_PARTITA_IVA
                            psUpdateClient.setString(idx++, c.getTelFAT());       // R07_TELEFONO
                            psUpdateClient.setString(idx++, c.getTelFAT());       // R07_TELEX
                            psUpdateClient.setString(idx++, c.getPagamento());    // R07_CD_PAGAMENTO
                            psUpdateClient.setString(idx++, c.getStatoFAT());     // R07_CD_NAZIONE
                            psUpdateClient.setString(idx++, c.getCodiceIVA());    // R07_CD_IVA

                            psUpdateClient.setString(idx++, c.getNomeFAT());     // R07_NOME
                            psUpdateClient.setString(idx++, c.getCognomeFAT());  // R07_COGNOME
                            psUpdateClient.setString(idx++, c.getUserID());      // R07_COD_INTERNO_CLIENTE
                            psUpdateClient.setString(idx++, c.getEmail());       // R07_EMAIL_ADDDRES

                            psUpdateClient.setString(idx++, existingCode);        // WHERE R07_CD_CLIENTE=?

                            int updated = psUpdateClient.executeUpdate();
                            publish("Updated " + updated + " rows in R07 for code " + existingCode);

                            // update/insert destination
                            psCheckDest.setString(1, existingCode);
                            try (ResultSet rs = psCheckDest.executeQuery()) {
                                if (rs.next()) {
                                    int dIdx = 1;
                                    psUpdateDest.setString(dIdx++, c.getNomeSPED() + " " + c.getCognomeSPED());
                                    psUpdateDest.setString(dIdx++, c.getIndirizzoSPED());
                                    psUpdateDest.setString(dIdx++, c.getCapSPED());
                                    psUpdateDest.setString(dIdx++, c.getCittaSPED());
                                    psUpdateDest.setString(dIdx++, c.getStatoSPED());
                                    psUpdateDest.setString(dIdx++, c.getTelFAT());
                                    psUpdateDest.setString(dIdx++, c.getTelFAT());
                                    psUpdateDest.setString(dIdx++, c.getEmail());
                                    psUpdateDest.setString(dIdx++, c.getStatoSPED());
                                    psUpdateDest.setString(dIdx++, existingCode);
                                    int ud = psUpdateDest.executeUpdate();
                                    publish("Updated " + ud + " rows in R10 for code " + existingCode);
                                } else {
                                    int dIdx = 1;
                                    psInsertDest.setString(dIdx++, existingCode);
                                    psInsertDest.setString(dIdx++, "0001");
                                    psInsertDest.setString(dIdx++, "V");
                                    psInsertDest.setInt(dIdx++, 1);
                                    psInsertDest.setString(dIdx++, c.getNomeSPED() + " " + c.getCognomeSPED());
                                    psInsertDest.setString(dIdx++, c.getIndirizzoSPED());
                                    psInsertDest.setString(dIdx++, c.getCapSPED());
                                    psInsertDest.setString(dIdx++, c.getCittaSPED());
                                    psInsertDest.setString(dIdx++, c.getStatoSPED());
                                    psInsertDest.setString(dIdx++, c.getTelFAT());
                                    psInsertDest.setString(dIdx++, c.getTelFAT());
                                    psInsertDest.setString(dIdx++, c.getEmail());
                                    psInsertDest.setString(dIdx++, c.getStatoSPED());
                                    psInsertDest.setString(dIdx++, "AZ1");
                                    int insd = psInsertDest.executeUpdate();
                                    publish("Inserted " + insd + " rows in R10 for code " + existingCode);
                                }
                            }

                            con.commit();
                            processed++;
                            continue;
                        }

                        // Not existing: choose code
                        String codeToUse = null;

                        // TRY FIRST: always attempt to allocate from 80000..89999
                        int candidate = findFirstAvailable8Code(con);
                        if (candidate > 0) {
                            codeToUse = String.valueOf(candidate);
                            publish("Allocated 8xxxx code " + codeToUse + " for new client");
                        } else {
                            // if 8xxxx range exhausted, fallback to non-8 global next
                            codeToUse = String.valueOf(nextCodeGlobal);
                            nextCodeGlobal++;
                            publish("8xxxx range exhausted, fallback to global code " + codeToUse);
                        }

                        // Insert client (same param order as earlier insert) - exactly 39 params
                        int idx = 1;
                        psInsertClient.setString(idx++, "V"); // R07_ST_RECORD
                        psInsertClient.setInt(idx++, 1);     // R07_ST_MODIFICA
                        psInsertClient.setString(idx++, codeToUse); // R07_CD_CLIENTE

                        psInsertClient.setString(idx++, c.getRagioneSociale()); // R07_RAGIONE_SOC
                        psInsertClient.setString(idx++, c.getIndirizzoFAT());  // R07_INDIRIZZO
                        psInsertClient.setString(idx++, c.getCapFAT());       // R07_CAP
                        psInsertClient.setString(idx++, c.getCittaFAT());     // R07_CITTA
                        psInsertClient.setString(idx++, c.getProvinciaForDB()); // R07_PROVINCIA
                        psInsertClient.setString(idx++, c.getStatoFAT());     // R07_STATO

                        psInsertClient.setString(idx++, c.getCodiceFiscale()); // R07_PARTITA_IVA
                        psInsertClient.setString(idx++, c.getTelFAT());       // R07_TELEFONO
                        psInsertClient.setString(idx++, c.getTelFAT());       // R07_TELEX
                        psInsertClient.setString(idx++, c.getPagamento());    // R07_CD_PAGAMENTO
                        psInsertClient.setString(idx++, c.getStatoFAT());     // R07_CD_NAZIONE
                        psInsertClient.setString(idx++, "EUR");               // R07_CD_VALUTA
                        psInsertClient.setString(idx++, c.getCodiceIVA());    // R07_CD_IVA

                        psInsertClient.setString(idx++, "N");                 // R07_FLAG_FITTIZIO
                        psInsertClient.setString(idx++, "00000000");          // R07_CONTO_CONTABILE
                        psInsertClient.setString(idx++, "000018");            // R07_CD_OPERATORE

                        psInsertClient.setString(idx++, "S");                 // R07_FLAG_FATT_IMMED
                        psInsertClient.setString(idx++, "N");                 // R07_FLAG_TIMBRATORE_LOGO
                        psInsertClient.setString(idx++, "AZ1");               // R07_CD_AZIENDA
                        psInsertClient.setString(idx++, "N");                 // R07_FLAG_CREA_FILE

                        psInsertClient.setString(idx++, "S");                 // R07_FLINVIOOPIVA
                        psInsertClient.setString(idx++, "N");                 // R07_FLAG_DISMESSA
                        psInsertClient.setString(idx++, "0000000");          // R07_FE_CODDEST

                        psInsertClient.setString(idx++, c.getNomeFAT());     // R07_NOME
                        psInsertClient.setString(idx++, c.getCognomeFAT());  // R07_COGNOME

                        psInsertClient.setString(idx++, "N");                 // R07_ADDEBITO_BOLLO
                        psInsertClient.setString(idx++, "N");                 // R07_PUBBLICA_AMM

                        psInsertClient.setString(idx++, c.getUserID());      // R07_COD_INTERNO_CLIENTE

                        psInsertClient.setInt(idx++, 0); // R07_PROVVIGIONE_AG
                        psInsertClient.setInt(idx++, 0); // R07_PROVVIGIONE_AG2
                        psInsertClient.setInt(idx++, 0); // R07_PROVVIGIONE_AG3
                        psInsertClient.setInt(idx++, 0); // R07_PROVVIGIONE_AG4

                        psInsertClient.setString(idx++, "N"); // R07_FLAG_BLOCCO_AMMINISTRATIVO
                        psInsertClient.setString(idx++, "N"); // R07_FLAG_NO_SP_TRASPORTO
                        psInsertClient.setInt(idx++, 1);      // R07_KEY_C32
                        psInsertClient.setString(idx++, c.getEmail()); // R07_EMAIL_ADDDRES

                        int rows = psInsertClient.executeUpdate();
                        if (rows < 1) {
                            throw new SQLException("Insert client affected 0 rows for " + c.getRagioneSociale());
                        }
                        publish("Inserted new client " + c.getRagioneSociale() + " with code " + codeToUse);

                        // insert destination
                        int dIdx = 1;
                        psInsertDest.setString(dIdx++, codeToUse);
                        psInsertDest.setString(dIdx++, "0001");
                        psInsertDest.setString(dIdx++, "V");
                        psInsertDest.setInt(dIdx++, 1);
                        psInsertDest.setString(dIdx++, c.getNomeSPED() + " " + c.getCognomeSPED());
                        psInsertDest.setString(dIdx++, c.getIndirizzoSPED());
                        psInsertDest.setString(dIdx++, c.getCapSPED());
                        psInsertDest.setString(dIdx++, c.getCittaSPED());
                        psInsertDest.setString(dIdx++, c.getStatoSPED());
                        psInsertDest.setString(dIdx++, c.getTelFAT());
                        psInsertDest.setString(dIdx++, c.getTelFAT());
                        psInsertDest.setString(dIdx++, c.getEmail());
                        psInsertDest.setString(dIdx++, c.getStatoSPED());
                        psInsertDest.setString(dIdx++, "AZ1");

                        int rows2 = psInsertDest.executeUpdate();
                        if (rows2 < 1) {
                            throw new SQLException("Insert destination affected 0 rows for " + c.getRagioneSociale());
                        }
                        publish("Inserted destination for client " + codeToUse);

                        con.commit();
                        processed++;
                    } // end for
                } // prepared statements
            } catch (SQLException ex) {
                publish("Database error: " + ex.getMessage());
                StringBuilder sb = new StringBuilder();
                sb.append("Registered JDBC drivers:\n");
                Enumeration<Driver> drivers = DriverManager.getDrivers();
                while (drivers.hasMoreElements()) {
                    Driver d = drivers.nextElement();
                    sb.append(" - ").append(d.getClass().getName()).append(" (").append(d).append(")\n");
                }
                publish(sb.toString());
                throw ex;
            }

            return processed;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String s : chunks) log(s);
        }

        @Override
        protected void done() {
            try {
                Integer count = get();
                log("Import finished, processed: " + count);
                JOptionPane.showMessageDialog(ClientImportApp.this, "Import finished, processed: " + count, "Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (InterruptedException | ExecutionException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                log("Import failed: " + cause.getMessage());
                StringWriter sw = new StringWriter();
                cause.printStackTrace(new PrintWriter(sw));
                JTextArea ta = new JTextArea(sw.toString(), 30, 80);
                ta.setEditable(false);
                JOptionPane.showMessageDialog(ClientImportApp.this, new JScrollPane(ta), "Import failed", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * Finds the first integer code in range [80000..89999] not present in R07_CD_CLIENTE.
         * Returns -1 if none found.
         *
         * This implementation is more robust: it filters non-numeric strings and trims values.
         */
        private int findFirstAvailable8Code(Connection con) throws SQLException {
            // Collect used 8xxxx numeric codes and compute max
            int max8 = -1;
            Set<Integer> used = new HashSet<>();
            try (PreparedStatement ps = con.prepareStatement("SELECT R07_CD_CLIENTE FROM MADEVENETO.R07_CLIENTI")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String s = rs.getString(1);
                        if (s == null) continue;
                        s = s.trim();
                        // only digits
                        boolean allDigits = true;
                        for (int i = 0; i < s.length(); i++) {
                            if (!Character.isDigit(s.charAt(i))) { allDigits = false; break; }
                        }
                        if (!allDigits) continue;
                        try {
                            int v = Integer.parseInt(s);
                            if (v >= 800000 && v <= 899999) {
                                used.add(v);
                                if (v > max8) max8 = v;
                            }
                        } catch (NumberFormatException ignored) { }
                    }
                }
            }

            // If we have a max in the 8xxxx range, try max+1 first (keeps sequence contiguous at the top)
            if (max8 >= 800000 && max8 < 899999) {
                int candidate = max8 + 1;
                if (!used.contains(candidate) && candidate <= 89999) {
                    return candidate;
                }
                // if candidate already used (very unlikely) fall through to gap search
            }

            // If no max found (no 8xxxx present) try to allocate starting from the "top area":
            // If we didn't find a max (max8 == -1) we'll prefer to start at 80000 + some offset,
            // but to keep behaviour predictable prefer to find the first free slot at/above 80000.
            for (int cand = 800000; cand <= 899999; cand++) {
                if (!used.contains(cand)) return cand;
            }

            // none available
            return -1;
        }
    } // end ImportWorker

    // Worker to load customers derived from orders with status=processing (paginated) using HttpUtils (Java 8 compatible)
    private class LoadWooWorker extends SwingWorker<Integer, String> {
        private final String apiBase;
        private final String key;
        private final String secret;

        LoadWooWorker(String apiBase, String key, String secret) {
            this.apiBase = apiBase;
            this.key = key;
            this.secret = secret;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            clients.clear();
            tableModel.setRowCount(0);
            publish("Loading customers from WooCommerce (orders with status=processing) ...");

            int page = 1;
            int perPage = 100;
            int totalLoaded = 0;

            // use set to deduplicate customers (prefer customer_id when >0, otherwise email)
            Set<String> seen = new HashSet<>();

            while (true) {
                String url = String.format("%s/orders?status=processing&per_page=%d&page=%d&consumer_key=%s&consumer_secret=%s",
                        apiBase,
                        perPage,
                        page,
                        URLEncoder.encode(key, "UTF-8"),
                        URLEncoder.encode(secret, "UTF-8")
                );

                publish("Fetching orders page " + page + " ...");
                String body;
                try {
                    body = HttpUtils.httpGet(url);
                } catch (IOException ioe) {
                    publish("WooCommerce API error: " + ioe.getMessage());
                    throw ioe;
                }

                JsonNode root = objectMapper.readTree(body);
                if (!root.isArray() || root.size() == 0) {
                    publish("No more orders (page " + page + " empty).");
                    break;
                }

                final List<Cliente_R07> pageClients = new ArrayList<>();
                for (JsonNode order : root) {
                    int customerId = order.path("customer_id").asInt(0);
                    String email = order.path("billing").path("email").asText("").trim();
                    String keyId = (customerId > 0) ? ("id:" + customerId) : ("email:" + email.toLowerCase());

                    if (keyId == null || keyId.trim().isEmpty()) {
                        continue;
                    }
                    if (seen.contains(keyId)) {
                        continue;
                    }

                    Cliente_R07 c = mapWooOrderToCliente(order);
                    pageClients.add(c);
                    seen.add(keyId);
                    totalLoaded++;
                }

                // update table on EDT
                SwingUtilities.invokeAndWait(() -> {
                    for (Cliente_R07 c : pageClients) {
                        clients.add(c);
                        tableModel.addRow(new Object[] {
                                c.getCodiceGriffe(), c.getRagioneSociale(), c.getNomeFAT(), c.getCognomeFAT(), c.getUserID(), c.getEmail(), c.getTelFAT(), c.getCittaFAT(), c.getCapFAT(), c.getProvinciaForDB()
                        });
                    }
                });

                if (root.size() < perPage) break;
                page++;
            }

            publish("Loaded " + totalLoaded + " unique customers from processing orders.");
            return totalLoaded;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String s : chunks) log(s);
        }

        @Override
        protected void done() {
            try {
                Integer count = get();
                log("Woo load finished: " + count + " customers loaded.");
            } catch (InterruptedException | ExecutionException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                log("Woo load failed: " + cause.getMessage());
                StringWriter sw = new StringWriter();
                cause.printStackTrace(new PrintWriter(sw));
                JTextArea ta = new JTextArea(sw.toString(), 30, 80);
                ta.setEditable(false);
                JOptionPane.showMessageDialog(ClientImportApp.this, new JScrollPane(ta), "Load failed", JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * Map a WooCommerce order JSON to a Cliente_R07 using billing/shipping data.
         */
        private Cliente_R07 mapWooOrderToCliente(JsonNode order) {
            JsonNode billing = order.path("billing");
            JsonNode shipping = order.path("shipping");

            String billFirst = billing.path("first_name").asText("");
            String billLast = billing.path("last_name").asText("");
            String billCompany = billing.path("company").asText("");
            String billAddress1 = billing.path("address_1").asText("");
            String billCity = billing.path("city").asText("");
            String billPostcode = billing.path("postcode").asText("");
            String billCountry = billing.path("country").asText("");
            String billState = billing.path("state").asText("");
            String billPhone = billing.path("phone").asText("");
            String billingEmail = billing.path("email").asText("");

            String shipFirst = shipping.path("first_name").asText("");
            String shipLast = shipping.path("last_name").asText("");
            String shipAddress1 = shipping.path("address_1").asText("");
            String shipCity = shipping.path("city").asText("");
            String shipPostcode = shipping.path("postcode").asText("");
            String shipCountry = shipping.path("country").asText();
            String shipState = shipping.path("state").asText();

            String vat = "";
            if (billing.has("vat_number")) vat = billing.path("vat_number").asText("");
            else if (order.has("meta_data")) {
                for (JsonNode md : order.path("meta_data")) {
                    String k = md.path("key").asText("");
                    if ("vat_number".equalsIgnoreCase(k) || "vat".equalsIgnoreCase(k) || "partita_iva".equalsIgnoreCase(k)) {
                        vat = md.path("value").asText("");
                        break;
                    }
                }
            }

            String ragione = (billCompany != null && !billCompany.isEmpty()) ? billCompany : ((billFirst + " " + billLast).trim());
            String orderId = order.path("id").asText("");
            String userId = (billingEmail != null && !billingEmail.isEmpty()) ? billingEmail : order.path("customer_id").asText(orderId);

            Cliente_R07 c = new Cliente_R07(
                    orderId,
                    ragione,
                    billAddress1,
                    billPostcode,
                    billCity,
                    billPhone,
                    billCountry,
                    "", // pagamento not available here
                    billState,
                    billFirst,
                    billLast,
                    userId,
                    shipFirst,
                    shipLast,
                    shipAddress1,
                    shipCity,
                    shipPostcode,
                    shipCountry,
                    vat != null ? vat : "",
                    billingEmail
            );
            return c;
        }
    } // end LoadWooWorker

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientImportApp app = new ClientImportApp();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}