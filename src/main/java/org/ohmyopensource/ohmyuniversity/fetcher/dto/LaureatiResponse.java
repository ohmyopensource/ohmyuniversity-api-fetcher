package org.ohmyopensource.ohmyuniversity.fetcher.dto;

/**
 * Response DTO for GET /api/statistiche/laureati.
 */
public class LaureatiResponse {

  private int annoLaurea;
  private String codiceAteneo;
  private String nomeAteneo;
  private String classeNumero;
  private String corsoNome;
  private int totM;
  private int totF;
  private int tot;

  public int getAnnoLaurea() { return annoLaurea; }
  public void setAnnoLaurea(int annoLaurea) { this.annoLaurea = annoLaurea; }

  public String getCodiceAteneo() { return codiceAteneo; }
  public void setCodiceAteneo(String codiceAteneo) { this.codiceAteneo = codiceAteneo; }

  public String getNomeAteneo() { return nomeAteneo; }
  public void setNomeAteneo(String nomeAteneo) { this.nomeAteneo = nomeAteneo; }

  public String getClasseNumero() { return classeNumero; }
  public void setClasseNumero(String classeNumero) { this.classeNumero = classeNumero; }

  public String getCorsoNome() { return corsoNome; }
  public void setCorsoNome(String corsoNome) { this.corsoNome = corsoNome; }

  public int getTotM() { return totM; }
  public void setTotM(int totM) { this.totM = totM; }

  public int getTotF() { return totF; }
  public void setTotF(int totF) { this.totF = totF; }

  public int getTot() { return tot; }
  public void setTot(int tot) { this.tot = tot; }
}