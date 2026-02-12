package clientImport;

import java.sql.Timestamp;

public class E01_001LW_Row {
    public Timestamp e01DataOrdine;       // E01_DATA_ORDINE
    public Integer e01AnnoOrdine;         // E01_ANNO_ORDINE
    public Integer e01NrOrdine;           // E01_NR_ORDINE
    public String e01CdStagione;          // E01_CD_STAGIONE
    public String e01CdDestinazione;      // E01_CD_DESTINAZIONE
    public String e01CdCliente;           // E01_CD_CLIENTE

    public String dsUtenteBut;            // DS_UTENTE_BUT
    public String e01Memo;                // E01_MEMO

    public Double qtaTotOrdine;           // QTA_TOT_ORDINE
    public Double qtaTotSpedita;          // QTA_TOT_SPEDITA
    public Double assegnatoOrdine;        // ASSEGNATO_ORDINE

    public Double valTotOrdine;           // VAL_TOT_ORDINE
    public Double impNettoOrd;            // IMP_NETTO_ORD

    public String r07RagioneSoc;          // R07_RAGIONE_SOC
    public String r10RagioneSoc;          // R10_RAGIONE_SOC
    public String c07RagSocAgente;        // C07_RAG_SOC_AGENTE

    public String e01CdOperatore;         // E01_CD_OPERATORE
    public String e01FlagChiuso;          // E01_FLAG_CHIUSO
    public String e01CdAgente;            // E01_CD_AGENTE

    public String r02DsNazione;           // R02_DS_NAZIONE
    public String r06DsPagamento;         // R06_DS_PAGAMENTO

    public Timestamp dataConsConf;        // DATA_CONS_CONF

    public Integer righeAnnullate;        // RIGHE_ANNULLATE (0/1 oppure count)
    public String r07ClienteInterno;      // R07_CLIENTE_INTERNO (Y/N o 0/1)
}