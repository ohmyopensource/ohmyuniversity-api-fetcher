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
 * Enrollments by university — aggregated at university level, without degree-class detail.
 *
 * Source: MUR/USTAT — "enrollments" dataset (file 02_immatricolatixateneo).
 * Historical series available since 1998/1999.
 *
 * Useful for showing students the size and growth trend
 * of each university (e.g. "Politecnico di Milano has doubled its enrollments
 * over the last 15 years").
 */
@Entity
@Table(name = "statistica_immatricolati_ateneo")
public class StatisticaImmatricolatiAteneo {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
  private UUID id;

  /** Academic year, format "2025/2026". */
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