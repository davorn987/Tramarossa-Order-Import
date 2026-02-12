package clientImport.ui;

import clientImport.*;

import clientImport.ui.dialogs.CustomerDebugDialog;
import clientImport.ui.dialogs.SettingsDialog;
import clientImport.ui.panels.GfImportPanel;
import clientImport.ui.panels.ManualOrdersPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GFEntriesViewerApp extends JFrame {

    private final Config cfg = Config.get();

    // Config values (kept here so SettingsDialog can edit them)
    private int formId;
    private int pageSize;
    private int offset;

    private String gfBaseUrl;
    private String gfKey;
    private String gfSecret;

    private String dbUrl;
    private String dbUser;
    private String dbPass;

    // Top user selector
    private JComboBox<G25OperatoreRecord> cmbOperatore;
    private JButton btnReloadUsers;

    // Panels
    private GfImportPanel gfImportPanel;
    private ManualOrdersPanel manualOrdersPanel;

    public GFEntriesViewerApp() {
        super("Tramarossa Orders Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 920);

        reloadFromConfig();
        initUi();

        refreshUsersAsync();
        manualOrdersPanel.refreshManualOrdersListsAsync();
    }

    private void reloadFromConfig() {
        gfBaseUrl = cfg.getGfApiBase();
        gfKey = cfg.getGfKey();
        gfSecret = cfg.getGfSecret();

        dbUrl = cfg.getDbUrl();
        dbUser = cfg.getDbUser();
        dbPass = cfg.getDbPassword();

        formId = cfg.getGfFormId();
        pageSize = cfg.getGfPageSize();
        offset = cfg.getGfOffset();
    }

    private void saveToConfig() throws IOException {
        cfg.setGfApiBase(gfBaseUrl);
        cfg.setGfKey(gfKey);
        cfg.setGfSecret(gfSecret);

        cfg.setDbUrl(dbUrl);
        cfg.setDbUser(dbUser);
        cfg.setDbPassword(dbPass);

        cfg.setGfFormId(formId);
        cfg.setGfPageSize(pageSize);
        cfg.setGfOffset(offset);

        cfg.save();
    }

    private void initUi() {
        setJMenuBar(buildMenuBar());

        JPanel globalTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        cmbOperatore = new JComboBox<>();
        cmbOperatore.setPreferredSize(new Dimension(320, 28));
        btnReloadUsers = new JButton("Reload Users");

        globalTop.add(new JLabel("User:"));
        globalTop.add(cmbOperatore);
        globalTop.add(btnReloadUsers);

        getContentPane().add(globalTop, BorderLayout.NORTH);

        gfImportPanel = new GfImportPanel(
                this,
                () -> gfBaseUrl, () -> gfKey, () -> gfSecret,
                () -> dbUrl, () -> dbUser, () -> dbPass,
                () -> formId, () -> pageSize, () -> offset
        );

        manualOrdersPanel = new ManualOrdersPanel(this, () -> dbUrl, () -> dbUser, () -> dbPass);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("GF Import", gfImportPanel);
        tabs.addTab("Manual Orders", manualOrdersPanel);

        getContentPane().add(tabs, BorderLayout.CENTER);

        btnReloadUsers.addActionListener(e -> refreshUsersAsync());
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();

        JMenu file = new JMenu("File");

        JMenuItem settings = new JMenuItem("Settings...");
        settings.addActionListener(e -> openSettingsDialog());

        JMenuItem reload = new JMenuItem("Reload settings");
        reload.addActionListener(e -> {
            reloadFromConfig();
            gfImportPanel.onSettingsReloaded();
            manualOrdersPanel.onSettingsReloaded();

            gfImportPanel.log("Settings reloaded from ~/.clientimport.properties");
            refreshUsersAsync();
            manualOrdersPanel.refreshManualOrdersListsAsync();
        });

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> dispose());

        file.add(settings);
        file.add(reload);
        file.addSeparator();
        file.add(exit);

        JMenu tools = new JMenu("Tools");
        JMenuItem custDbg = new JMenuItem("Customer Debug...");
        custDbg.addActionListener(e -> new CustomerDebugDialog(this, dbUrl, dbUser, dbPass).setVisible(true));
        tools.add(custDbg);

        mb.add(file);
        mb.add(tools);
        return mb;
    }

    private void openSettingsDialog() {
        SettingsDialog dlg = new SettingsDialog(
                this,
                gfBaseUrl, gfKey, gfSecret,
                dbUrl, dbUser, dbPass,
                formId, pageSize, offset
        );
        dlg.setVisible(true);

        if (!dlg.saved) return;

        gfBaseUrl = dlg.gfBaseUrl;
        gfKey = dlg.gfKey;
        gfSecret = dlg.gfSecret;

        dbUrl = dlg.dbUrl;
        dbUser = dlg.dbUser;
        dbPass = dlg.dbPass;

        formId = dlg.formId;
        pageSize = dlg.pageSize;
        offset = dlg.offset;

        try {
            saveToConfig();
            gfImportPanel.log("Settings saved to ~/.clientimport.properties");
            refreshUsersAsync();
            manualOrdersPanel.refreshManualOrdersListsAsync();
        } catch (Exception ex) {
            gfImportPanel.showFullException("Saving settings failed", ex);
        }
    }

    private void refreshUsersAsync() {
        btnReloadUsers.setEnabled(false);
        cmbOperatore.setEnabled(false);

        SwingWorker<java.util.List<G25OperatoreRecord>, Void> w = new SwingWorker<java.util.List<G25OperatoreRecord>, Void>() {
            @Override
            protected java.util.List<G25OperatoreRecord> doInBackground() throws Exception {
                try { Class.forName("oracle.jdbc.driver.OracleDriver"); } catch (ClassNotFoundException ignored) {}
                try (java.sql.Connection con = java.sql.DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                    return new G25OperatoriRepository().listActiveOperatori(con);
                }
            }

            @Override
            protected void done() {
                btnReloadUsers.setEnabled(true);
                cmbOperatore.setEnabled(true);
                try {
                    java.util.List<G25OperatoreRecord> list = get();
                    DefaultComboBoxModel<G25OperatoreRecord> m = new DefaultComboBoxModel<>();
                    for (G25OperatoreRecord r : list) m.addElement(r);
                    cmbOperatore.setModel(m);

                    String savedCd = cfg.getE01OperatoreCd();
                    String savedDs = cfg.getE01OperatoreDs();
                    selectUserInCombo(savedCd, savedDs);

                    gfImportPanel.log("Loaded users: " + list.size());
                } catch (Exception ex) {
                    gfImportPanel.log("ERROR loading users: " + ex.getMessage());
                    gfImportPanel.showFullException("Load users failed", ex);
                }
            }
        };

        w.execute();
    }

    private void selectUserInCombo(String cd, String ds) {
        ComboBoxModel<G25OperatoreRecord> m = cmbOperatore.getModel();
        if (m == null) return;

        int size = m.getSize();
        if (cd != null && !cd.trim().isEmpty()) {
            for (int i = 0; i < size; i++) {
                G25OperatoreRecord r = m.getElementAt(i);
                if (r != null && cd.equalsIgnoreCase(safe(r.cdOperatore))) {
                    cmbOperatore.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (ds != null && !ds.trim().isEmpty()) {
            for (int i = 0; i < size; i++) {
                G25OperatoreRecord r = m.getElementAt(i);
                if (r != null && ds.equalsIgnoreCase(safe(r.dsOperatore))) {
                    cmbOperatore.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (size > 0) cmbOperatore.setSelectedIndex(0);
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GFEntriesViewerApp app = new GFEntriesViewerApp();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}
