package org.ohmyopensource.ohmyuniversity.fetcher.dto;

/**
 * Response DTO for GET /api/statistiche/immatricolati/classe.
 */
public class ImmatricolatiClasseResponse {

  private String anno;
  private String classeNumero;
  private String classeNome;
  private int totM;
  private int totF;
  private int tot;

  public String getAnno() { return anno; }
  public void setAnno(String anno) { this.anno = anno; }

  public String getClasseNumero() { return classeNumero; }
  public void setClasseNumero(String classeNumero) { this.classeNumero = classeNumero; }

  public String getClasseNome() { return classeNome; }
  public void setClasseNome(String classeNome) { this.classeNome = classeNome; }

  public int getTotM() { return totM; }
  public void setTotM(int totM) { this.totM = totM; }

  public int getTotF() { return totF; }
  public void setTotF(int totF) { this.totF = totF; }

  public int getTot() { return tot; }
  public void setTot(int tot) { this.tot = tot; }
}