package org.ohmyopensource.ohmyuniversity.fetcher.service;

import java.util.List;
import java.util.Optional;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiAteneo;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiClasse;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaIscritti;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaLaureati;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaImmatricolatiAteneoRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaImmatricolatiClasseRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaIscrittiRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaLaureatiRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.TassoCompletamentoResponse;
import org.springframework.stereotype.Service;

/**
 * Service for reading orientation statistics.
 *
 * All methods are read-only — this service never writes to the database.
 * Data is populated exclusively by the batch jobs.
 *
 * The completion rate (tasso di completamento) is computed by joining
 * enrolled students in a given academic year against graduates of the same
 * course 3 years later. This is a proxy metric, not a precise cohort analysis,
 * but it is a reliable signal for prospective students.
 */
@Service
public class StatisticheService {

  private final StatisticaIscrittiRepository iscrittiRepository;
  private final StatisticaLaureatiRepository laureatiRepository;
  private final StatisticaImmatricolatiClasseRepository immatricolatiClasseRepository;
  private final StatisticaImmatricolatiAteneoRepository immatricolatiAteneoRepository;

  public StatisticheService(
      StatisticaIscrittiRepository iscrittiRepository,
      StatisticaLaureatiRepository laureatiRepository,
      StatisticaImmatricolatiClasseRepository immatricolatiClasseRepository,
      StatisticaImmatricolatiAteneoRepository immatricolatiAteneoRepository) {
    this.iscrittiRepository = iscrittiRepository;
    this.laureatiRepository = laureatiRepository;
    this.immatricolatiClasseRepository = immatricolatiClasseRepository;
    this.immatricolatiAteneoRepository = immatricolatiAteneoRepository;
  }

  // ================================
  // Iscritti
  // ================================

  /**
   * Returns the historical series of enrolled students for a given degree class,
   * sorted by academic year ascending.
   */
  public List<StatisticaIscritti> findIscrittiPerClasse(String classeNumero) {
    return iscrittiRepository.findByClasseNumeroOrderByAnnoAsc(classeNumero);
  }

  /**
   * Returns the historical series of enrolled students for a given university,
   * sorted by academic year ascending.
   */
  public List<StatisticaIscritti> findIscrittiPerAteneo(String codiceAteneo) {
    return iscrittiRepository.findByCodiceAteneoOrderByAnnoAsc(codiceAteneo);
  }

  /**
   * Returns enrolled students for a given university and academic year.
   */
  public List<StatisticaIscritti> findIscrittiPerAteneoEAnno(String codiceAteneo, String anno) {
    return iscrittiRepository.findByAnnoAndCodiceAteneo(anno, codiceAteneo);
  }

  /**
   * Returns enrolled students for a given degree class and academic year.
   */
  public List<StatisticaIscritti> findIscrittiPerClasseEAnno(String classeNumero, String anno) {
    return iscrittiRepository.findByAnnoAndClasseNumero(anno, classeNumero);
  }

  /**
   * Searches enrolled students by course name (case-insensitive partial match).
   */
  public List<StatisticaIscritti> searchIscrittiPerCorso(String corsoNome) {
    return iscrittiRepository
        .findByCorsoNomeContainingIgnoreCaseOrderByAnnoDesc(corsoNome);
  }

  // ================================
  // Laureati
  // ================================

  /**
   * Returns the historical series of graduates for a given degree class,
   * sorted by graduation year ascending.
   */
  public List<StatisticaLaureati> findLaureatiPerClasse(String classeNumero) {
    return laureatiRepository.findByClasseNumeroOrderByAnnoLaureaAsc(classeNumero);
  }

  /**
   * Returns the historical series of graduates for a given university,
   * sorted by graduation year ascending.
   */
  public List<StatisticaLaureati> findLaureatiPerAteneo(String codiceAteneo) {
    return laureatiRepository.findByCodiceAteneoOrderByAnnoLaureaAsc(codiceAteneo);
  }

  /**
   * Returns graduates for a given university and graduation year.
   */
  public List<StatisticaLaureati> findLaureatiPerAteneoEAnno(String codiceAteneo, int anno) {
    return laureatiRepository.findByAnnoLaureaAndCodiceAteneo(anno, codiceAteneo);
  }

  /**
   * Searches graduates by course name (case-insensitive partial match).
   */
  public List<StatisticaLaureati> searchLaureatiPerCorso(String corsoNome) {
    return laureatiRepository
        .findByCorsoNomeContainingIgnoreCaseOrderByAnnoLaureaDesc(corsoNome);
  }

  // ================================
  // Immatricolati
  // ================================

  /**
   * Returns the historical series of first-year students for a given degree class
   * (national aggregate), sorted by academic year ascending.
   */
  public List<StatisticaImmatricolatiClasse> findImmatricolatiPerClasse(String classeNumero) {
    return immatricolatiClasseRepository.findByClasseNumeroOrderByAnnoAsc(classeNumero);
  }

  /**
   * Returns first-year students ranked by total for a given academic year.
   */
  public List<StatisticaImmatricolatiClasse> findImmatricolatiClassePerAnno(String anno) {
    return immatricolatiClasseRepository.findByAnnoOrderByTotDesc(anno);
  }

  /**
   * Returns the historical series of first-year students for a given university,
   * sorted by academic year ascending.
   */
  public List<StatisticaImmatricolatiAteneo> findImmatricolatiPerAteneo(String codiceAteneo) {
    return immatricolatiAteneoRepository.findByCodiceAteneoOrderByAnnoAsc(codiceAteneo);
  }

  /**
   * Returns universities ranked by first-year students for a given academic year.
   */
  public List<StatisticaImmatricolatiAteneo> findImmatricolatiAteneoPerAnno(String anno) {
    return immatricolatiAteneoRepository.findByAnnoOrderByTotDesc(anno);
  }

  // ================================
  // Tasso di completamento
  // ================================

  /**
   * Computes the estimated completion rate for all courses of a given degree class,
   * comparing enrolled students in {@code annoIscrizione} against graduates 3 years later.
   *
   * The graduation year is derived from the first year of the academic year string:
   * "2019/2020" → enrolled in 2019 → expected graduation in 2022.
   *
   * @param classeNumero   degree class code, e.g. "L-31"
   * @param annoIscrizione academic year string, e.g. "2019/2020"
   * @return list of completion rate records, sorted by rate descending
   */
  public List<TassoCompletamentoResponse> calcolaTassoCompletamento(
      String classeNumero,
      String annoIscrizione) {

    int annoLaurea = extractFirstYear(annoIscrizione) + 3;

    List<StatisticaIscritti> iscritti =
        iscrittiRepository.findByAnnoAndClasseNumero(annoIscrizione, classeNumero);

    List<StatisticaLaureati> laureati =
        laureatiRepository.findByAnnoLaureaAndClasseNumero(annoLaurea, classeNumero);

    return iscritti.stream()
        .map(i -> {
          Optional<StatisticaLaureati> match = laureati.stream()
              .filter(l -> l.getCodiceAteneo().equals(i.getCodiceAteneo())
                  && l.getCorsoNome().equalsIgnoreCase(i.getCorsoNome()))
              .findFirst();

          TassoCompletamentoResponse r = new TassoCompletamentoResponse();
          r.setCodiceAteneo(i.getCodiceAteneo());
          r.setNomeAteneo(i.getNomeAteneo());
          r.setClasseNumero(i.getClasseNumero());
          r.setCorsoNome(i.getCorsoNome());
          r.setAnnoIscrizione(annoIscrizione);
          r.setAnnoLaureaRiferimento(annoLaurea);
          r.setIscrittiAnnoBase(i.getTot());

          int laureatiTot = match.map(StatisticaLaureati::getTot).orElse(0);
          r.setLaureatiAnnoRiferimento(laureatiTot);

          double tasso = i.getTot() > 0
              ? Math.round((laureatiTot * 1000.0 / i.getTot())) / 10.0
              : 0.0;
          r.setTassoCompletamentoPct(tasso);

          return r;
        })
        .sorted((a, b) ->
            Double.compare(b.getTassoCompletamentoPct(), a.getTassoCompletamentoPct()))
        .toList();
  }

  // ================================
  // Private helpers
  // ================================

  /**
   * Extracts the first calendar year from an academic year string.
   * "2019/2020" → 2019. Throws IllegalArgumentException if format is invalid.
   */
  private int extractFirstYear(String annoIscrizione) {
    try {
      return Integer.parseInt(annoIscrizione.substring(0, 4));
    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
      throw new IllegalArgumentException(
          "Invalid academic year format: '" + annoIscrizione + "'. Expected format: 'YYYY/YYYY'");
    }
  }
}