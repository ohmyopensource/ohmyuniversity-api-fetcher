package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.laureati;

/**
 * DTO representing a single raw row from the laureati-per-corso CSV files.
 *
 * Column mapping (same indices across all three historical files):
 * index 0: anno (ANNO_LAUREA / AnnoS)
 * index 1: codiceAteneo (COD_Ateneo / AteneoCOD)
 * index 2: nomeAteneo (NomeOperativo / AteneoNOME)
 * index 3: classeNumero (NUMERO / ClasseNUMERO)
 * index 4: corsoNome (NOME_CORSO / CorsoNOME)
 * index 5: sedeC / COMUNE — ignored
 * index 6: sedeP / ID_PROVINCIA_ISTAT — ignored
 * index 7: sesso (ID_SESSO / Sesso)
 * index 8: laureati (laureati / Lau)
 */
public class LaureatiRecord {

  private final int annoLaurea;
  private final String codiceAteneo;
  private final String nomeAteneo;
  private final String classeNumero;
  private final String corsoNome;
  private final String sesso;
  private final int laureati;

  private LaureatiRecord(
      int annoLaurea,
      String codiceAteneo,
      String nomeAteneo,
      String classeNumero,
      String corsoNome,
      String sesso,
      int laureati) {
    this.annoLaurea = annoLaurea;
    this.codiceAteneo = codiceAteneo;
    this.nomeAteneo = nomeAteneo;
    this.classeNumero = classeNumero;
    this.corsoNome = corsoNome;
    this.sesso = sesso;
    this.laureati = laureati;
  }

  /**
   * Parses a CSV row already split by semicolon.
   * Returns null if the row is malformed or unparseable.
   */
  public static LaureatiRecord fromCsvFields(String[] fields) {
    if (fields.length < 9) {
      return null;
    }
    try {
      int anno = Integer.parseInt(fields[0].trim());
      String codiceAteneo = fields[1].trim();
      String nomeAteneo = fields[2].trim();
      String classeNumero = fields[3].trim();
      String corsoNome = fields[4].trim();
      String sesso = fields[7].trim();
      int laureati = Integer.parseInt(fields[8].trim());

      if (codiceAteneo.isBlank() || corsoNome.isBlank()) {
        return null;
      }

      return new LaureatiRecord(anno, codiceAteneo, nomeAteneo,
          classeNumero, corsoNome, sesso, laureati);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public int getAnnoLaurea() { return annoLaurea; }
  public String getCodiceAteneo() { return codiceAteneo; }
  public String getNomeAteneo() { return nomeAteneo; }
  public String getClasseNumero() { return classeNumero; }
  public String getCorsoNome() { return corsoNome; }
  public String getSesso() { return sesso; }
  public int getLaureati() { return laureati; }
}