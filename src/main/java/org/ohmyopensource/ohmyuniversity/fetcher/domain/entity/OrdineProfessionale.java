package org.ohmyopensource.ohmyuniversity.fetcher.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Represents an Italian professional order (ordine professionale).
 *
 * Data is loaded from a static JSON file maintained in the repository
 * at src/main/resources/data/ordini-professionali.json.
 * The job runs twice a year to refresh the data.
 *
 * The codice field is the natural key used for upsert — if a record
 * with the same codice already exists, it is updated in place.
 */
@Entity
@Table(name = "ordine_professionale")
public class OrdineProfessionale {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
  private UUID id;

  /**
   * Unique business key — e.g. "AVVOCATI", "MEDICI_CHIRURGHI".
   * Used for idempotent upsert during the batch job.
   */
  @NotBlank
  @Column(name = "codice", nullable = false, unique = true)
  private String codice;

  @NotBlank
  @Column(name = "nome", nullable = false)
  private String nome;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "categoria", nullable = false)
  private CategoriaOrdine categoria;

  @NotBlank
  @Column(name = "organismo_nazionale", nullable = false)
  private String organismoNazionale;

  @Column(name = "sito_ufficiale")
  private String sitoUfficiale;

  /**
   * Classi di laurea richieste per l'iscrizione (e.g. "LMG/01", "LM-41").
   * Stored as PostgreSQL text[]. Empty array means no specific degree required.
   */
  @Array(length = 20)
  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "requisiti_laurea", columnDefinition = "text[]")
  private String[] requisitiLaurea = new String[0];

  @Column(name = "requisiti_note", length = 1000)
  private String requisitiNote;

  @Column(name = "ministero_vigilanza")
  private String ministeroVigilanza;

  /**
   * Date of the source data — updated manually in the JSON file
   * when official changes are made.
   */
  @Column(name = "aggiornato_al")
  private LocalDate aggiornatoAl;

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

  public String getCodice() {
    return codice;
  }

  public void setCodice(String codice) {
    this.codice = codice;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public CategoriaOrdine getCategoria() {
    return categoria;
  }

  public void setCategoria(CategoriaOrdine categoria) {
    this.categoria = categoria;
  }

  public String getOrganismoNazionale() {
    return organismoNazionale;
  }

  public void setOrganismoNazionale(String organismoNazionale) {
    this.organismoNazionale = organismoNazionale;
  }

  public String getSitoUfficiale() {
    return sitoUfficiale;
  }

  public void setSitoUfficiale(String sitoUfficiale) {
    this.sitoUfficiale = sitoUfficiale;
  }

  public String[] getRequisitiLaurea() {
    return requisitiLaurea;
  }

  public void setRequisitiLaurea(String[] requisitiLaurea) {
    this.requisitiLaurea = requisitiLaurea;
  }

  public String getRequisitiNote() {
    return requisitiNote;
  }

  public void setRequisitiNote(String requisitiNote) {
    this.requisitiNote = requisitiNote;
  }

  public String getMinisteroVigilanza() {
    return ministeroVigilanza;
  }

  public void setMinisteroVigilanza(String ministeroVigilanza) {
    this.ministeroVigilanza = ministeroVigilanza;
  }

  public LocalDate getAggiornatoAl() {
    return aggiornatoAl;
  }

  public void setAggiornatoAl(LocalDate aggiornatoAl) {
    this.aggiornatoAl = aggiornatoAl;
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