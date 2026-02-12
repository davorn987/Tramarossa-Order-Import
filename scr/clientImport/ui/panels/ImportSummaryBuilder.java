package clientImport.ui.panels;

import clientImport.GF224EntryPreview;
import clientImport.OracleOrderCreateService;

public class ImportSummaryBuilder {

    public static String build(GF224EntryPreview gf, OracleOrderCreateService.CreateOrderResult res) {
        StringBuilder sb = new StringBuilder();

        sb.append("GF\n");
        sb.append("  entryId:    ").append(nn(gf.entryId)).append("\n");
        sb.append("  dateCreated:").append(nn(gf.dateCreatedRaw)).append("\n");
        sb.append("  cdCliente:  ").append(nn(gf.griffeIdCliente)).append("\n");
        sb.append("  dest:       ").append(nn(gf.griffeIdSpedizione)).append("\n");
        sb.append("  listino(GF):").append(nn(gf.listino)).append("\n\n");

        sb.append("IMPORT RESULT\n");
        sb.append("  anno:       ").append(res.annoOrdine).append("\n");
        sb.append("  nr:         ").append(res.nrOrdine).append("\n");
        sb.append("  righeE12:   ").append(res.insertedE12Lines).append("\n");
        sb.append("  fallback000000: ").append(res.usedFallbackClient000000).append("\n");
        sb.append("  r07Found:   ").append(res.r07Found).append("\n");
        sb.append("  r10Found:   ").append(res.r10Found).append("\n");
        sb.append("  destDropped:").append(res.destinationDropped).append("\n\n");

        sb.append("E01 FIELDS (final)\n");
        sb.append("  E01_CD_IVA:       ").append(nn(res.e01CdIvaFinal)).append("\n");
        sb.append("  E01_CD_AGENTE:    ").append(nn(res.e01CdAgenteFinal)).append("\n");
        sb.append("  E01_CD_PAGAMENTO: ").append(nn(res.e01CdPagamentoFinal)).append("\n");
        sb.append("  E01_CD_VALUTA:    ").append(nn(res.e01CdValutaFinal)).append("\n");
        sb.append("  E01_CD_ABI:       ").append(nn(res.e01CdAbiFinal)).append("\n");
        sb.append("  E01_CD_CAB:       ").append(nn(res.e01CdCabFinal)).append("\n");
        sb.append("  E01_CD_TRASPORTO: ").append(nn(res.e01CdTrasportoFinal)).append("\n");
        sb.append("  E01_CD_VETTORE:   ").append(nn(res.e01CdVettoreFinal)).append("\n");
        sb.append("  E01_CD_LINGUA:    ").append(nn(res.e01CdLinguaFinal)).append("\n");
        sb.append("  E01_CD_ZONA:      ").append(nn(res.e01CdZonaFinal)).append("\n\n");

        sb.append("LISTINO GROUP CHECK\n");
        sb.append("  keyC32:     ").append(nn(res.keyC32)).append("\n");
        sb.append("  idListinoResolved: ").append(res.idListinoResolved == null ? "(null)" : res.idListinoResolved).append("\n");
        sb.append("  fallbackUsed(57):  ").append(res.idListinoFallbackUsed).append("\n\n");

        if (res.allListiniByGroup != null && !res.allListiniByGroup.isEmpty()) {
            sb.append("ALL LISTINI FOR GROUP (C30_ST_RECORD='V')\n");
            for (java.util.Map.Entry<String, Integer> e : res.allListiniByGroup.entrySet()) {
                sb.append("  ").append(String.format("%-6s", e.getKey())).append(" -> ").append(e.getValue()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("SQL CHECKS (copy/paste)\n");
        sb.append("-- Header (E01)\n");
        sb.append("SELECT E01_CD_CLIENTE, E01_CD_DESTINAZIONE, E01_CD_IVA, E01_ID_LISTINO, E01_CD_VALUTA, E01_CD_LISTINO, E01_CD_STAGIONE, E01_MEMO,\n");
        sb.append("       E01_CD_AGENTE, E01_CD_PAGAMENTO, E01_CD_ABI, E01_CD_CAB, E01_CD_TRASPORTO, E01_CD_VETTORE, E01_CD_LINGUA, E01_CD_ZONA\n");
        sb.append("FROM MADEVENETO.E01_TESTATA_ORDINI_CLI\n");
        sb.append("WHERE E01_ANNO_ORDINE = ").append(res.annoOrdine).append(" AND E01_NR_ORDINE = ").append(res.nrOrdine).append(";\n\n");

        sb.append("-- Lines (E12) - IVA check\n");
        sb.append("SELECT E12_RIGA, E12_CD_IVA, E12_FK_1, E12_FK_2, E12_FK_3, E12_FK_6\n");
        sb.append("FROM MADEVENETO.E12_RIGHE_DETT_ORD_CLI\n");
        sb.append("WHERE E12_ANNO_ORDINE = ").append(res.annoOrdine).append(" AND E12_NR_ORDINE = ").append(res.nrOrdine).append("\n");
        sb.append("ORDER BY E12_RIGA;\n\n");

        sb.append("-- Listino group check\n");
        if (res.keyC32 != null && !res.keyC32.trim().isEmpty()) {
            sb.append("SELECT C30_CD_STAGIONE, C30_ID_LISTINO\n");
            sb.append("FROM MADEVENETO.C30_TEST_LIST_CLI\n");
            sb.append("WHERE C30_ST_RECORD='V' AND C30_KEY_C32='").append(res.keyC32).append("'\n");
            sb.append("ORDER BY C30_CD_STAGIONE;\n");
        } else {
            sb.append("-- keyC32 not available\n");
        }

        return sb.toString();
    }

    private static String nn(String s) {
        if (s == null) return "(null)";
        String t = s.trim();
        return t.isEmpty() ? "(empty)" : t;
    }
}