package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.iscritti;

/**
 * DTO representing a single raw row from the enrollments-by-program CSV.
 *
 * The {@code sesso} field is "M" or "F". The processor aggregates rows
 * by {@code (year, universityCode, classeNumero, courseName)} by summing M and F
 * and collapsing any multiple campuses of the same program within the same university.
 */
public class IscrittoRecord {

  private final String anno;
  private final String codiceAteneo;
  private final String nomeAteneo;
  private final String classeNumero;
  private final String corsoNome;
  private final String sesso;
  private final int iscritti;

  public IscrittoRecord(
      String anno,
      String codiceAteneo,
      String nomeAteneo,
      String classeNumero,
      String corsoNome,
      String sesso,
      int iscritti) {
    this.anno = anno;
    this.codiceAteneo = codiceAteneo;
    this.nomeAteneo = nomeAteneo;
    this.classeNumero = classeNumero;
    this.corsoNome = corsoNome;
    this.sesso = sesso;
    this.iscritti = iscritti;
  }

  /**
   * Creates an IscrittoRecord from an already split CSV row.
   * Returns null if the row is malformed or contains unparsable data.
   *
   * @param fields array of fields from the CSV row
   * @return IscrittoRecord or null
   */
  public static IscrittoRecord fromCsvFields(String[] fields) {
    if (fields.length < 10) {
      return null;
    }
    try {
      String anno = fields[0].trim();
      String codiceAteneo = fields[1].trim();
      String nomeAteneo = fields[2].trim();
      String classeNumero = fields[3].trim();
      String corsoNome = fields[4].trim();
      // fields[5] = SedeP, fields[6] = SedeC, fields[7] = GruppoCODICE - IGNORED
      String sesso = fields[8].trim();
      int iscritti = Integer.parseInt(fields[9].trim());

      if (anno.isBlank() || codiceAteneo.isBlank() || corsoNome.isBlank()) {
        return null;
      }

      return new IscrittoRecord(anno, codiceAteneo, nomeAteneo,
          classeNumero, corsoNome, sesso, iscritti);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public String getAnno() {
    return anno;
  }

  public String getCodiceAteneo() {
    return codiceAteneo;
  }

  public String getNomeAteneo() {
    return nomeAteneo;
  }

  public String getClasseNumero() {
    return classeNumero;
  }

  public String getCorsoNome() {
    return corsoNome;
  }

  public String getSesso() {
    return sesso;
  }

  public int getIscritti() {
    return iscritti;
  }
}