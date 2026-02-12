package clientImport;

import java.sql.Timestamp;

public class R04CambioRecord {
    public String cdValuta;          // R04_CD_VALUTA
    public Timestamp dataCambio;     // R04_DATA_CAMBIO
    public Double importoCambio;     // R04_IMPORTO_CAMBIO
    public Double importoCambioEuro; // R04_IMPORTO_CAMBIO_EURO

    @Override
    public String toString() {
        return "R04CambioRecord{" +
                "cdValuta='" + cdValuta + '\'' +
                ", dataCambio=" + dataCambio +
                ", importoCambio=" + importoCambio +
                ", importoCambioEuro=" + importoCambioEuro +
                '}';
    }
}