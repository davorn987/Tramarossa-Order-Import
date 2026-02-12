package clientImport.ui.dialogs;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {
    public boolean saved = false;

    public String gfBaseUrl;
    public String gfKey;
    public String gfSecret;

    public String dbUrl;
    public String dbUser;
    public String dbPass;

    public int formId;
    public int pageSize;
    public int offset;

    private JTextField txtGfBase;
    private JTextField txtGfKey;
    private JPasswordField txtGfSecret;

    private JTextField txtDbUrl;
    private JTextField txtDbUser;
    private JPasswordField txtDbPass;

    private JTextField txtFormId;
    private JTextField txtPageSize;
    private JTextField txtOffset;

    public SettingsDialog(JFrame owner,
                          String gfBaseUrl, String gfKey, String gfSecret,
                          String dbUrl, String dbUser, String dbPass,
                          int formId, int pageSize, int offset) {
        super(owner, "Settings", true);
        setSize(740, 440);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        this.gfBaseUrl = gfBaseUrl;
        this.gfKey = gfKey;
        this.gfSecret = gfSecret;

        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;

        this.formId = formId;
        this.pageSize = pageSize;
        this.offset = offset;

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;

        txtGfBase = new JTextField(this.gfBaseUrl, 44);
        txtGfKey = new JTextField(this.gfKey, 44);
        txtGfSecret = new JPasswordField(this.gfSecret, 44);

        txtDbUrl = new JTextField(this.dbUrl, 44);
        txtDbUser = new JTextField(this.dbUser, 16);
        txtDbPass = new JPasswordField(this.dbPass, 16);

        txtFormId = new JTextField(String.valueOf(this.formId), 8);
        txtPageSize = new JTextField(String.valueOf(this.pageSize), 8);
        txtOffset = new JTextField(String.valueOf(this.offset), 8);

        r = addRow(panel, c, r, "GF Base URL:", txtGfBase);
        r = addRow(panel, c, r, "GF API Key:", txtGfKey);
        r = addRow(panel, c, r, "GF API Secret:", txtGfSecret);

        r = addRow(panel, c, r, "DB URL:", txtDbUrl);

        JPanel cred = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        cred.add(new JLabel("User:"));
        cred.add(txtDbUser);
        cred.add(new JLabel("Pass:"));
        cred.add(txtDbPass);
        r = addRow(panel, c, r, "DB Credentials:", cred);

        JPanel paging = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        paging.add(new JLabel("Form ID:"));
        paging.add(txtFormId);
        paging.add(new JLabel("Page size:"));
        paging.add(txtPageSize);
        paging.add(new JLabel("Offset:"));
        paging.add(txtOffset);
        r = addRow(panel, c, r, "GF Query:", paging);

        add(panel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton cancel = new JButton("Cancel");
        JButton save = new JButton("Save");

        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> {
            this.gfBaseUrl = txtGfBase.getText().trim();
            this.gfKey = txtGfKey.getText().trim();
            this.gfSecret = new String(txtGfSecret.getPassword()).trim();

            this.dbUrl = txtDbUrl.getText().trim();
            this.dbUser = txtDbUser.getText().trim();
            this.dbPass = new String(txtDbPass.getPassword());

            this.formId = safeParseInt(txtFormId.getText().trim(), this.formId);
            this.pageSize = safeParseInt(txtPageSize.getText().trim(), this.pageSize);
            this.offset = safeParseInt(txtOffset.getText().trim(), this.offset);

            saved = true;
            dispose();
        });

        buttons.add(cancel);
        buttons.add(save);

        add(buttons, BorderLayout.SOUTH);
    }

    private int addRow(JPanel panel, GridBagConstraints c, int row, String label, Component comp) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        panel.add(comp, c);
        return row + 1;
    }

    private int safeParseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception ex) { return def; }
    }
}