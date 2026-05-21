package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.immatricolati;

/**
 * DTO representing a single raw row from the enrollments-by-university CSV.
 */
public class ImmatricolatoAteneoRecord {

  private final String anno;
  private final String nomeAteneo;
  private final String codiceAteneo;
  private final String sesso;
  private final int immatricolati;

  public ImmatricolatoAteneoRecord(
      String anno,
      String nomeAteneo,
      String codiceAteneo,
      String sesso,
      int immatricolati) {
    this.anno = anno;
    this.nomeAteneo = nomeAteneo;
    this.codiceAteneo = codiceAteneo;
    this.sesso = sesso;
    this.immatricolati = immatricolati;
  }

  /**
   * Creates a record from an already split CSV row. Returns null if malformed.
   */
  public static ImmatricolatoAteneoRecord fromCsvFields(String[] fields) {
    if (fields.length < 5) {
      return null;
    }
    try {
      String anno = fields[0].trim();
      String nomeAteneo = fields[1].trim();
      String codiceAteneo = fields[2].trim();
      String sesso = fields[3].trim();
      int immatricolati = Integer.parseInt(fields[4].trim());

      if (anno.isBlank() || codiceAteneo.isBlank()) {
        return null;
      }

      return new ImmatricolatoAteneoRecord(anno, nomeAteneo, codiceAteneo, sesso, immatricolati);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public String getAnno() {
    return anno;
  }

  public String getNomeAteneo() {
    return nomeAteneo;
  }

  public String getCodiceAteneo() {
    return codiceAteneo;
  }

  public String getSesso() {
    return sesso;
  }

  public int getImmatricolati() {
    return immatricolati;
  }
}