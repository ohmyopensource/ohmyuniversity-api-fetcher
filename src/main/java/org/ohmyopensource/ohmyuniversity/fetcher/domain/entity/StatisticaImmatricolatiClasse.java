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
 * Enrollments by degree class — national aggregate.
 *
 * Source: MUR/USTAT — "enrollments" dataset (file 17_immatricolatixclasse).
 * It does not include university-level detail: it represents the national total
 * of new enrollments for each degree class and academic year.
 * Historical series available since 1998/1999.
 *
 * Useful for showing students the national trend of interest in a
 * specific degree class (e.g. "over the last 10 years, L-31 Computer Science
 * has grown by 40%").
 */
@Entity
@Table(name = "statistica_immatricolati_classe")
public class StatisticaImmatricolatiClasse {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
  private UUID id;

  /** Academic year, format "2025/2026". */
  @NotBlank
  @Column(name = "anno", nullable = false, length = 9)
  private String anno;

  /** Degree class code (e.g. "L-31", "LMG/01", "L/DS"). */
  @NotBlank
  @Column(name = "classe_numero", nullable = false, length = 20)
  private String classeNumero;

  @NotBlank
  @Column(name = "classe_nome", nullable = false)
  private String classeNome;

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

  public String getClasseNumero() {
    return classeNumero;
  }

  public void setClasseNumero(String classeNumero) {
    this.classeNumero = classeNumero;
  }

  public String getClasseNome() {
    return classeNome;
  }

  public void setClasseNome(String classeNome) {
    this.classeNome = classeNome;
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