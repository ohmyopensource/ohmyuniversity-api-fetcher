package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.iscritti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates raw enrollment CSV records by the key
 * {@code (year, universityCode, classCode, programName)}.
 *
 * The CSV contains one row per gender (M/F) and potentially multiple rows
 * for the same program if it is offered in different campuses within the same university.
 * This aggregator collapses all rows by summing the counts, producing
 * a single row with {@code totM}, {@code totF}, and {@code tot}.
 */
public class IscrittixCorsoAggregator {

  private IscrittixCorsoAggregator() {
  }

  /**
   * Aggregates a list of raw records into a list of aggregated records.
   *
   * @param records list of raw {@link IscrittoRecord} entries (one row per gender/campus)
   * @return list of {@link AggregatedIscrittoRecord}, one for each upsert key
   */
  public static List<AggregatedIscrittoRecord> aggregate(List<IscrittoRecord> records) {
    Map<String, AggregatedIscrittoRecord> map = new HashMap<>();

    for (IscrittoRecord r : records) {
      String key = r.getAnno() + "|" + r.getCodiceAteneo() + "|"
          + r.getClasseNumero() + "|" + r.getCorsoNome();

      AggregatedIscrittoRecord agg = map.computeIfAbsent(key, k ->
          new AggregatedIscrittoRecord(
              r.getAnno(),
              r.getCodiceAteneo(),
              r.getNomeAteneo(),
              r.getClasseNumero(),
              r.getCorsoNome()
          ));

      if ("M".equalsIgnoreCase(r.getSesso())) {
        agg.addM(r.getIscritti());
      } else if ("F".equalsIgnoreCase(r.getSesso())) {
        agg.addF(r.getIscritti());
      }
    }

    return new ArrayList<>(map.values());
  }

  /**
   * Aggregated record — one row per key (year, university, class, program).
   */
  public static class AggregatedIscrittoRecord {

    private final String anno;
    private final String codiceAteneo;
    private final String nomeAteneo;
    private final String classeNumero;
    private final String corsoNome;
    private int totM = 0;
    private int totF = 0;

    public AggregatedIscrittoRecord(
        String anno,
        String codiceAteneo,
        String nomeAteneo,
        String classeNumero,
        String corsoNome) {
      this.anno = anno;
      this.codiceAteneo = codiceAteneo;
      this.nomeAteneo = nomeAteneo;
      this.classeNumero = classeNumero;
      this.corsoNome = corsoNome;
    }

    public void addM(int count) {
      totM += count;
    }

    public void addF(int count) {
      totF += count;
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

    public int getTotM() {
      return totM;
    }

    public int getTotF() {
      return totF;
    }

    public int getTot() {
      return totM + totF;
    }
  }
}