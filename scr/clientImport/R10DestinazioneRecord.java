package clientImport;

public class R10DestinazioneRecord {
    public String cdCliente;        // R10_CD_CLIENTE
    public String cdDestinazione;   // R10_CD_DESTINAZIONE

    public String ragioneSoc;       // R10_RAGIONE_SOC
    public String indirizzo;        // R10_INDIRIZZO
    public String cap;              // R10_CAP
    public String citta;            // R10_CITTA
    public String stato;            // R10_STATO
    public String telefono;         // R10_TELEFONO
    public String email;            // R10_EMAIL_ADDDRES
    public String cdNazione;        // R10_CD_NAZIONE
    public String cdAzienda;        // R10_CD_AZIENDA

    @Override
    public String toString() {
        return "R10DestinazioneRecord{" +
                "cdCliente='" + cdCliente + '\'' +
                ", cdDestinazione='" + cdDestinazione + '\'' +
                ", ragioneSoc='" + ragioneSoc + '\'' +
                ", indirizzo='" + indirizzo + '\'' +
                ", cap='" + cap + '\'' +
                ", citta='" + citta + '\'' +
                ", stato='" + stato + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", cdNazione='" + cdNazione + '\'' +
                ", cdAzienda='" + cdAzienda + '\'' +
                '}';
    }
}