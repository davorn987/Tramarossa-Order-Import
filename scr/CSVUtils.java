package clientImport;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple CSV parser that handles fields optionally quoted with " and separated by ';'.
 * Not a full replacement for a library but sufficient for semicolon-separated CSVs with quoted fields.
 */
public class CSVUtils {

    public static List<String[]> readAll(File file, String charsetName) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // skip empty lines
                if (line.trim().isEmpty()) continue;
                rows.add(parseLine(line));
            }
        }
        return rows;
    }

    public static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                // handle double quotes escaping
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++; // skip next
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ';' && !inQuotes) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        fields.add(cur.toString());
        return fields.toArray(new String[0]);
    }
}