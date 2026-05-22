package org.ohmyopensource.ohmyuniversity.fetcher.dto;

/**
 * Response DTO for GET /api/statistiche/immatricolati/ateneo.
 */
public class ImmatricolatiAteneoResponse {

  private String anno;
  private String codiceAteneo;
  private String nomeAteneo;
  private int totM;
  private int totF;
  private int tot;

  public String getAnno() { return anno; }
  public void setAnno(String anno) { this.anno = anno; }

  public String getCodiceAteneo() { return codiceAteneo; }
  public void setCodiceAteneo(String codiceAteneo) { this.codiceAteneo = codiceAteneo; }

  public String getNomeAteneo() { return nomeAteneo; }
  public void setNomeAteneo(String nomeAteneo) { this.nomeAteneo = nomeAteneo; }

  public int getTotM() { return totM; }
  public void setTotM(int totM) { this.totM = totM; }

  public int getTotF() { return totF; }
  public void setTotF(int totF) { this.totF = totF; }

  public int getTot() { return tot; }
  public void setTot(int tot) { this.tot = tot; }
}