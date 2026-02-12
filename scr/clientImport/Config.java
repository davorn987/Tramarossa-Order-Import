package clientImport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Configuration helper that persists settings to a properties file
 * in the user's home directory (~/.clientimport.properties).
 *
 * Note: DB password and API secrets are stored in plain text in the properties file.
 */
public class Config {
    private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator + ".clientimport.properties";
    private static Config instance;
    private final Properties props = new Properties();

    // Defaults (WooCommerce)
    public static final String DEFAULT_WC_BASE_URL = "https://www.tramarossa.it/wp-json/wc/v3";
    public static final String DEFAULT_WC_CONSUMER_KEY = "ck_b930cc87ba0718f52dffea420f5be96b439149dd";
    public static final String DEFAULT_WC_CONSUMER_SECRET = "cs_68af5d88b9f7fb93d9f696bc83373e368e536410";

    // Defaults (Gravity Forms REST API v2)
    public static final String DEFAULT_GF_BASE_URL = "https://b2b.tramarossa.it";
    public static final String DEFAULT_GF_CONSUMER_KEY = "ck_625fc28ee8d227d4dfb54106d300e859a49a9c94";
    public static final String DEFAULT_GF_CONSUMER_SECRET = "cs_929ff863f36b0b0205513bc5a1e7e321e1fd4a81";

    // Defaults (Oracle)
    public static final String DEFAULT_DB_URL = "jdbc:oracle:thin:@//ONISD2.onindustry.local:1521/TRAMDB";
    public static final String DEFAULT_DB_USER = "madeveneto";
    public static final String DEFAULT_DB_PASS = "SD";

    // NEW defaults (GF viewer)
    public static final int DEFAULT_GF_FORM_ID = 224;
    public static final int DEFAULT_GF_PAGE_SIZE = 30;
    public static final int DEFAULT_GF_OFFSET = 0;

    private Config() {
        setDefaults();
        load();
    }

    public static synchronized Config get() {
        if (instance == null) instance = new Config();
        return instance;
    }

    private void setDefaults() {
        // Woo
        props.setProperty("woo.api.base", DEFAULT_WC_BASE_URL);
        props.setProperty("woo.key", DEFAULT_WC_CONSUMER_KEY);
        props.setProperty("woo.secret", DEFAULT_WC_CONSUMER_SECRET);

        // Gravity Forms
        props.setProperty("gf.api.base", DEFAULT_GF_BASE_URL);
        props.setProperty("gf.key", DEFAULT_GF_CONSUMER_KEY);
        props.setProperty("gf.secret", DEFAULT_GF_CONSUMER_SECRET);

        // DB
        props.setProperty("db.url", DEFAULT_DB_URL);
        props.setProperty("db.user", DEFAULT_DB_USER);
        props.setProperty("db.password", DEFAULT_DB_PASS);

        // NEW viewer defaults
        props.setProperty("gf.formId", String.valueOf(DEFAULT_GF_FORM_ID));
        props.setProperty("gf.pageSize", String.valueOf(DEFAULT_GF_PAGE_SIZE));
        props.setProperty("gf.offset", String.valueOf(DEFAULT_GF_OFFSET));
    }

    private void load() {
        File f = new File(CONFIG_FILE);
        if (!f.exists()) return;
        try (InputStream in = new FileInputStream(f)) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("Unable to load config from " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    public synchronized void save() throws IOException {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Client Importer configuration");
        }
    }

    // --- Woo getters / setters ---
    public String getWooApiBase() { return props.getProperty("woo.api.base", DEFAULT_WC_BASE_URL); }
    public void setWooApiBase(String v) { props.setProperty("woo.api.base", v == null ? "" : v); }

    public String getWooKey() { return props.getProperty("woo.key", DEFAULT_WC_CONSUMER_KEY); }
    public void setWooKey(String v) { props.setProperty("woo.key", v == null ? "" : v); }

    public String getWooSecret() { return props.getProperty("woo.secret", DEFAULT_WC_CONSUMER_SECRET); }
    public void setWooSecret(String v) { props.setProperty("woo.secret", v == null ? "" : v); }

    // --- Gravity Forms getters / setters ---
    public String getGfApiBase() { return props.getProperty("gf.api.base", DEFAULT_GF_BASE_URL); }
    public void setGfApiBase(String v) { props.setProperty("gf.api.base", v == null ? "" : v); }

    public String getGfKey() { return props.getProperty("gf.key", DEFAULT_GF_CONSUMER_KEY); }
    public void setGfKey(String v) { props.setProperty("gf.key", v == null ? "" : v); }

    public String getGfSecret() { return props.getProperty("gf.secret", DEFAULT_GF_CONSUMER_SECRET); }
    public void setGfSecret(String v) { props.setProperty("gf.secret", v == null ? "" : v); }

    // --- DB getters / setters ---
    public String getDbUrl() { return props.getProperty("db.url", DEFAULT_DB_URL); }
    public void setDbUrl(String v) { props.setProperty("db.url", v == null ? "" : v); }

    public String getDbUser() { return props.getProperty("db.user", DEFAULT_DB_USER); }
    public void setDbUser(String v) { props.setProperty("db.user", v == null ? "" : v); }

    public String getDbPassword() { return props.getProperty("db.password", DEFAULT_DB_PASS); }
    public void setDbPassword(String v) { props.setProperty("db.password", v == null ? "" : v); }

    // --- NEW: GF viewer settings ---
    public int getGfFormId() { return parseInt("gf.formId", DEFAULT_GF_FORM_ID); }
    public void setGfFormId(int v) { props.setProperty("gf.formId", String.valueOf(v)); }

    public int getGfPageSize() { return parseInt("gf.pageSize", DEFAULT_GF_PAGE_SIZE); }
    public void setGfPageSize(int v) { props.setProperty("gf.pageSize", String.valueOf(v)); }

    public int getGfOffset() { return parseInt("gf.offset", DEFAULT_GF_OFFSET); }
    public void setGfOffset(int v) { props.setProperty("gf.offset", String.valueOf(v)); }

    private int parseInt(String key, int def) {
        try { return Integer.parseInt(props.getProperty(key, String.valueOf(def)).trim()); }
        catch (Exception e) { return def; }
    }

    public synchronized void resetToDefaults() { setDefaults(); }

    /**
     * NOTE: returns a COPY. Use getters/setters to mutate config.
     */
    public Properties asProperties() {
        Properties copy = new Properties();
        copy.putAll(props);
        return copy;
    }
    
 // OPERATORI

    public String getE01OperatoreCd() {
        return props.getProperty("e01.operatore.cd", "000018");
    }
    public void setE01OperatoreCd(String v) {
        props.setProperty("e01.operatore.cd", v == null ? "" : v);
    }

    public String getE01OperatoreDs() {
        return props.getProperty("e01.operatore.ds", "DAVIDEN");
    }
    public void setE01OperatoreDs(String v) {
        props.setProperty("e01.operatore.ds", v == null ? "" : v);
    }
}