package clientImport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.swing.*;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * GUI tool to inspect AND update Gravity Forms entry fields.
 *
 * Injection modes:
 * - Plain String mode: inject raw text as a STRING value
 * - JSON mode: validate JSON and store its COMPACT form as a STRING value (useful for custom repeaters saved as JSON-string)
 *
 * Update strategy:
 * - GET the full entry first (to keep everything else unchanged)
 * - Create a copy of the entry JSON, overwrite ONLY entry[fieldKey]
 * - PUT the full entry JSON back
 * - GET verify and show before/after for the updated field
 *
 * NOTE:
 * - This matches your requirement: "aggiungere il campo da inviare, lasciando invariato il resto".
 */
public class GFRepeaterDebuggerApp extends JFrame {

    private final ObjectMapper om = new ObjectMapper();

    private JTextField txtBaseUrl;
    private JTextField txtKey;
    private JPasswordField txtSecret;

    private JTextField txtEntryId;
    private JTextField txtFieldKey;

    private JButton btnLoad;
    private JButton btnListKeys;
    private JButton btnValidate;
    private JButton btnInject;

    private JCheckBox chkPlainString;

    private JTextArea txtRaw;
    private JTextArea txtPretty;
    private JTextArea txtInject;

    private JLabel lblInfo;

    public GFRepeaterDebuggerApp() {
        super("GF Field Debugger (view + list keys + validate + inject)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 1020);
        initUi();
    }

    private void initUi() {
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        txtBaseUrl = new JTextField("https://b2b.tramarossa.it", 28);
        txtKey = new JTextField("", 30);
        txtSecret = new JPasswordField("", 30);

        txtEntryId = new JTextField("", 10);
        txtFieldKey = new JTextField("73", 10);

        btnLoad = new JButton("Load field");
        btnListKeys = new JButton("List field keys");
        btnValidate = new JButton("Validate");
        btnInject = new JButton("Inject");

        chkPlainString = new JCheckBox("Plain string (no JSON)");
        chkPlainString.setToolTipText("If checked, the text is injected as-is as a STRING value (e.g. customersss).");

        int row = 0;

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        top.add(new JLabel("GF Base URL:"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        top.add(txtBaseUrl, c);

        row++;
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        top.add(new JLabel("GF API Key:"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        top.add(txtKey, c);

        row++;
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        top.add(new JLabel("GF API Secret:"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        top.add(txtSecret, c);

        row++;
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row4.add(new JLabel("Entry ID:"));
        row4.add(txtEntryId);
        row4.add(new JLabel("Field key:"));
        row4.add(txtFieldKey);
        row4.add(btnLoad);
        row4.add(btnListKeys);
        row4.add(btnValidate);
        row4.add(btnInject);
        row4.add(chkPlainString);

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        top.add(new JLabel("Target:"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        top.add(row4, c);

        lblInfo = new JLabel(" ");
        row++;
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; c.weightx = 1;
        top.add(lblInfo, c);

        getContentPane().add(top, BorderLayout.NORTH);

        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        txtRaw = new JTextArea(9, 120);
        txtPretty = new JTextArea(18, 120);
        txtInject = new JTextArea(12, 120);

        txtRaw.setEditable(false);
        txtPretty.setEditable(false);

        txtInject.setFont(mono);
        txtRaw.setFont(mono);
        txtPretty.setFont(mono);

        JSplitPane splitTop = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(wrapWithTitle("Raw value (entry[fieldKey])", txtRaw)),
                new JScrollPane(wrapWithTitle("Parsed / Pretty (best effort)", txtPretty))
        );
        splitTop.setResizeWeight(0.33);

        JPanel injectPanel = wrapWithTitle(
                "Inject (paste here). JSON mode expects JSON; Plain string mode injects raw text as a string.",
                new JScrollPane(txtInject)
        );

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitTop, injectPanel);
        mainSplit.setResizeWeight(0.60);

        getContentPane().add(mainSplit, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> onLoad());
        btnListKeys.addActionListener(e -> onListKeys());
        btnValidate.addActionListener(e -> onValidate());
        btnInject.addActionListener(e -> onInject());
    }

    private JPanel wrapWithTitle(String title, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void onListKeys() {
        final String base = txtBaseUrl.getText().trim();
        final String key = txtKey.getText().trim();
        final String secret = new String(txtSecret.getPassword()).trim();
        final String entryId = txtEntryId.getText().trim();

        if (entryId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Entry ID is required.", "Missing input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        disableButtons();
        lblInfo.setText("Listing keys for entry " + entryId + " ...");

        SwingWorker<Void, Void> w = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                GFClient client = new GFClient(base, key, secret);
                JsonNode entryNode = client.getEntry(entryId);

                if (entryNode == null || !entryNode.isObject()) {
                    throw new RuntimeException("Entry not found / unexpected JSON type");
                }

                ObjectNode entry = (ObjectNode) entryNode;
                List<String> keys = new ArrayList<>();
                Iterator<String> it = entry.fieldNames();
                while (it.hasNext()) keys.add(it.next());
                Collections.sort(keys);

                SwingUtilities.invokeLater(() -> showKeysDialog(keys));
                return null;
            }

            @Override protected void done() {
                enableButtons();
                try { get(); lblInfo.setText("Keys listed."); }
                catch (Exception ex) { lblInfo.setText("ERROR: " + ex.getMessage()); showException("List keys failed", ex); }
            }
        };

        w.execute();
    }

    private void showKeysDialog(List<String> keys) {
        JTextArea ta = new JTextArea(30, 70);
        ta.setEditable(false);

        StringBuilder sb = new StringBuilder();
        sb.append("Field keys present in entry JSON:\n\n");
        for (String k : keys) sb.append(k).append('\n');

        ta.setText(sb.toString());
        ta.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Entry keys", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onLoad() {
        txtRaw.setText("");
        txtPretty.setText("");
        lblInfo.setText(" ");

        final String base = txtBaseUrl.getText().trim();
        final String key = txtKey.getText().trim();
        final String secret = new String(txtSecret.getPassword()).trim();
        final String entryId = txtEntryId.getText().trim();
        final String fieldKey = txtFieldKey.getText().trim();

        if (entryId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Entry ID is required.", "Missing input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fieldKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Field key is required (e.g. 73).", "Missing input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        disableButtons();
        lblInfo.setText("Loading entry " + entryId + " ...");

        SwingWorker<Void, Void> w = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                GFClient client = new GFClient(base, key, secret);
                JsonNode entry = client.getEntry(entryId);

                if (entry == null) throw new RuntimeException("Entry not found / null response");
                if (!entry.isObject()) throw new RuntimeException("Unexpected entry type: " + entry.getNodeType());

                JsonNode fieldNode = entry.get(fieldKey);

                final String rawText;
                if (fieldNode == null || fieldNode.isNull()) rawText = "";
                else if (fieldNode.isTextual()) rawText = fieldNode.asText();
                else rawText = fieldNode.toString();

                String pretty = tryPretty(rawText);

                SwingUtilities.invokeLater(() -> {
                    txtRaw.setText(rawText);
                    txtRaw.setCaretPosition(0);

                    txtPretty.setText(pretty);
                    txtPretty.setCaretPosition(0);

                    txtInject.setText(rawText);
                    txtInject.setCaretPosition(0);

                    lblInfo.setText("Loaded. Field '" + fieldKey + "' nodeType=" +
                            (fieldNode == null ? "MISSING" : fieldNode.getNodeType().name()) +
                            " rawLength=" + rawText.length());
                });

                return null;
            }

            @Override protected void done() {
                enableButtons();
                try { get(); }
                catch (Exception ex) { lblInfo.setText("ERROR: " + ex.getMessage()); showException("Load failed", ex); }
            }
        };

        w.execute();
    }

    private void onValidate() {
        String raw = txtInject.getText();
        if (raw == null) raw = "";

        boolean plain = chkPlainString.isSelected();

        if (plain) {
            String s = raw;
            JTextArea ta = new JTextArea(16, 110);
            ta.setEditable(false);
            ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            ta.setText("PLAIN STRING MODE\n\nValue length: " + s.length() + "\n\nValue:\n" + s);
            ta.setCaretPosition(0);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Validate OK", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            String normalized = normalizeForParsing(raw);
            JsonNode n = om.readTree(normalized);
            String compact = om.writeValueAsString(n);
            String pretty = om.writerWithDefaultPrettyPrinter().writeValueAsString(n);

            JTextArea ta = new JTextArea(26, 110);
            ta.setEditable(false);
            ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            ta.setText(
                    "JSON MODE - VALID JSON\n\n" +
                            "Normalized length: " + normalized.length() + "\n" +
                            "Compact length   : " + compact.length() + "\n\n" +
                            "Pretty:\n" + pretty
            );
            ta.setCaretPosition(0);

            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Validate OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showValidationError(ex);
        }
    }

    private void onInject() {
        final String base = txtBaseUrl.getText().trim();
        final String key = txtKey.getText().trim();
        final String secret = new String(txtSecret.getPassword()).trim();
        final String entryId = txtEntryId.getText().trim();
        final String fieldKey = txtFieldKey.getText().trim();
        final String injectText = txtInject.getText();

        if (entryId.isEmpty() || fieldKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Entry ID and Field key are required.", "Missing input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final boolean plain = chkPlainString.isSelected();

        // what we'll store in entry[fieldKey] (always a STRING)
        final String storedValue;
        if (plain) {
            storedValue = injectText == null ? "" : injectText;
        } else {
            try {
                storedValue = normalizeInjectJsonToCompact(injectText);
            } catch (Exception ex) {
                showValidationError(ex);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confirm INJECT?\n\nEntry ID: " + entryId + "\nField key: " + fieldKey + "\nMode: " + (plain ? "PLAIN STRING" : "JSON") + "\n" +
                        "Stored value length: " + storedValue.length() + "\n\n" +
                        "Action: GET full entry -> overwrite ONLY that field -> PUT full entry -> GET verify.",
                "Confirm inject",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        disableButtons();
        lblInfo.setText("Injecting into entry " + entryId + " field '" + fieldKey + "' ...");

        SwingWorker<Void, Void> w = new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                GFClient client = new GFClient(base, key, secret);

                // GET full entry (base)
                JsonNode entryNode = client.getEntry(entryId);
                if (entryNode == null || !entryNode.isObject()) {
                    throw new RuntimeException("Entry not found / unexpected JSON type");
                }
                ObjectNode fullEntry = (ObjectNode) entryNode;

                // capture before
                JsonNode beforeNode = fullEntry.get(fieldKey);
                String before = (beforeNode == null || beforeNode.isNull()) ? "" : (beforeNode.isTextual() ? beforeNode.asText() : beforeNode.toString());

                // overwrite only the selected field (keep everything else unchanged)
                fullEntry.put(fieldKey, storedValue);

                // PUT full entry back
                String url = stripTrailingSlash(base) + "/wp-json/gf/v2/entries/" +
                        URLEncoder.encode(entryId.trim(), StandardCharsets.UTF_8.name());

                String payload = om.writeValueAsString(fullEntry);
                String putResponse = HttpUtils.httpPutJson(url, key, secret, payload);

                // verify with GET
                JsonNode afterEntry = client.getEntry(entryId);
                JsonNode afterNode = (afterEntry != null) ? afterEntry.get(fieldKey) : null;
                String after = (afterNode == null || afterNode.isNull()) ? "" : (afterNode.isTextual() ? afterNode.asText() : afterNode.toString());

                final String msg =
                        "INJECT DONE (full entry PUT)\n\n" +
                                "Field key: " + fieldKey + "\n" +
                                "Mode: " + (plain ? "PLAIN STRING" : "JSON") + "\n" +
                                "Before length: " + before.length() + "\n" +
                                "After length : " + after.length() + "\n" +
                                "Changed?      : " + (!before.equals(after)) + "\n\n" +
                                "PUT response:\n" + putResponse;

                SwingUtilities.invokeLater(() -> {
                    JTextArea ta = new JTextArea(msg, 26, 110);
                    ta.setEditable(false);
                    ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                    ta.setCaretPosition(0);
                    JOptionPane.showMessageDialog(GFRepeaterDebuggerApp.this, new JScrollPane(ta), "Inject result", JOptionPane.INFORMATION_MESSAGE);
                });

                return null;
            }

            @Override protected void done() {
                enableButtons();
                try {
                    get();
                    lblInfo.setText("Inject finished (see dialog). Reloading...");
                    onLoad();
                } catch (Exception ex) {
                    lblInfo.setText("ERROR: " + ex.getMessage());
                    showException("Inject failed", ex);
                }
            }
        };

        w.execute();
    }

    /**
     * Normalize for parsing:
     * - strips BOM/zero width
     * - trims
     * - fixes "" => "
     * - fixes broken empty values :"(comma) -> :""
     * - cuts off anything after last ] or }
     */
    private String normalizeForParsing(String raw) {
        if (raw == null) return "";
        String s = raw;

        s = s.replace("\uFEFF", "");
        s = s.replace("\u200B", "");

        s = s.trim();
        if (s.isEmpty()) return "";

        s = s.replace("\"\"", "\"").trim();

        // Repair broken empty-string values like :" ,
        s = s.replaceAll(":\"(?=\\s*,)", ":\"\"");

        int lastArr = s.lastIndexOf(']');
        int lastObj = s.lastIndexOf('}');
        int cut = Math.max(lastArr, lastObj);
        if (cut >= 0 && cut < s.length() - 1) {
            s = s.substring(0, cut + 1).trim();
        }

        return s;
    }

    private String normalizeInjectJsonToCompact(String raw) throws Exception {
        String s = normalizeForParsing(raw);
        if (s.isEmpty()) return "[]";
        JsonNode n = om.readTree(s);
        return om.writeValueAsString(n);
    }

    private String tryPretty(String raw) {
        if (raw == null) return "";
        try {
            JsonNode n = om.readTree(raw);
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(n);
        } catch (Exception ignore) {}

        String s = normalizeForParsing(raw);
        try {
            JsonNode n = om.readTree(s);
            return om.writerWithDefaultPrettyPrinter().writeValueAsString(n);
        } catch (Exception ignore) {}

        return "(Not valid JSON / cannot parse)\n\n" + raw;
    }

    private void showValidationError(Exception ex) {
        String message = ex.getMessage();
        if (message == null) message = ex.toString();

        JTextArea ta = new JTextArea(18, 110);
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setText("INVALID INPUT\n\n" + message + "\n\n" +
                "Tip:\n" +
                "- JSON mode: una stringa deve essere tra virgolette (es. \"customersss\").\n" +
                "- Oppure spunta 'Plain string (no JSON)'.");
        ta.setCaretPosition(0);

        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Invalid", JOptionPane.ERROR_MESSAGE);
    }

    private void disableButtons() {
        btnLoad.setEnabled(false);
        btnListKeys.setEnabled(false);
        btnValidate.setEnabled(false);
        btnInject.setEnabled(false);
    }

    private void enableButtons() {
        btnLoad.setEnabled(true);
        btnListKeys.setEnabled(true);
        btnValidate.setEnabled(true);
        btnInject.setEnabled(true);
    }

    private void showException(String title, Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        JTextArea ta = new JTextArea(sw.toString(), 26, 120);
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), title, JOptionPane.ERROR_MESSAGE);
    }

    private static String stripTrailingSlash(String s) {
        if (s == null) return "";
        String t = s.trim();
        while (t.endsWith("/")) t = t.substring(0, t.length() - 1);
        return t;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GFRepeaterDebuggerApp app = new GFRepeaterDebuggerApp();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}