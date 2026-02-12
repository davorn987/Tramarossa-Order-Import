package clientImport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Gravity Forms REST API v2 client.
 *
 * Behaviour:
 * - listEntries(...) returns ONLY unread entries (is_read=0)
 * - markEntryRead(entryId) does GET full entry + PUT full entry with is_read=1
 *   (avoids wiping fields when doing partial PUT)
 */
public class GFClient {
    private final String siteBase;   // e.g. https://b2b.tramarossa.it
    private final String apiUser;    // GF REST API consumer key
    private final String apiPass;    // GF REST API consumer secret
    private final ObjectMapper om = new ObjectMapper();

    public GFClient(String siteBase, String apiUser, String apiPass) {
        this.siteBase = stripTrailingSlash(siteBase);
        this.apiUser = apiUser;
        this.apiPass = apiPass;
    }

    /**
     * List ONLY unread entries (is_read=0).
     * Gravity Forms expects `search` as a JSON string.
     */
    public JsonNode listEntries(Integer formId, int pageSize, int offset) throws Exception {
        String url = siteBase + "/wp-json/gf/v2/entries";

        StringBuilder qs = new StringBuilder();
        qs.append("?paging%5Bpage_size%5D=").append(pageSize);
        qs.append("&paging%5Boffset%5D=").append(offset);

        if (formId != null) {
            qs.append("&form_ids%5B%5D=").append(URLEncoder.encode(String.valueOf(formId), "UTF-8"));
        }

        String searchJson = "{\"field_filters\":[{\"key\":\"is_read\",\"value\":\"0\"}]}";
        String searchEnc = URLEncoder.encode(searchJson, StandardCharsets.UTF_8.name());
        qs.append("&search=").append(searchEnc);

        String body = HttpUtils.httpGet(url + qs, apiUser, apiPass);
        return om.readTree(body);
    }

    /**
     * GET /entries/{id} (full entry object)
     */
    public JsonNode getEntry(String entryId) throws Exception {
        if (entryId == null || entryId.trim().isEmpty()) return null;
        String idEnc = URLEncoder.encode(entryId.trim(), "UTF-8");
        String url = siteBase + "/wp-json/gf/v2/entries/" + idEnc;

        String body = HttpUtils.httpGet(url, apiUser, apiPass);
        return om.readTree(body);
    }

    /**
     * Mark entry as read (visualizzato): is_read=1
     *
     * IMPORTANT:
     * We must PUT the FULL entry payload, otherwise GF may overwrite/clear fields.
     */
    public void markEntryRead(String entryId) throws Exception {
        if (entryId == null || entryId.trim().isEmpty()) return;

        JsonNode entry = getEntry(entryId);
        if (entry == null || !entry.isObject()) {
            throw new RuntimeException("GF getEntry returned null/non-object for entryId=" + entryId);
        }

        ObjectNode obj = (ObjectNode) entry;
        obj.put("is_read", 1);

        String idEnc = URLEncoder.encode(entryId.trim(), "UTF-8");
        String url = siteBase + "/wp-json/gf/v2/entries/" + idEnc;

        String payload = om.writeValueAsString(obj);
        HttpUtils.httpPutJson(url, apiUser, apiPass, payload);
    }

    private static String stripTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}