package clientImport.ui.panels;

import clientImport.*;
import clientImport.ui.dialogs.CustomerConfirmDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class GfImportPanel extends JPanel {

    private final JFrame owner;

    private final Supplier<String> gfBaseUrl;
    private final Supplier<String> gfKey;
    private final Supplier<String> gfSecret;

    private final Supplier<String> dbUrl;
    private final Supplier<String> dbUser;
    private final Supplier<String> dbPass;

    private final IntSupplier formId;
    private final IntSupplier pageSize;
    private final IntSupplier offset;

    private JButton btnLoad;
    private JButton btnImportOrder;
    private JTable table;
    private DefaultTableModel model;
    private JTextArea logArea;

    private final java.util.List<GF224EntryPreview> previews = new java.util.ArrayList<>();

    public GfImportPanel(JFrame owner,
                         Supplier<String> gfBaseUrl, Supplier<String> gfKey, Supplier<String> gfSecret,
                         Supplier<String> dbUrl, Supplier<String> dbUser, Supplier<String> dbPass,
                         IntSupplier formId, IntSupplier pageSize, IntSupplier offset) {

        super(new BorderLayout());
        this.owner = owner;

        this.gfBaseUrl = gfBaseUrl;
        this.gfKey = gfKey;
        this.gfSecret = gfSecret;

        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;

        this.formId = formId;
        this.pageSize = pageSize;
        this.offset = offset;

        initUi();
    }

    public void onSettingsReloaded() { }

    private void initUi() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        btnLoad = new JButton("Load UNREAD Entries");
        btnImportOrder = new JButton("IMPORT ORDER");
        btnImportOrder.setEnabled(false);

        top.add(btnLoad);
        top.add(btnImportOrder);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{
                "Entry ID", "Date Created", "Cliente", "Dest", "Company", "VAT", "Items", "Total", "Delivery Date", "Listino"
        }, 0) {
            @Override public boolean isCellEditable(int r, int col) { return false; }
        };

        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean ok = table.getSelectedRow() >= 0 && table.getSelectedRow() < previews.size();
            btnImportOrder.setEnabled(ok);
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        logArea = new JTextArea(8, 120);
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        btnLoad.addActionListener(e -> onLoad());
        btnImportOrder.addActionListener(e -> onImportOrder());
    }

    public void onLoad() {
        model.setRowCount(0);
        previews.clear();
        btnImportOrder.setEnabled(false);

        final String base = gfBaseUrl.get();
        final String key = gfKey.get();
        final String secret = gfSecret.get();

        final int localFormId = formId.getAsInt();
        final int localPageSize = pageSize.getAsInt();
        final int localOffset = offset.getAsInt();

        log("[" + new Date() + "] Loading UNREAD GF entries... formId=" + localFormId + " pageSize=" + localPageSize + " offset=" + localOffset);

        SwingWorker<Void, String> w = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                GFClient client = new GFClient(base, key, secret);
                com.fasterxml.jackson.databind.JsonNode res = client.listEntries(localFormId, localPageSize, localOffset);

                com.fasterxml.jackson.databind.JsonNode entriesNode = res;
                if (res != null && res.isObject() && res.has("entries")) entriesNode = res.get("entries");
                if (entriesNode == null || !entriesNode.isArray()) {
                    publish("Unexpected response: " + (res == null ? "null" : res.toString()));
                    return null;
                }

                for (com.fasterxml.jackson.databind.JsonNode entry : entriesNode) {
                    final GF224EntryPreview p = GFForm224Mapper.map(entry);
                    SwingUtilities.invokeLater(() -> {
                        previews.add(p);
                        model.addRow(new Object[]{
                                p.entryId, p.dateCreatedRaw, p.griffeIdCliente, p.griffeIdSpedizione,
                                p.companyName, p.vat, p.itemsNumber, p.totalPrice, p.deliveryDate, p.listino
                        });
                    });
                }

                publish("Loaded UNREAD entries: " + entriesNode.size());
                return null;
            }

            @Override protected void process(List<String> chunks) { for (String s : chunks) log(s); }
            @Override protected void done() {
                try { get(); }
                catch (Exception ex) { log("ERROR: " + ex.getMessage()); showFullException("GF load failed", ex); }
            }
        };

        w.execute();
    }

    private void onImportOrder() {
        int idx = table.getSelectedRow();
        if (idx < 0 || idx >= previews.size()) {
            JOptionPane.showMessageDialog(owner, "Seleziona una entry prima di importare.", "Import", JOptionPane.WARNING_MESSAGE);
            return;
        }

        GF224EntryPreview gf = previews.get(idx);

        // ALWAYS show confirm dialog:
        // - preselect GF customer/dest if present
        // - if GF missing customer, OK stays disabled until user selects one
        CustomerConfirmDialog dlg = new CustomerConfirmDialog(
                owner,
                dbUrl.get(), dbUser.get(), dbPass.get(),
                gf.entryId,
                gf.companyName,
                gf.griffeIdCliente,
                gf.griffeIdSpedizione
        );
        dlg.setVisible(true);
        if (!dlg.confirmed) return;

        String chosenCliente = dlg.selectedCdCliente;           // always non-null
        String chosenDest = dlg.selectedCdDestinazione;         // nullable

        int ok = JOptionPane.showConfirmDialog(
                owner,
                "Importare l'ordine da Gravity Forms?\nEntry #" + gf.entryId +
                        "\nCliente: " + chosenCliente +
                        "\nDest: " + (chosenDest == null ? "(none)" : chosenDest) +
                        "\nTotale: " + gf.totalPrice,
                "Conferma Import",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (ok != JOptionPane.OK_OPTION) return;

        btnImportOrder.setEnabled(false);
        btnLoad.setEnabled(false);

        final String base = gfBaseUrl.get();
        final String key = gfKey.get();
        final String secret = gfSecret.get();

        final String jdbcUrl = dbUrl.get();
        final String jdbcUser = dbUser.get();
        final String jdbcPass = dbPass.get();

        SwingWorker<OracleOrderCreateService.CreateOrderResult, String> w = new SwingWorker<OracleOrderCreateService.CreateOrderResult, String>() {

            private String summaryText;

            @Override
            protected OracleOrderCreateService.CreateOrderResult doInBackground() throws Exception {
                publish("IMPORT start entryId=" + gf.entryId);

                R07ClienteRecord r07 = null;
                E01TestataOrdineCli e01 = E01Builder.build(gf, r07);

                // Apply chosen customer/destination (always)
                e01.setE01CdCliente(chosenCliente);
                e01.setE01CdDestinazione(chosenDest);

                java.sql.Timestamp dt = GFDateParser.parseToTimestamp(gf.dateCreatedRaw);
                if (dt == null) throw new IllegalStateException("Cannot parse gf.dateCreatedRaw=" + gf.dateCreatedRaw);
                e01.setE01DataOrdine(dt);
                e01.setE01DtRiferCliente(dt);
                e01.setSdDtIns(dt);

                OracleOrderCreateService svc = new OracleOrderCreateService(jdbcUrl, jdbcUser, jdbcPass);
                OracleOrderCreateService.CreateOrderResult res = svc.createOrderFromGF(e01, gf, r07);

                GFClient gfClient = new GFClient(base, key, secret);
                gfClient.markEntryRead(gf.entryId);
                publish("MARK AS READ OK: entryId=" + gf.entryId);

                summaryText = ImportSummaryBuilder.build(gf, res);
                return res;
            }

            @Override
            protected void process(List<String> chunks) { for (String s : chunks) log(s); }

            @Override
            protected void done() {
                btnLoad.setEnabled(true);
                try {
                    get();

                    JTextArea ta = new JTextArea(summaryText, 30, 130);
                    ta.setEditable(false);
                    ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                    JOptionPane.showMessageDialog(
                            owner,
                            new JScrollPane(ta),
                            "Import Summary (GF Entry #" + gf.entryId + ")",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    onLoad();

                } catch (Exception ex) {
                    boolean selOk = table.getSelectedRow() >= 0 && table.getSelectedRow() < previews.size();
                    btnImportOrder.setEnabled(selOk);
                    log("IMPORT/MARKREAD ERROR: " + ex.getMessage());
                    showFullException("Import failed", ex);
                }
            }
        };

        w.execute();
    }

    public void log(String msg) {
        if (logArea == null) return;
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
        System.out.println(msg);
    }

    public void showFullException(String title, Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        JTextArea ta = new JTextArea(sw.toString(), 30, 120);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(owner, new JScrollPane(ta), title, JOptionPane.ERROR_MESSAGE);
    }
}
