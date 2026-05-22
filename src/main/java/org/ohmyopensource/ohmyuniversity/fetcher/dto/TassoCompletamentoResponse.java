package org.ohmyopensource.ohmyuniversity.fetcher.dto;

/**
 * Response DTO for GET /api/statistiche/tasso-completamento.
 *
 * Represents the estimated completion rate for a course at a university,
 * calculated by comparing enrolled students in a given academic year
 * against graduates from the same course 3 years later.
 *
 * This is a proxy metric — not a precise cohort tracking — but it gives
 * a reliable orientation signal for prospective students.
 */
public class TassoCompletamentoResponse {

  private String codiceAteneo;
  private String nomeAteneo;
  private String classeNumero;
  private String corsoNome;
  private String annoIscrizione;
  private int annoLaureaRiferimento;
  private int iscrittiAnnoBase;
  private int laureatiAnnoRiferimento;
  private double tassoCompletamentoPct;

  public String getCodiceAteneo() { return codiceAteneo; }
  public void setCodiceAteneo(String codiceAteneo) { this.codiceAteneo = codiceAteneo; }

  public String getNomeAteneo() { return nomeAteneo; }
  public void setNomeAteneo(String nomeAteneo) { this.nomeAteneo = nomeAteneo; }

  public String getClasseNumero() { return classeNumero; }
  public void setClasseNumero(String classeNumero) { this.classeNumero = classeNumero; }

  public String getCorsoNome() { return corsoNome; }
  public void setCorsoNome(String corsoNome) { this.corsoNome = corsoNome; }

  public String getAnnoIscrizione() { return annoIscrizione; }
  public void setAnnoIscrizione(String annoIscrizione) { this.annoIscrizione = annoIscrizione; }

  public int getAnnoLaureaRiferimento() { return annoLaureaRiferimento; }
  public void setAnnoLaureaRiferimento(int annoLaureaRiferimento) {
    this.annoLaureaRiferimento = annoLaureaRiferimento;
  }

  public int getIscrittiAnnoBase() { return iscrittiAnnoBase; }
  public void setIscrittiAnnoBase(int iscrittiAnnoBase) {
    this.iscrittiAnnoBase = iscrittiAnnoBase;
  }

  public int getLaureatiAnnoRiferimento() { return laureatiAnnoRiferimento; }
  public void setLaureatiAnnoRiferimento(int laureatiAnnoRiferimento) {
    this.laureatiAnnoRiferimento = laureatiAnnoRiferimento;
  }

  public double getTassoCompletamentoPct() { return tassoCompletamentoPct; }
  public void setTassoCompletamentoPct(double tassoCompletamentoPct) {
    this.tassoCompletamentoPct = tassoCompletamentoPct;
  }
}