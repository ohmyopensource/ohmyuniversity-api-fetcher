package org.ohmyopensource.ohmyuniversity.fetcher.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * Graduates per course, university and degree class.
 *
 * Source: MUR/USTAT — dataset "laureati" (files 12a/12b/12c_laureati_corso).
 * M/F values are already aggregated by the processor: each row represents
 * the total graduates for the combination (annoLaurea, ateneo, classe, corso).
 *
 * Note: annoLaurea is a calendar year (e.g. 2024), not an academic year
 * like "2024/2025". This matches the MUR source format.
 */
@Entity
@Table(name = "statistica_laureati")
public class StatisticaLaureati {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
  private UUID id;

  /** Calendar year of graduation, e.g. 2024. */
  @Column(name = "anno_laurea", nullable = false)
  private int annoLaurea;

  /** Numeric MUR university code, e.g. "101" for Torino. */
  @NotBlank
  @Column(name = "codice_ateneo", nullable = false, length = 10)
  private String codiceAteneo;

  @NotBlank
  @Column(name = "nome_ateneo", nullable = false)
  private String nomeAteneo;

  /**
   * Degree class code (e.g. "L-31", "LM-18", "LMG/01").
   * Pre-reform codes are numeric strings (e.g. "13", "37").
   */
  @NotBlank
  @Column(name = "classe_numero", nullable = false, length = 20)
  private String classeNumero;

  @NotBlank
  @Column(name = "corso_nome", nullable = false)
  private String corsoNome;

  @Column(name = "tot_m", nullable = false)
  private int totM;

  @Column(name = "tot_f", nullable = false)
  private int totF;

  @Column(name = "tot", nullable = false)
  private int tot;

  @NotNull
  @Column(name = "fetched_at", nullable = false)
  private Instant fetchedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public int getAnnoLaurea() {
    return annoLaurea;
  }

  public void setAnnoLaurea(int annoLaurea) {
    this.annoLaurea = annoLaurea;
  }

  public String getCodiceAteneo() {
    return codiceAteneo;
  }

  public void setCodiceAteneo(String codiceAteneo) {
    this.codiceAteneo = codiceAteneo;
  }

  public String getNomeAteneo() {
    return nomeAteneo;
  }

  public void setNomeAteneo(String nomeAteneo) {
    this.nomeAteneo = nomeAteneo;
  }

  public String getClasseNumero() {
    return classeNumero;
  }

  public void setClasseNumero(String classeNumero) {
    this.classeNumero = classeNumero;
  }

  public String getCorsoNome() {
    return corsoNome;
  }

  public void setCorsoNome(String corsoNome) {
    this.corsoNome = corsoNome;
  }

  public int getTotM() {
    return totM;
  }

  public void setTotM(int totM) {
    this.totM = totM;
  }

  public int getTotF() {
    return totF;
  }

  public void setTotF(int totF) {
    this.totF = totF;
  }

  public int getTot() {
    return tot;
  }

  public void setTot(int tot) {
    this.tot = tot;
  }

  public Instant getFetchedAt() {
    return fetchedAt;
  }

  public void setFetchedAt(Instant fetchedAt) {
    this.fetchedAt = fetchedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}