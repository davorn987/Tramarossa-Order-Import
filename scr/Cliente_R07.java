package clientImport;

import java.text.Normalizer;
import java.util.Arrays;

public class Cliente_R07 {
    private String codiceGriffe;
    private String ragioneSociale;
    private String indirizzoFAT;
    private String capFAT;
    private String cittaFAT;
    private String telFAT;
    private String codiceFiscale;
    private String statoFAT;
    private String pagamento;
    private String provincia;
    private String nomeFAT;
    private String cognomeFAT;
    private String userID;
    private String nomeSPED;
    private String cognomeSPED;
    private String indirizzoSPED;
    private String cittaSPED;
    private String capSPED;
    private String statoSPED;
    private String codiceIVA;
    private String email;

    public Cliente_R07(String CG, String RS, String ICF, String CAPF, String CITF, String TelF,
            String StatF, String P, String Prov, String NF, String CF, String User,
            String NS, String CS, String ICS, String CITS, String CAPS, String StatoS, String CODFIS, String MAIL) {
        setCodiceGriffe(CG);
        setRagioneSociale(RS);
        setIndirizzoFAT(ICF);
        setCapFAT(CAPF);
        setCittaFAT(CITF);
        setTelFAT(TelF);
        setStatoFAT(StatF);
        setPagamento(P);
        setProvincia(Prov);
        setNomeFAT(NF);
        setCognomeFAT(CF);
        setUserID(User);
        setNomeSPED(NS);
        setCognomeSPED(CS);
        setIndirizzoSPED(ICS);
        setCittaSPED(CITS);
        setCapSPED(CAPS);
        setStatoSPED(StatoS);
        setCodiceFiscale(CODFIS);
        // codice IVA: se il chiamante non fornisce esplicitamente, proviamo a derivarlo dallo stato
        setCodiceIVAFromState(StatF);
        setEmail(MAIL);
    }

    public Cliente_R07() {
        setCodiceGriffe("");
        setRagioneSociale("");
        setIndirizzoFAT("");
        setCapFAT("");
        setCittaFAT("");
        setTelFAT("");
        setStatoFAT("");
        setPagamento("");
        setProvincia("");
        setNomeFAT("");
        setCognomeFAT("");
        setUserID("");
        setNomeSPED("");
        setCognomeSPED("");
        setIndirizzoSPED("");
        setCittaSPED("");
        setCapSPED("");
        setStatoSPED("");
        setCodiceFiscale("");
        setCodiceIVA("");
        setEmail("");
    }

    // --- getters (raw values, without embedded quotes) ---
    public String getCodiceGriffe() { return codiceGriffe; }
    public String getRagioneSociale() { return ragioneSociale; }
    public String getIndirizzoFAT() { return indirizzoFAT; }
    public String getCapFAT() { return capFAT; }
    public String getCittaFAT() { return cittaFAT; }
    public String getTelFAT() { return telFAT; }
    public String getStatoFAT() { return statoFAT; }
    public String getPagamento() { return pagamento; }
    public String getProvincia() { return provincia; }
    public String getNomeFAT() { return nomeFAT; }
    public String getCognomeFAT() { return cognomeFAT; }
    public String getUserID() { return userID; }
    public String getUserIDForCompare() { return userID; }
    public String getNomeSPED() { return nomeSPED; }
    public String getCognomeSPED() { return cognomeSPED; }
    public String getIndirizzoSPED() { return indirizzoSPED; }
    public String getCittaSPED() { return cittaSPED; }
    public String getCapSPED() { return capSPED; }
    public String getStatoSPED() { return statoSPED; }
    public String getCodiceFiscale() { return codiceFiscale; }
    public String getCodiceIVA() { return codiceIVA; }
    public String getEmail() { return email; }

    // --- setters with sanitization ---
    public void setCodiceGriffe(String codiceGriffe) {
        this.codiceGriffe = safeTrim(flattenToAsciiNullSafe(codiceGriffe));
    }
    public void setRagioneSociale(String ragioneSociale) {
        this.ragioneSociale = safeTrim(removeQuotes(flattenToAsciiNullSafe(ragioneSociale)));
    }
    public void setIndirizzoFAT(String indirizzoFAT) {
        this.indirizzoFAT = safeTrim(removeQuotes(flattenToAsciiNullSafe(indirizzoFAT)));
    }
    public void setCapFAT(String capFAT) {
        this.capFAT = safeTrim(removeQuotes(flattenToAsciiNullSafe(capFAT)));
    }
    public void setCittaFAT(String cittaFAT) {
        this.cittaFAT = safeTrim(removeQuotes(flattenToAsciiNullSafe(cittaFAT)));
    }
    public void setTelFAT(String telFAT) {
        this.telFAT = safeTrim(removeQuotes(flattenToAsciiNullSafe(telFAT)));
    }
    public void setStatoFAT(String statoFAT) {
        this.statoFAT = safeTrim(removeQuotes(flattenToAsciiNullSafe(statoFAT)));
    }
    public void setPagamento(String pagamento) {
        this.pagamento = metodoPagamento(safeTrim(flattenToAsciiNullSafe(pagamento)));
    }
    public void setProvincia(String provincia) {
        this.provincia = safeTrim(flattenToAsciiNullSafe(provincia));
    }
    public void setNomeFAT(String nomeFAT) {
        this.nomeFAT = safeTrim(removeQuotes(flattenToAsciiNullSafe(nomeFAT)));
    }
    public void setCognomeFAT(String cognomeFAT) {
        this.cognomeFAT = safeTrim(removeQuotes(flattenToAsciiNullSafe(cognomeFAT)));
    }
    public void setUserID(String userID) {
        if (userID == null) {
            this.userID = "";
            return;
        }
        String uid = userID.trim();
        if (uid.contains("@")) {
            this.userID = "Ospite";
        } else {
            this.userID = safeTrim(flattenToAsciiNullSafe(uid));
        }
    }
    public void setNomeSPED(String nomeSPED) {
        this.nomeSPED = safeTrim(removeQuotes(flattenToAsciiNullSafe(nomeSPED)));
    }
    public void setCognomeSPED(String cognomeSPED) {
        this.cognomeSPED = safeTrim(removeQuotes(flattenToAsciiNullSafe(cognomeSPED)));
    }
    public void setIndirizzoSPED(String indirizzoSPED) {
        this.indirizzoSPED = safeTrim(removeQuotes(flattenToAsciiNullSafe(indirizzoSPED)));
    }
    public void setCittaSPED(String cittaSPED) {
        this.cittaSPED = safeTrim(removeQuotes(flattenToAsciiNullSafe(cittaSPED)));
    }
    public void setCapSPED(String capSPED) {
        this.capSPED = safeTrim(removeQuotes(flattenToAsciiNullSafe(capSPED)));
    }
    public void setStatoSPED(String statoSPED) {
        this.statoSPED = safeTrim(removeQuotes(flattenToAsciiNullSafe(statoSPED)));
    }
    public void setCodiceFiscale(String codiceFiscale) {
        if (codiceFiscale == null || codiceFiscale.trim().isEmpty()) {
            this.codiceFiscale = "_";
        } else {
            this.codiceFiscale = safeTrim(flattenToAsciiNullSafe(codiceFiscale));
        }
    }
    public void setCodiceIVA(String codiceIVA) {
        this.codiceIVA = safeTrim(flattenToAsciiNullSafe(codiceIVA));
    }
    // helper: derive codice IVA from Stato se possibile
    public void setCodiceIVAFromState(String stato) {
        String computed = CheckIVA(safeTrim(flattenToAsciiNullSafe(stato)));
        this.codiceIVA = computed;
    }
    public void setEmail(String email) {
        this.email = safeTrim(removeQuotes(email == null ? "" : email));
    }

    // --- utility methods ---
    private String removeQuotes(String s) {
        if (s == null) return "";
        return s.replace("\"", "");
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String metodoPagamento(String PAGAMENTO) {
        if (PAGAMENTO == null) return "NX";
        if (PAGAMENTO.equalsIgnoreCase("PayPal")) {
            return "PP";
        } else {
            return "NX";
        }
    }

    public String getProvinciaForDB() {
        if (provincia == null || provincia.isEmpty()) return " ";
        if (provincia.length() < 2) return provincia;
        return provincia.substring(0, 2);
    }

    @Override
    public String toString() {
        return "Cliente[" + codiceGriffe + ", " + ragioneSociale + ", " + userID + ", " + email + "]";
    }

    public static String flattenToAsciiNullSafe(String string) {
        if (string == null) return "";
        String normalized = Normalizer.normalize(string, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c <= '\u007F') sb.append(c);
        }
        return sb.toString();
    }

    public static String CheckIVA(String STATO) {
        if (STATO == null) return "A50";
        String[] StatiCEE = { "AT", "BE", "BG", "CY", "CZ", "DE", "DK", "EE",
                "EL", "ES", "FI", "FR", "HR", "HU", "IE", "LT",
                "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SK", "IT", "SI" };
        if (Arrays.asList(StatiCEE).contains(STATO)) {
            return STATO;
        } else {
            return "A50";
        }
    }
}