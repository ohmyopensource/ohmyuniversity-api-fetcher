package org.ohmyopensource.ohmyuniversity.fetcher.dto;

import java.time.LocalDate;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CategoriaOrdine;

/**
 * Response DTO for GET /api/ordini-professionali.
 *
 * Exposes the data needed by the frontend to show students
 * which professional orders they can join based on their degree.
 */
public class OrdineProfessionaleResponse {

  private UUID id;
  private String codice;
  private String nome;
  private CategoriaOrdine categoria;
  private String organismoNazionale;
  private String sitoUfficiale;
  private String[] requisitiLaurea;
  private String requisitiNote;
  private String ministeroVigilanza;
  private LocalDate aggiornatoAl;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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
}