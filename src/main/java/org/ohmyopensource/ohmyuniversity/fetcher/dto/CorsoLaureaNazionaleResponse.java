package org.ohmyopensource.ohmyuniversity.fetcher.dto;

import java.util.UUID;

/**
 * Response DTO for a single national degree program entry.
 */
public class CorsoLaureaNazionaleResponse {

  private UUID id;
  private Integer annoAccademico;
  private String ateneoNome;
  private String area;
  private String gruppoNome;
  private String classeLaureaCod;
  private String classeLaureaDes;
  private String nomeCorso;
  private String tipoCorso;
  private String provincia;
  private String comune;
  private String accesso;
  private String didattica;
  private String linkRicerca;

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

  public String getLinkRicerca() {
    return linkRicerca;
  }

  public void setLinkRicerca(String linkRicerca) {
    this.linkRicerca = linkRicerca;
  }
}