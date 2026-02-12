package clientImport;

public class GFRepeaterRow {
    public String modello;
    public String tessuto;
    public String trattamento;
    public String colore;

    // size fields from JSON (size_1..size_13)
    public String size1;
    public String size2;
    public String size3;
    public String size4;
    public String size5;
    public String size6;
    public String size7;
    public String size8;
    public String size9;
    public String size10;
    public String size11;
    public String size12;
    public String size13;

    public int totalePezzi;

    /** from JSON "prezzo" */
    public String prezzoUnit;     // "115,00"

    /** from JSON "prezzo_totale" */
    public String prezzoTotale;   // "690,00"

    /** NEW: from JSON "prezzo_eur" (EUR reference price) */
    public String prezzoEur;      // "125,00"

    public String tessutoProduzione;
    public String fk1;
    public String fk2;
    public String fk3;
    public String fk6;

    public boolean confirmed;
}