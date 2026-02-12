package clientImport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Simple HTTP helpers using HttpURLConnection (Java 8+),
 * with optional Basic Auth.
 */
public class HttpUtils {

    public static String httpGet(String urlStr) throws IOException {
        return httpGet(urlStr, null, null);
    }

    public static String httpGet(String urlStr, String basicUser, String basicPass) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(20000);
        conn.setDoInput(true);

        if (basicUser != null && basicPass != null) {
            applyBasicAuth(conn, basicUser, basicPass);
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

        String body = readAll(is);

        conn.disconnect();

        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + ": " + body);
        }
        return body;
    }

    /**
     * HTTP PUT with JSON body (Content-Type: application/json), with optional Basic Auth.
     * Returns response body as String.
     */
    public static String httpPutJson(String urlStr, String basicUser, String basicPass, String jsonBody) throws IOException {
        if (jsonBody == null) jsonBody = "";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(20000);
        conn.setDoInput(true);
        conn.setDoOutput(true);

        if (basicUser != null && basicPass != null) {
            applyBasicAuth(conn, basicUser, basicPass);
        }

        byte[] payload = jsonBody.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(payload.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
            os.flush();
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

        String body = readAll(is);

        conn.disconnect();

        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + ": " + body);
        }
        return body;
    }

    private static void applyBasicAuth(HttpURLConnection conn, String basicUser, String basicPass) {
        String token = basicUser + ":" + basicPass;
        String encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + encoded);
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString();
    }
}