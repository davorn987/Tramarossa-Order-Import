package clientImport;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Parses Gravity Forms entry date strings (date_created).
 * Common formats:
 * - "2026-01-25 19:11:31"
 * - "2026-01-25 19:11:31.000"
 */
public class GFDateParser {

    private static final String[] FORMATS = new String[] {
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss"
    };

    public static Timestamp parseToTimestamp(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;

        for (String f : FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(f);
                sdf.setLenient(false);
                return new Timestamp(sdf.parse(t).getTime());
            } catch (ParseException ignored) { }
        }
        return null;
    }
}