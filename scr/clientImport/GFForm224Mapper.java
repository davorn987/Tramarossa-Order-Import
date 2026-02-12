package clientImport;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Extracts mapped fields from a Gravity Forms entry (form 224).
 * Mapping based on your merge tags / field IDs.
 */
public class GFForm224Mapper {

    private static String getField(JsonNode entry, String fieldId) {
        if (entry == null || fieldId == null || fieldId.trim().isEmpty()) return "";
        JsonNode v = entry.get(fieldId);
        if (v == null || v.isNull()) return "";
        return v.asText("");
    }

    private static String join2(String a, String b) {
        String aa = (a == null) ? "" : a.trim();
        String bb = (b == null) ? "" : b.trim();
        if (aa.isEmpty()) return bb;
        if (bb.isEmpty()) return aa;
        return aa + " " + bb;
    }

    public static GF224EntryPreview map(JsonNode entry) {
        GF224EntryPreview o = new GF224EntryPreview();

        o.entryId = entry.path("id").asText("");
        o.dateCreatedRaw = entry.path("date_created").asText(""); // IMPORTANT for E01 dates

        // Totals / ids
        o.griffeIdCliente = getField(entry, "56");
        o.griffeIdSpedizione = getField(entry, "57");
        o.itemsNumber = getField(entry, "27");
        o.totalPrice = getField(entry, "3");

        // Company/Billing
        o.companyName = join2(getField(entry, "7.3"), getField(entry, "16.3"));
        o.companyAddress = join2(getField(entry, "8.1"), getField(entry, "18.1"));
        o.companyCity = join2(getField(entry, "8.3"), getField(entry, "18.3"));
        o.companyCountry = join2(getField(entry, "8.6"), getField(entry, "18.6"));
        o.companyCap = join2(getField(entry, "8.5"), getField(entry, "18.5"));
        o.vat = join2(getField(entry, "21"), getField(entry, "35"));
        o.phone = join2(getField(entry, "31"), getField(entry, "38"));
        o.email = join2(getField(entry, "10"), getField(entry, "17"));

        // Delivery/Shipping
        o.deliveryName = join2(getField(entry, "40.3"), getField(entry, "41.3"));
        o.deliveryAddress = join2(getField(entry, "9.1"), getField(entry, "19.1"));
        o.deliveryCity = join2(getField(entry, "9.3"), getField(entry, "19.3"));
        o.deliveryCountry = join2(getField(entry, "9.6"), getField(entry, "19.6"));
        o.deliveryCap = join2(getField(entry, "9.5"), getField(entry, "19.5"));

        // Dates / terms / payment
        o.deliveryDate = getField(entry, "6");
        o.deliveryTerms = join2(getField(entry, "42"), getField(entry, "43"));
        o.paymentTerms = join2(getField(entry, "22"), getField(entry, "36"));
        o.iban = join2(getField(entry, "23"), getField(entry, "37"));

        // Order info
        o.listino = getField(entry, "64");
        o.note = getField(entry, "4");

        // Repeaters (raw JSON string)
        o.repeater73Json = getField(entry, "73");
        o.repeater74Json = getField(entry, "74");
        o.repeater77Json = getField(entry, "77"); // NEW

        return o;
    }
}