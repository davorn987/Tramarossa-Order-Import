package clientImport;

public class G25OperatoreRecord {
    public String cdOperatore;   // G25_CD_OPERATORE
    public String stRecord;      // G25_ST_RECORD
    public String dsOperatore;   // G25_DS_OPERATORE

    @Override
    public String toString() {
        // usato dalla JComboBox (mostra label)
        return dsOperatore != null ? dsOperatore : (cdOperatore != null ? cdOperatore : "");
    }
}