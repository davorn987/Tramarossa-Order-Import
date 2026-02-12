package clientImport.ui.panels;

import clientImport.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.function.Supplier;

public class ManualOrdersPanel extends JPanel {

    private final JFrame owner;
    private final Supplier<String> dbUrl;
    private final Supplier<String> dbUser;
    private final Supplier<String> dbPass;

    private JTable manualOrdersTable;
    private DefaultTableModel manualOrdersModel;

    private JComboBox<UiOption> moCmbCliente;
    private JComboBox<UiOption> moCmbAgente;
    private JComboBox<Integer> moCmbAnno;

    private JTextField moTxtDaNr;
    private JTextField moTxtANr;

    private JCheckBox moChkPrezzi0;
    private JCheckBox moChkEscludiClienteInterno;

    private JRadioButton moRbAnnTutte;
    private JRadioButton moRbAnnSenzaA;
    private JRadioButton moRbAnnSoloA;

    private JRadioButton moRbChiusoTutti;
    private JRadioButton moRbChiusoSi;
    private JRadioButton moRbChiusoNo;

    private JButton moBtnAggiorna;
    private JButton moBtnNuovo;
    private JButton moBtnModifica;
    private JButton moBtnElimina;
    private JButton moBtnReloadLists;

    public ManualOrdersPanel(JFrame owner, Supplier<String> dbUrl, Supplier<String> dbUser, Supplier<String> dbPass) {
        super(new BorderLayout());
        this.owner = owner;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;

        initUi();
    }

    public void onSettingsReloaded() {
        // nothing required; suppliers always read latest values
    }

    private void initUi() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(new TitledBorder("Filtro di Selezione"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        moCmbCliente = new JComboBox<>();
        moCmbCliente.setPreferredSize(new Dimension(420, 26));
        moCmbAgente = new JComboBox<>();
        moCmbAgente.setPreferredSize(new Dimension(320, 26));

        moCmbAnno = new JComboBox<>();
        for (int y = 2000; y <= 2050; y++) moCmbAnno.addItem(y);
        moCmbAnno.setSelectedItem(2026);

        moTxtDaNr = new JTextField("", 6);
        moTxtANr = new JTextField("", 6);

        moChkPrezzi0 = new JCheckBox("Mostra solo ordini con prezzi a 0");
        moChkEscludiClienteInterno = new JCheckBox("Escludi Cliente Interno");

        moRbAnnTutte = new JRadioButton("Tutte", true);
        moRbAnnSenzaA = new JRadioButton("Senza A");
        moRbAnnSoloA = new JRadioButton("Solo A");
        ButtonGroup grpAnn = new ButtonGroup();
        grpAnn.add(moRbAnnTutte);
        grpAnn.add(moRbAnnSenzaA);
        grpAnn.add(moRbAnnSoloA);

        moRbChiusoTutti = new JRadioButton("TUTTI", true);
        moRbChiusoSi = new JRadioButton("Si");
        moRbChiusoNo = new JRadioButton("No");
        ButtonGroup grpCh = new ButtonGroup();
        grpCh.add(moRbChiusoTutti);
        grpCh.add(moRbChiusoSi);
        grpCh.add(moRbChiusoNo);

        moBtnAggiorna = new JButton("Aggiorna");
        moBtnNuovo = new JButton("Nuovo");
        moBtnModifica = new JButton("Modifica/Visualizza");
        moBtnElimina = new JButton("Elimina");
        moBtnReloadLists = new JButton("Reload lists");

        int row = 0;

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        filterPanel.add(new JLabel("Cliente"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 0.8;
        filterPanel.add(moCmbCliente, c);

        c.gridx = 2; c.gridy = row; c.weightx = 0;
        filterPanel.add(new JLabel("Agente"), c);
        c.gridx = 3; c.gridy = row; c.weightx = 0.4;
        filterPanel.add(moCmbAgente, c);

        c.gridx = 4; c.gridy = row; c.weightx = 0;
        filterPanel.add(new JLabel("Anno"), c);
        c.gridx = 5; c.gridy = row; c.weightx = 0.2;
        filterPanel.add(moCmbAnno, c);

        row++;
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        filterPanel.add(moChkPrezzi0, c);
        c.gridx = 1; c.gridy = row; c.weightx = 0;
        filterPanel.add(moChkEscludiClienteInterno, c);

        c.gridx = 2; c.gridy = row; c.weightx = 0;
        filterPanel.add(new JLabel("Righe annullate"), c);
        JPanel annullatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        annullatePanel.add(moRbAnnTutte); annullatePanel.add(moRbAnnSenzaA); annullatePanel.add(moRbAnnSoloA);
        c.gridx = 3; c.gridy = row; c.weightx = 0.6; c.gridwidth = 2;
        filterPanel.add(annullatePanel, c);
        c.gridwidth = 1;

        c.gridx = 5; c.gridy = row; c.weightx = 0;
        JPanel nrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nrPanel.add(new JLabel("Da Nr")); nrPanel.add(moTxtDaNr);
        nrPanel.add(new JLabel("A Nr")); nrPanel.add(moTxtANr);
        filterPanel.add(nrPanel, c);

        row++;
        c.gridx = 2; c.gridy = row; c.weightx = 0;
        filterPanel.add(new JLabel("Chiuso"), c);
        JPanel chiusoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chiusoPanel.add(moRbChiusoTutti); chiusoPanel.add(moRbChiusoSi); chiusoPanel.add(moRbChiusoNo);
        c.gridx = 3; c.gridy = row; c.weightx = 0.6; c.gridwidth = 2;
        filterPanel.add(chiusoPanel, c);
        c.gridwidth = 1;

        c.gridx = 5; c.gridy = row; c.weightx = 0;
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.add(moBtnReloadLists);
        actionsPanel.add(moBtnAggiorna);
        actionsPanel.add(moBtnNuovo);
        actionsPanel.add(moBtnModifica);
        actionsPanel.add(moBtnElimina);
        filterPanel.add(actionsPanel, c);

        add(filterPanel, BorderLayout.NORTH);

        manualOrdersModel = new DefaultTableModel(new String[]{
                "Data Ordine", "Anno", "Nr", "Stagione", "Dest",
                "Cliente", "Utente", "Note",
                "Qta Ord.", "Qta Spe.", "Qta Ass.",
                "Imp. Ordine", "Imp. Netto",
                "Rag. Sociale", "Destinatario", "Agente",
                "Chiuso", "Pagamento", "Nazione", "Data Consegna",
                "Righe A", "Cliente Interno", "Cd Operatore", "Cd Agente"
        }, 0) {
            @Override public boolean isCellEditable(int r, int col) { return false; }
        };

        manualOrdersTable = new JTable(manualOrdersModel);
        manualOrdersTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        manualOrdersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(manualOrdersTable), BorderLayout.CENTER);

        moBtnReloadLists.addActionListener(e -> refreshManualOrdersListsAsync());
        moBtnAggiorna.addActionListener(e -> onManualOrdersRefresh());

        moBtnNuovo.addActionListener(e -> JOptionPane.showMessageDialog(owner, "TODO: Nuovo ordine (editor)", "Manual Orders", JOptionPane.INFORMATION_MESSAGE));
        moBtnModifica.addActionListener(e -> JOptionPane.showMessageDialog(owner, "TODO: Modifica/Visualizza ordine selezionato", "Manual Orders", JOptionPane.INFORMATION_MESSAGE));
        moBtnElimina.addActionListener(e -> JOptionPane.showMessageDialog(owner, "TODO: Elimina ordine selezionato", "Manual Orders", JOptionPane.INFORMATION_MESSAGE));
    }

    public void refreshManualOrdersListsAsync() {
        moBtnReloadLists.setEnabled(false);
        SwingWorker<Void, Void> w = new SwingWorker<Void, Void>() {
            List<UiOption> clienti;
            List<UiOption> agenti;

            @Override
            protected Void doInBackground() throws Exception {
                try { Class.forName("oracle.jdbc.driver.OracleDriver"); } catch (ClassNotFoundException ignored) {}
                try (Connection con = DriverManager.getConnection(dbUrl.get(), dbUser.get(), dbPass.get())) {
                    clienti = new R07ClientiRepository().listClientiAttivi(con);
                    agenti = new C07AgentiRepository().listAgentiAttivi(con);
                }
                return null;
            }

            @Override
            protected void done() {
                moBtnReloadLists.setEnabled(true);
                try {
                    get();

                    DefaultComboBoxModel<UiOption> cm = new DefaultComboBoxModel<>();
                    cm.addElement(new UiOption("", "(Tutti i clienti)"));
                    if (clienti != null) for (UiOption o : clienti) cm.addElement(o);
                    moCmbCliente.setModel(cm);

                    DefaultComboBoxModel<UiOption> am = new DefaultComboBoxModel<>();
                    am.addElement(new UiOption("", "(Tutti gli agenti)"));
                    if (agenti != null) for (UiOption o : agenti) am.addElement(o);
                    moCmbAgente.setModel(am);

                } catch (Exception ex) {
                    showFullException("Reload lists failed", ex);
                }
            }
        };
        w.execute();
    }

    private void onManualOrdersRefresh() {
        E01001LWRepository.Filters f = new E01001LWRepository.Filters();

        UiOption clienteOpt = (UiOption) moCmbCliente.getSelectedItem();
        UiOption agenteOpt = (UiOption) moCmbAgente.getSelectedItem();

        f.cdCliente = (clienteOpt == null) ? null : clienteOpt.code;
        f.cdAgente = (agenteOpt == null) ? null : agenteOpt.code;

        Integer anno = (Integer) moCmbAnno.getSelectedItem();
        f.anno = anno;

        f.daNr = parseIntOrDefault(moTxtDaNr.getText(), 0);
        Integer aNr = parseIntOrNull(moTxtANr.getText());
        f.aNr = aNr;

        if (moRbChiusoSi.isSelected()) f.chiuso = true;
        else if (moRbChiusoNo.isSelected()) f.chiuso = false;
        else f.chiuso = null;

        if (moRbAnnSenzaA.isSelected()) f.annullate = E01001LWRepository.Annullate.SENZA_A;
        else if (moRbAnnSoloA.isSelected()) f.annullate = E01001LWRepository.Annullate.SOLO_A;
        else f.annullate = E01001LWRepository.Annullate.TUTTE;

        f.soloPrezziZero = moChkPrezzi0.isSelected();
        f.escludiClienteInterno = moChkEscludiClienteInterno.isSelected();

        moBtnAggiorna.setEnabled(false);
        SwingWorker<List<E01_001LW_Row>, Void> w = new SwingWorker<List<E01_001LW_Row>, Void>() {
            @Override
            protected List<E01_001LW_Row> doInBackground() throws Exception {
                try { Class.forName("oracle.jdbc.driver.OracleDriver"); } catch (ClassNotFoundException ignored) {}
                try (Connection con = DriverManager.getConnection(dbUrl.get(), dbUser.get(), dbPass.get())) {
                    return new E01001LWRepository().listOrders(con, f);
                }
            }

            @Override
            protected void done() {
                moBtnAggiorna.setEnabled(true);
                try {
                    List<E01_001LW_Row> rows = get();
                    manualOrdersModel.setRowCount(0);

                    for (E01_001LW_Row r : rows) {
                        manualOrdersModel.addRow(new Object[]{
                                r.e01DataOrdine,
                                r.e01AnnoOrdine,
                                r.e01NrOrdine,
                                r.e01CdStagione,
                                r.e01CdDestinazione,

                                r.e01CdCliente,
                                r.dsUtenteBut,
                                r.e01Memo,

                                r.qtaTotOrdine,
                                r.qtaTotSpedita,
                                r.assegnatoOrdine,

                                r.valTotOrdine,
                                r.impNettoOrd,

                                r.r07RagioneSoc,
                                r.r10RagioneSoc,
                                r.c07RagSocAgente,

                                r.e01FlagChiuso,
                                r.r06DsPagamento,
                                r.r02DsNazione,
                                r.dataConsConf,

                                r.righeAnnullate,
                                r.r07ClienteInterno,
                                r.e01CdOperatore,
                                r.e01CdAgente
                        });
                    }

                } catch (Exception ex) {
                    showFullException("Manual Orders refresh failed", ex);
                }
            }
        };
        w.execute();
    }

    private Integer parseIntOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try { return Integer.parseInt(t); } catch (Exception e) { return null; }
    }

    private int parseIntOrDefault(String s, int def) {
        Integer v = parseIntOrNull(s);
        return v == null ? def : v;
    }

    private void showFullException(String title, Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        JTextArea ta = new JTextArea(sw.toString(), 30, 120);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(owner, new JScrollPane(ta), title, JOptionPane.ERROR_MESSAGE);
    }
}