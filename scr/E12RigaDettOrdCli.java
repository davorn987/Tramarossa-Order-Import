package clientImport;

import java.sql.Timestamp;
import java.util.Arrays;

/**
 * DTO for MADEVENETO.E12_RIGHE_DETT_ORD_CLI (subset + arrays 1..41).
 *
 * Arrays are 41 elements (index 0 -> 01, index 40 -> 41).
 * We store numeric ids as Strings where the DB column is char/varchar.
 */
public class E12RigaDettOrdCli {

    public String cdAzienda;      // AZ1
    public int annoOrdine;        // E01_ANNO_ORDINE
    public int nrOrdine;          // E01_NR_ORDINE
    public int riga;              // 1..N

    public String stRecord;       // V
    public int stModifica;        // 0

    public String cdIva;          // E01_CD_IVA
    public Timestamp dataRigaOrdine;        // E01_DATA_ORDINE
    public Timestamp dataConsConfermata;    // GF delivery date (field 6) at 00:00:00.000

    public String flagTipoRiga;   // P
    public String idListino;      // NULL
    public String idAccordo;      // NULL
    public Double percAbbin;      // NULL

    public String cdTipoArticolo; // PF

    public String fk1;
    public String fk2;
    public String fk3;
    public String fk6;

    public String note; // optional

    // 01..41
    public String[] cod = new String[41];     // E12_COD01..E12_COD41 (G82_KEY ordered by sequence)
    public int[] qtaOr = new int[41];         // E12_QTAORxx from form
    public int[] qtaPr = new int[41];         // always 0
    public int[] qtaSp = new int[41];         // always 0
    public double[] prUv = new double[41];    // price unit vend (same for all sizes)
    public double[] prUe = new double[41];    // price unit eur (same for all sizes)
    public String[] flgC = new String[41];    // N

    public String flagProduzione; // S
    public String flagChiuso;     // N
    public Double sconto;         // NULL

    public E12RigaDettOrdCli() {
        Arrays.fill(cod, null);
        Arrays.fill(flgC, "N");
        // ints default to 0
        // doubles default to 0.0
    }
}