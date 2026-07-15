package org.ohmyopensource.ohmyuniversity.fetcher.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a single degree program active in a given academic year.
 *
 * <p>Imported from the MUR "Offerta formativa 2010-2024" open dataset
 * ({@code https://dati-ustat.mur.gov.it/dataset/metadati}). National reference data, not tied to
 * any specific student — used to compute which degree programs are a plausible next step from a
 * student's current degree class.
 *
 * <p>No numeric university code is available in this resource — only the
 * display name ({@code ateneoNome}).
 *
 * <p>Maps to the {@code corso_laurea_nazionale} table created by migration {@code V8}.
 */
@Entity
@Table(name = "corso_laurea_nazionale")
public class CorsoLaureaNazionale {

  /**
   * Internal UUID primary key.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;

  /**
   * Academic year the program was active in (source column ANNO_VALIDITA).
   */
  @Column(name = "anno_accademico", nullable = false)
  private Integer annoAccademico;

  /**
   * University/campus display name (source column NomeOperativo).
   */
  @Column(name = "ateneo_nome", nullable = false, length = 255)
  private String ateneoNome;

  /**
   * Broad disciplinary area (source column Area).
   */
  @Column(name = "area", length = 255)
  private String area;

  /**
   * Disciplinary group within the area (source column Gruppo_Nome, trimmed).
   */
  @Column(name = "gruppo_nome", columnDefinition = "TEXT")
  private String gruppoNome;

  /**
   * Degree class code (source column NUMERO, e.g. L-1, LM-12).
   */
  @Column(name = "classe_laurea_cod", nullable = false, length = 20)
  private String classeLaureaCod;

  /**
   * Degree class description (source column DES).
   */
  @Column(name = "classe_laurea_des", length = 255)
  private String classeLaureaDes;

  /**
   * Official degree program name (source column NOME_CORSO).
   */
  @Column(name = "nome_corso", nullable = false, length = 255)
  private String nomeCorso;

  /**
   * Derived from classe_laurea_cod prefix (L = triennale, LM = magistrale).
   */
  @Column(name = "tipo_corso", nullable = false, length = 20)
  private String tipoCorso;

  /**
   * Province of the campus (source column PROVINCIA).
   */
  @Column(name = "provincia", length = 100)
  private String provincia;

  /**
   * Municipality of the campus (source column COMUNE).
   */
  @Column(name = "comune", length = 100)
  private String comune;

  /**
   * Admission type (source column ACCESSO, e.g. "accesso libero", "locale").
   */
  @Column(name = "accesso", length = 50)
  private String accesso;

  /**
   * Teaching mode (source column DIDATTICA, e.g. "in presenza", "mista", "a distanza").
   */
  @Column(name = "didattica", length = 50)
  private String didattica;

  /**
   * Timestamp of the last successful upsert by the batch job.
   */
  @Column(name = "fetched_at", nullable = false)
  private Instant fetchedAt;

  /**
   * Row creation timestamp, set once on insert.
   */
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /**
   * Row last-update timestamp, refreshed on every upsert.
   */
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  // ============ Getters | Setters ============

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Integer getAnnoAccademico() {
    return annoAccademico;
  }

  public void setAnnoAccademico(Integer annoAccademico) {
    this.annoAccademico = annoAccademico;
  }

  public String getAteneoNome() {
    return ateneoNome;
  }

  public void setAteneoNome(String ateneoNome) {
    this.ateneoNome = ateneoNome;
  }

  public String getArea() {
    return area;
  }

  public void setArea(String area) {
    this.area = area;
  }

  public String getGruppoNome() {
    return gruppoNome;
  }

  public void setGruppoNome(String gruppoNome) {
    this.gruppoNome = gruppoNome;
  }

  public String getClasseLaureaCod() {
    return classeLaureaCod;
  }

  public void setClasseLaureaCod(String classeLaureaCod) {
    this.classeLaureaCod = classeLaureaCod;
  }

  public String getClasseLaureaDes() {
    return classeLaureaDes;
  }

  public void setClasseLaureaDes(String classeLaureaDes) {
    this.classeLaureaDes = classeLaureaDes;
  }

  public String getNomeCorso() {
    return nomeCorso;
  }

  public void setNomeCorso(String nomeCorso) {
    this.nomeCorso = nomeCorso;
  }

  public String getTipoCorso() {
    return tipoCorso;
  }

  public void setTipoCorso(String tipoCorso) {
    this.tipoCorso = tipoCorso;
  }

  public String getProvincia() {
    return provincia;
  }

  public void setProvincia(String provincia) {
    this.provincia = provincia;
  }

  public String getComune() {
    return comune;
  }

  public void setComune(String comune) {
    this.comune = comune;
  }

  public String getAccesso() {
    return accesso;
  }

  public void setAccesso(String accesso) {
    this.accesso = accesso;
  }

  public String getDidattica() {
    return didattica;
  }

  public void setDidattica(String didattica) {
    this.didattica = didattica;
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

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}