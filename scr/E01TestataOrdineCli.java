package clientImport;

import java.sql.Timestamp;

/**
 * DTO for MADEVENETO.E01_TESTATA_ORDINI_CLI.
 * Only fields you listed are included.
 *
 * Note: Types are kept simple (String / Integer / Timestamp / Double).
 * You can adjust to BigDecimal / LocalDateTime later if preferred.
 */
public class E01TestataOrdineCli {

    private Integer e01AnnoOrdine;
    private Integer e01NrOrdine;
    private Timestamp e01DataOrdine;
    private Timestamp e01DataImmissione;
    private String e01CdCliente;
    private String e01CdClienteSped;
    private String e01RiferCliente;
    private Timestamp e01DtRiferCliente;

    // NEW: hidden status fields (required by DB)
    private String e01StRecord;       // "V"
    private Integer e01StModifica;    // 0

    private String e01CdAgente;
    private String e01CdIva;
    private String e01CdAbi;
    private String e01CdCab;
    private String e01CdZona;
    private String e01CdPagamento;
    private String e01CdValuta;
    private String e01CdImballo;
    private String e01CdSpedizioniere;
    private String e01CdVettore;
    private String e01CdTrasporto;
    private String e01CdLingua;
    private String e01CdOperatore;
    private String e01CdAddebitoSpese;
    private String e01CdAspetto;
    private String e01CdCausaleTrasp;
    private Double e01ImportoCambio;
    private Double e01ImportoCambioEuro;
    private String e01CdMagazzino;
    private String e01Memo;
    private String e01FlagAccumBolle;
    private String e01FlagMitDestVett;
    private String e01FlagStampa;
    private String e01FlagChiuso;
    private String e01CdAgente2;
    private String e01CdAgente3;
    private String e01CdAgente4;
    private String e01OrdInterno;
    private Double e01PercExtracosto;
    private Double e01CostoTrasporto;
    private String e01Abbinamento;
    private String e01FlagClav;
    private String e01CdDestinazione;
    private String e01FlagImposta;
    private String e01CdStagione;
    private String e01CdLineaCli;
    private Double e01Sconto01;
    private Double e01Sconto02;
    private String e01CdDivisione;
    private Double e01PercAge1;
    private Double e01PercAge2;
    private Double e01PercAge3;
    private Double e01PercAge4;
    private String e01TipoOrdine;
    private String e01CdListino;
    private Timestamp e01DataConsConfermata;
    private String e01PathImgOrdine;
    private String e01CdAzienda;
    private String e01FlgFlash;
    private String e01IdListino;
    private String e01CdBancaPag;
    private String e01Accuratezza;
    private String e01AccurVar;
    private Timestamp sdDtIns;
    private Timestamp e01DataRichiesta;
    private String e01CdUtente;
    private String e01FlagDaConfermare;
    private String e01FlagOrdProd;

    public Integer getE01AnnoOrdine() { return e01AnnoOrdine; }
    public void setE01AnnoOrdine(Integer e01AnnoOrdine) { this.e01AnnoOrdine = e01AnnoOrdine; }

    public Integer getE01NrOrdine() { return e01NrOrdine; }
    public void setE01NrOrdine(Integer e01NrOrdine) { this.e01NrOrdine = e01NrOrdine; }

    public Timestamp getE01DataOrdine() { return e01DataOrdine; }
    public void setE01DataOrdine(Timestamp e01DataOrdine) { this.e01DataOrdine = e01DataOrdine; }

    public Timestamp getE01DataImmissione() { return e01DataImmissione; }
    public void setE01DataImmissione(Timestamp e01DataImmissione) { this.e01DataImmissione = e01DataImmissione; }

    public String getE01CdCliente() { return e01CdCliente; }
    public void setE01CdCliente(String e01CdCliente) { this.e01CdCliente = e01CdCliente; }

    public String getE01CdClienteSped() { return e01CdClienteSped; }
    public void setE01CdClienteSped(String e01CdClienteSped) { this.e01CdClienteSped = e01CdClienteSped; }

    public String getE01RiferCliente() { return e01RiferCliente; }
    public void setE01RiferCliente(String e01RiferCliente) { this.e01RiferCliente = e01RiferCliente; }

    public Timestamp getE01DtRiferCliente() { return e01DtRiferCliente; }
    public void setE01DtRiferCliente(Timestamp e01DtRiferCliente) { this.e01DtRiferCliente = e01DtRiferCliente; }

    // NEW: hidden status fields getters/setters
    public String getE01StRecord() { return e01StRecord; }
    public void setE01StRecord(String e01StRecord) { this.e01StRecord = e01StRecord; }

    public Integer getE01StModifica() { return e01StModifica; }
    public void setE01StModifica(Integer e01StModifica) { this.e01StModifica = e01StModifica; }

    public String getE01CdAgente() { return e01CdAgente; }
    public void setE01CdAgente(String e01CdAgente) { this.e01CdAgente = e01CdAgente; }

    public String getE01CdIva() { return e01CdIva; }
    public void setE01CdIva(String e01CdIva) { this.e01CdIva = e01CdIva; }

    public String getE01CdAbi() { return e01CdAbi; }
    public void setE01CdAbi(String e01CdAbi) { this.e01CdAbi = e01CdAbi; }

    public String getE01CdCab() { return e01CdCab; }
    public void setE01CdCab(String e01CdCab) { this.e01CdCab = e01CdCab; }

    public String getE01CdZona() { return e01CdZona; }
    public void setE01CdZona(String e01CdZona) { this.e01CdZona = e01CdZona; }

    public String getE01CdPagamento() { return e01CdPagamento; }
    public void setE01CdPagamento(String e01CdPagamento) { this.e01CdPagamento = e01CdPagamento; }

    public String getE01CdValuta() { return e01CdValuta; }
    public void setE01CdValuta(String e01CdValuta) { this.e01CdValuta = e01CdValuta; }

    public String getE01CdImballo() { return e01CdImballo; }
    public void setE01CdImballo(String e01CdImballo) { this.e01CdImballo = e01CdImballo; }

    public String getE01CdSpedizioniere() { return e01CdSpedizioniere; }
    public void setE01CdSpedizioniere(String e01CdSpedizioniere) { this.e01CdSpedizioniere = e01CdSpedizioniere; }

    public String getE01CdVettore() { return e01CdVettore; }
    public void setE01CdVettore(String e01CdVettore) { this.e01CdVettore = e01CdVettore; }

    public String getE01CdTrasporto() { return e01CdTrasporto; }
    public void setE01CdTrasporto(String e01CdTrasporto) { this.e01CdTrasporto = e01CdTrasporto; }

    public String getE01CdLingua() { return e01CdLingua; }
    public void setE01CdLingua(String e01CdLingua) { this.e01CdLingua = e01CdLingua; }

    public String getE01CdOperatore() { return e01CdOperatore; }
    public void setE01CdOperatore(String e01CdOperatore) { this.e01CdOperatore = e01CdOperatore; }

    public String getE01CdAddebitoSpese() { return e01CdAddebitoSpese; }
    public void setE01CdAddebitoSpese(String e01CdAddebitoSpese) { this.e01CdAddebitoSpese = e01CdAddebitoSpese; }

    public String getE01CdAspetto() { return e01CdAspetto; }
    public void setE01CdAspetto(String e01CdAspetto) { this.e01CdAspetto = e01CdAspetto; }

    public String getE01CdCausaleTrasp() { return e01CdCausaleTrasp; }
    public void setE01CdCausaleTrasp(String e01CdCausaleTrasp) { this.e01CdCausaleTrasp = e01CdCausaleTrasp; }

    public Double getE01ImportoCambio() { return e01ImportoCambio; }
    public void setE01ImportoCambio(Double e01ImportoCambio) { this.e01ImportoCambio = e01ImportoCambio; }

    public Double getE01ImportoCambioEuro() { return e01ImportoCambioEuro; }
    public void setE01ImportoCambioEuro(Double e01ImportoCambioEuro) { this.e01ImportoCambioEuro = e01ImportoCambioEuro; }

    public String getE01CdMagazzino() { return e01CdMagazzino; }
    public void setE01CdMagazzino(String e01CdMagazzino) { this.e01CdMagazzino = e01CdMagazzino; }

    public String getE01Memo() { return e01Memo; }
    public void setE01Memo(String e01Memo) { this.e01Memo = e01Memo; }

    public String getE01FlagAccumBolle() { return e01FlagAccumBolle; }
    public void setE01FlagAccumBolle(String e01FlagAccumBolle) { this.e01FlagAccumBolle = e01FlagAccumBolle; }

    public String getE01FlagMitDestVett() { return e01FlagMitDestVett; }
    public void setE01FlagMitDestVett(String e01FlagMitDestVett) { this.e01FlagMitDestVett = e01FlagMitDestVett; }

    public String getE01FlagStampa() { return e01FlagStampa; }
    public void setE01FlagStampa(String e01FlagStampa) { this.e01FlagStampa = e01FlagStampa; }

    public String getE01FlagChiuso() { return e01FlagChiuso; }
    public void setE01FlagChiuso(String e01FlagChiuso) { this.e01FlagChiuso = e01FlagChiuso; }

    public String getE01CdAgente2() { return e01CdAgente2; }
    public void setE01CdAgente2(String e01CdAgente2) { this.e01CdAgente2 = e01CdAgente2; }

    public String getE01CdAgente3() { return e01CdAgente3; }
    public void setE01CdAgente3(String e01CdAgente3) { this.e01CdAgente3 = e01CdAgente3; }

    public String getE01CdAgente4() { return e01CdAgente4; }
    public void setE01CdAgente4(String e01CdAgente4) { this.e01CdAgente4 = e01CdAgente4; }

    public String getE01OrdInterno() { return e01OrdInterno; }
    public void setE01OrdInterno(String e01OrdInterno) { this.e01OrdInterno = e01OrdInterno; }

    public Double getE01PercExtracosto() { return e01PercExtracosto; }
    public void setE01PercExtracosto(Double e01PercExtracosto) { this.e01PercExtracosto = e01PercExtracosto; }

    public Double getE01CostoTrasporto() { return e01CostoTrasporto; }
    public void setE01CostoTrasporto(Double e01CostoTrasporto) { this.e01CostoTrasporto = e01CostoTrasporto; }

    public String getE01Abbinamento() { return e01Abbinamento; }
    public void setE01Abbinamento(String e01Abbinamento) { this.e01Abbinamento = e01Abbinamento; }

    public String getE01FlagClav() { return e01FlagClav; }
    public void setE01FlagClav(String e01FlagClav) { this.e01FlagClav = e01FlagClav; }

    public String getE01CdDestinazione() { return e01CdDestinazione; }
    public void setE01CdDestinazione(String e01CdDestinazione) { this.e01CdDestinazione = e01CdDestinazione; }

    public String getE01FlagImposta() { return e01FlagImposta; }
    public void setE01FlagImposta(String e01FlagImposta) { this.e01FlagImposta = e01FlagImposta; }

    public String getE01CdStagione() { return e01CdStagione; }
    public void setE01CdStagione(String e01CdStagione) { this.e01CdStagione = e01CdStagione; }

    public String getE01CdLineaCli() { return e01CdLineaCli; }
    public void setE01CdLineaCli(String e01CdLineaCli) { this.e01CdLineaCli = e01CdLineaCli; }

    public Double getE01Sconto01() { return e01Sconto01; }
    public void setE01Sconto01(Double e01Sconto01) { this.e01Sconto01 = e01Sconto01; }

    public Double getE01Sconto02() { return e01Sconto02; }
    public void setE01Sconto02(Double e01Sconto02) { this.e01Sconto02 = e01Sconto02; }

    public String getE01CdDivisione() { return e01CdDivisione; }
    public void setE01CdDivisione(String e01CdDivisione) { this.e01CdDivisione = e01CdDivisione; }

    public Double getE01PercAge1() { return e01PercAge1; }
    public void setE01PercAge1(Double e01PercAge1) { this.e01PercAge1 = e01PercAge1; }

    public Double getE01PercAge2() { return e01PercAge2; }
    public void setE01PercAge2(Double e01PercAge2) { this.e01PercAge2 = e01PercAge2; }

    public Double getE01PercAge3() { return e01PercAge3; }
    public void setE01PercAge3(Double e01PercAge3) { this.e01PercAge3 = e01PercAge3; }

    public Double getE01PercAge4() { return e01PercAge4; }
    public void setE01PercAge4(Double e01PercAge4) { this.e01PercAge4 = e01PercAge4; }

    public String getE01TipoOrdine() { return e01TipoOrdine; }
    public void setE01TipoOrdine(String e01TipoOrdine) { this.e01TipoOrdine = e01TipoOrdine; }

    public String getE01CdListino() { return e01CdListino; }
    public void setE01CdListino(String e01CdListino) { this.e01CdListino = e01CdListino; }

    public Timestamp getE01DataConsConfermata() { return e01DataConsConfermata; }
    public void setE01DataConsConfermata(Timestamp e01DataConsConfermata) { this.e01DataConsConfermata = e01DataConsConfermata; }

    public String getE01PathImgOrdine() { return e01PathImgOrdine; }
    public void setE01PathImgOrdine(String e01PathImgOrdine) { this.e01PathImgOrdine = e01PathImgOrdine; }

    public String getE01CdAzienda() { return e01CdAzienda; }
    public void setE01CdAzienda(String e01CdAzienda) { this.e01CdAzienda = e01CdAzienda; }

    public String getE01FlgFlash() { return e01FlgFlash; }
    public void setE01FlgFlash(String e01FlgFlash) { this.e01FlgFlash = e01FlgFlash; }

    public String getE01IdListino() { return e01IdListino; }
    public void setE01IdListino(String e01IdListino) { this.e01IdListino = e01IdListino; }

    public String getE01CdBancaPag() { return e01CdBancaPag; }
    public void setE01CdBancaPag(String e01CdBancaPag) { this.e01CdBancaPag = e01CdBancaPag; }

    public String getE01Accuratezza() { return e01Accuratezza; }
    public void setE01Accuratezza(String e01Accuratezza) { this.e01Accuratezza = e01Accuratezza; }

    public String getE01AccurVar() { return e01AccurVar; }
    public void setE01AccurVar(String e01AccurVar) { this.e01AccurVar = e01AccurVar; }

    public Timestamp getSdDtIns() { return sdDtIns; }
    public void setSdDtIns(Timestamp sdDtIns) { this.sdDtIns = sdDtIns; }

    public Timestamp getE01DataRichiesta() { return e01DataRichiesta; }
    public void setE01DataRichiesta(Timestamp e01DataRichiesta) { this.e01DataRichiesta = e01DataRichiesta; }

    public String getE01CdUtente() { return e01CdUtente; }
    public void setE01CdUtente(String e01CdUtente) { this.e01CdUtente = e01CdUtente; }

    public String getE01FlagDaConfermare() { return e01FlagDaConfermare; }
    public void setE01FlagDaConfermare(String e01FlagDaConfermare) { this.e01FlagDaConfermare = e01FlagDaConfermare; }

    public String getE01FlagOrdProd() { return e01FlagOrdProd; }
    public void setE01FlagOrdProd(String e01FlagOrdProd) { this.e01FlagOrdProd = e01FlagOrdProd; }
}