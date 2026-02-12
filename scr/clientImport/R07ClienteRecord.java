package clientImport;

/**
 * DTO for MADEVENETO.R07_CLIENTI.
 * Includes the fields needed to map to E01_TESTATA_ORDINI_CLI.
 */
public class R07ClienteRecord {

    // identity / anagraphics
    public String cdCliente;          // R07_CD_CLIENTE
    public String ragioneSoc;         // R07_RAGIONE_SOC
    public String indirizzo;          // R07_INDIRIZZO
    public String cap;               // R07_CAP
    public String citta;             // R07_CITTA
    public String provincia;         // R07_PROVINCIA
    public String stato;             // R07_STATO
    public String partitaIva;        // R07_PARTITA_IVA
    public String telefono;          // R07_TELEFONO
    public String nome;              // R07_NOME
    public String cognome;           // R07_COGNOME
    public String codInterno;        // R07_COD_INTERNO_CLIENTE
    public String email;             // R07_EMAIL_ADDDRES

    // mapping fields (already used)
    public String cdPagamento;       // R07_CD_PAGAMENTO
    public String cdNazione;         // R07_CD_NAZIONE
    public String cdLingua;          // R07_CD_LINGUA
    public String cdZona;            // R07_CD_ZONA
    public String http;              // R07_HTTP
    public String cdAgente;          // R07_CD_AGENTE
    public String cdValuta;          // R07_CD_VALUTA
    public String cdModConsegna;     // R07_CD_MOD_CONSEGNA
    public String cdVettore;         // R07_CD_VETTORE
    public String cdIva;             // R07_CD_IVA
    public String cdAbi;             // R07_CD_ABI
    public String cdCab;             // R07_CD_CAB
    public String contoCorrente;     // R07_CONTO_CORRENTE
    public String cdTrasporto;       // R07_CD_TRASPORTO
    public String cdImballo;         // R07_CD_IMBALLO

    // NEW: discount code to be resolved in C23
    public String cdScontoMaggioraz; // R07_CD_SCONTO_MAGGIORAZ

    // NEW: listino group key (for listino resolution via C30/C32)
    public String keyC32;            // R07_KEY_C32

    @Override
    public String toString() {
        return "R07ClienteRecord{" +
                "cdCliente='" + cdCliente + '\'' +
                ", ragioneSoc='" + ragioneSoc + '\'' +
                ", cdIva='" + cdIva + '\'' +
                ", cdValuta='" + cdValuta + '\'' +
                ", cdPagamento='" + cdPagamento + '\'' +
                ", cdAgente='" + cdAgente + '\'' +
                ", keyC32='" + keyC32 + '\'' +
                ", cdScontoMaggioraz='" + cdScontoMaggioraz + '\'' +
                '}';
    }
}