package org.ohmyopensource.ohmyuniversity.fetcher.job.ordini;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO that maps a single entry from ordini-professionali.json.
 * Jackson deserializes the JSON array into a list of these records.
 */
public class OrdineRecord {

  @JsonProperty("codice")
  private String codice;

  @JsonProperty("nome")
  private String nome;

  @JsonProperty("categoria")
  private String categoria;

  @JsonProperty("organismoNazionale")
  private String organismoNazionale;

  @JsonProperty("sitioUfficiale")
  private String sitoUfficiale;

  @JsonProperty("requisitiLaurea")
  private List<String> requisitiLaurea;

  @JsonProperty("requisitiNote")
  private String requisitiNote;

  @JsonProperty("ministeroVigilanza")
  private String ministeroVigilanza;

  @JsonProperty("aggiornatoAl")
  private String aggiornatoAl;

  public String getCodice() {
    return codice;
  }

  public String getNome() {
    return nome;
  }

  public String getCategoria() {
    return categoria;
  }

  public String getOrganismoNazionale() {
    return organismoNazionale;
  }

  public String getSitoUfficiale() {
    return sitoUfficiale;
  }

  public List<String> getRequisitiLaurea() {
    return requisitiLaurea;
  }

  public String getRequisitiNote() {
    return requisitiNote;
  }

  public String getMinisteroVigilanza() {
    return ministeroVigilanza;
  }

  public String getAggiornatoAl() {
    return aggiornatoAl;
  }
}