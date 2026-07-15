package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.offerta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raw record parsed from a single row of the MUR "Offerta formativa 2010-2024" CSV.
 *
 * Column layout confirmed against real file content:
 * ANNO_VALIDITA;NomeOperativo;Area;Gruppo_Nome;NUMERO;DES;NOME_CORSO;PROVINCIA;COMUNE;ACCESSO;DIDATTICA
 *
 * Note: Gruppo_Nome is fixed-width padded with trailing spaces in the source
 * file — always trimmed here. No ateneo code is present in this resource,
 * only the display name (NomeOperativo).
 */
class OffertaFormativaRecord {

  private static final Logger log = LoggerFactory.getLogger(OffertaFormativaRecord.class);

  private static final int COL_ANNO_VALIDITA = 0;
  private static final int COL_NOME_OPERATIVO = 1;
  private static final int COL_AREA = 2;
  private static final int COL_GRUPPO_NOME = 3;
  private static final int COL_NUMERO = 4;
  private static final int COL_DES = 5;
  private static final int COL_NOME_CORSO = 6;
  private static final int COL_PROVINCIA = 7;
  private static final int COL_COMUNE = 8;
  private static final int COL_ACCESSO = 9;
  private static final int COL_DIDATTICA = 10;
  private static final int MIN_COLUMNS = 11;

  private final int annoAccademico;
  private final String ateneoNome;
  private final String area;
  private final String gruppoNome;
  private final String classeLaureaCod;
  private final String classeLaureaDes;
  private final String nomeCorso;
  private final String provincia;
  private final String comune;
  private final String accesso;
  private final String didattica;

  private OffertaFormativaRecord(
      int annoAccademico, String ateneoNome, String area, String gruppoNome,
      String classeLaureaCod, String classeLaureaDes, String nomeCorso,
      String provincia, String comune, String accesso, String didattica) {
    this.annoAccademico = annoAccademico;
    this.ateneoNome = ateneoNome;
    this.area = area;
    this.gruppoNome = gruppoNome;
    this.classeLaureaCod = classeLaureaCod;
    this.classeLaureaDes = classeLaureaDes;
    this.nomeCorso = nomeCorso;
    this.provincia = provincia;
    this.comune = comune;
    this.accesso = accesso;
    this.didattica = didattica;
  }

  /**
   * Parses a single CSV row into a record, or returns {@code null} if the
   * row is malformed (too few columns, unparsable academic year, missing
   * mandatory fields).
   *
   * @param fields row fields as produced by {@code MurCsvReader}
   * @return parsed record, or {@code null} if the row should be skipped
   */
  static OffertaFormativaRecord fromCsvFields(String[] fields) {
    if (fields == null || fields.length < MIN_COLUMNS) {
      return null;
    }
    try {
      int anno = Integer.parseInt(fields[COL_ANNO_VALIDITA].trim());
      String ateneoNome = fields[COL_NOME_OPERATIVO].trim();
      String area = fields[COL_AREA].trim();
      String gruppoNome = fields[COL_GRUPPO_NOME].trim();
      String classeLaureaCod = fields[COL_NUMERO].trim();
      String classeLaureaDes = fields[COL_DES].trim();
      String nomeCorso = fields[COL_NOME_CORSO].trim();
      String provincia = fields[COL_PROVINCIA].trim();
      String comune = fields[COL_COMUNE].trim();
      String accesso = fields[COL_ACCESSO].trim();
      String didattica = fields[COL_DIDATTICA].trim();

      if (ateneoNome.isEmpty() || classeLaureaCod.isEmpty() || nomeCorso.isEmpty()) {
        return null;
      }

      return new OffertaFormativaRecord(
          anno, ateneoNome, area, gruppoNome, classeLaureaCod, classeLaureaDes,
          nomeCorso, provincia, comune, accesso, didattica);
    } catch (NumberFormatException e) {
      log.warn("OffertaFormativaRecord: skipping malformed row: {}", (Object) fields);
      return null;
    }
  }

  /**
   * Derives the program type from the degree class code prefix.
   * "L-..." → "L" (triennale), "LM-..." → "LM" (magistrale, includes ciclo unico).
   * No distinct source column carries this information.
   */
  String deriveTipoCorso() {
    String cod = classeLaureaCod.toUpperCase();
    if (cod.startsWith("LM")) {
      return "LM";
    }
    if (cod.startsWith("L")) {
      return "L";
    }
    return "ALTRO";
  }

  int getAnnoAccademico() {
    return annoAccademico;
  }

  String getAteneoNome() {
    return ateneoNome;
  }

  String getArea() {
    return area;
  }

  String getGruppoNome() {
    return gruppoNome;
  }

  String getClasseLaureaCod() {
    return classeLaureaCod;
  }

  String getClasseLaureaDes() {
    return classeLaureaDes;
  }

  String getNomeCorso() {
    return nomeCorso;
  }

  String getProvincia() {
    return provincia;
  }

  String getComune() {
    return comune;
  }

  String getAccesso() {
    return accesso;
  }

  String getDidattica() {
    return didattica;
  }
}