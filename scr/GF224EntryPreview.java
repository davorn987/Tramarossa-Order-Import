package clientImport;

/**
 * Mapped preview of Gravity Forms entry (form 224).
 * Added: dateCreatedRaw (GF "date_created") to build E01 order header dates.
 */
public class GF224EntryPreview {
    public String entryId;

    /** GF entry.date_created (raw string) */
    public String dateCreatedRaw;

    // Billing / company
    public String companyName;
    public String companyAddress;
    public String companyCity;
    public String companyCountry;
    public String companyCap;
    public String vat;
    public String phone;
    public String email;

    // Delivery
    public String deliveryName;
    public String deliveryAddress;
    public String deliveryCity;
    public String deliveryCountry;
    public String deliveryCap;
    public String deliveryDate;
    public String deliveryTerms;

    // Payment
    public String paymentTerms;
    public String iban;

    // Order data
    public String listino;        // field 64
    public String note;           // field 4
    public String repeater73Json; // field 73 (raw JSON string)
    public String repeater74Json; // field 74 (raw JSON string)
    public String repeater77Json; // field 77 (raw JSON string)

    // Totals / ids
    public String griffeIdCliente;      // 56
    public String griffeIdSpedizione;   // 57
    public String itemsNumber;          // 27
    public String totalPrice;           // 3

    @Override
    public String toString() {
        return "GF224EntryPreview{entryId=" + entryId +
                ", dateCreatedRaw=" + dateCreatedRaw +
                ", companyName=" + companyName +
                ", totalPrice=" + totalPrice +
                ", items=" + itemsNumber + "}";
    }
}