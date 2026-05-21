package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.immatricolati;

/**
 * DTO representing a single raw row from the enrollments-by-degree-class CSV.
 */
public class ImmatricolatoClasseRecord {

  private final String anno;
  private final String classeNumero;
  private final String classeNome;
  private final String sesso;
  private final int immatricolati;

  public ImmatricolatoClasseRecord(
      String anno,
      String classeNumero,
      String classeNome,
      String sesso,
      int immatricolati) {
    this.anno = anno;
    this.classeNumero = classeNumero;
    this.classeNome = classeNome;
    this.sesso = sesso;
    this.immatricolati = immatricolati;
  }

  /**
   * Creates a record from an already split CSV row. Returns null if malformed.
   */
  public static ImmatricolatoClasseRecord fromCsvFields(String[] fields) {
    if (fields.length < 5) {
      return null;
    }
    try {
      String anno = fields[0].trim();
      String classeNumero = fields[1].trim();
      String classeNome = fields[2].trim();
      String sesso = fields[3].trim();
      int immatricolati = Integer.parseInt(fields[4].trim());

      if (anno.isBlank() || classeNumero.isBlank()) {
        return null;
      }

      return new ImmatricolatoClasseRecord(anno, classeNumero, classeNome, sesso, immatricolati);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public String getAnno() {
    return anno;
  }

  public String getClasseNumero() {
    return classeNumero;
  }

  public String getClasseNome() {
    return classeNome;
  }

  public String getSesso() {
    return sesso;
  }

  public int getImmatricolati() {
    return immatricolati;
  }
}