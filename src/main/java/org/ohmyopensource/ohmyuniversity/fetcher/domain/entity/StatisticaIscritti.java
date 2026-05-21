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
 * Enrollments by degree program, university, and degree class.
 *
 * Source: MUR/USTAT — "enrollments" dataset (file 13_iscrittixcorso and historical files).
 * Male/Female values are already aggregated by the processor: each row represents the total
 * number of enrolled students for the combination (year, university, degree class, program).
 *
 * The preReform flag is TRUE when classeNumero is "99999" — the code used
 * by MUR for pre-reform programs (before DM 509/99) with no assigned degree class.
 */
@Entity
@Table(name = "statistica_iscritti")
public class StatisticaIscritti {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
  private UUID id;

  /** Academic year, format "2024/2025". */
  @NotBlank
  @Column(name = "anno", nullable = false, length = 9)
  private String anno;

  /** MUR university numeric code (e.g. "101" for Turin). */
  @NotBlank
  @Column(name = "codice_ateneo", nullable = false, length = 10)
  private String codiceAteneo;

  @NotBlank
  @Column(name = "nome_ateneo", nullable = false)
  private String nomeAteneo;

  /**
   * Degree class code (e.g. "L-31", "LM-18", "LMG/01").
   * "99999" indicates pre-reform programs without an assigned degree class.
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

  /** TRUE if the program is pre-reform (classeNumero = "99999"). */
  @Column(name = "pre_riforma", nullable = false)
  private boolean preRiforma;

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

  public String getAnno() {
    return anno;
  }

  public void setAnno(String anno) {
    this.anno = anno;
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

  public boolean isPreRiforma() {
    return preRiforma;
  }

  public void setPreRiforma(boolean preRiforma) {
    this.preRiforma = preRiforma;
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