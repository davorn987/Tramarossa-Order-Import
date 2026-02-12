package clientImport;

/**
 * Generic combo option for "code + label".
 */
public class UiOption {
    public final String code;
    public final String label;

    public UiOption(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @Override
    public String toString() {
        return label != null ? label : (code != null ? code : "");
    }
}