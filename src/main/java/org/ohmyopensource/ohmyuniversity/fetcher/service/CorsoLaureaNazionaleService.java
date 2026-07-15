package org.ohmyopensource.ohmyuniversity.fetcher.service;

import java.util.List;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CorsoLaureaNazionale;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.CorsoLaureaNazionaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for querying {@link CorsoLaureaNazionale} data.
 * <p>
 * Read-only service — all writes happen exclusively via the batch job.
 */
@Service
@Transactional(readOnly = true)
public class CorsoLaureaNazionaleService {

  private final CorsoLaureaNazionaleRepository repository;

  public CorsoLaureaNazionaleService(CorsoLaureaNazionaleRepository repository) {
    this.repository = repository;
  }

  /**
   * Returns all degree programs matching the given degree class, for a given academic year. When
   * {@code annoAccademico} is {@code null}, the most recent academic year present in the dataset is
   * used instead — avoids stacking 15 years of history in a single response.
   *
   * @param classeLaureaCod degree class code (e.g. "LM-32")
   * @param annoAccademico  academic year filter, or {@code null} for latest available
   */
  public List<CorsoLaureaNazionale> findByClasseLaurea(String classeLaureaCod,
      Integer annoAccademico) {
    int anno = annoAccademico != null ? annoAccademico : latestAnno();
    return repository.findByClasseLaureaCodAndAnnoAccademico(classeLaureaCod, anno);
  }

  /**
   * Returns all degree programs of a given type ("L" or "LM"), for a given academic year. Same
   * default-year behavior as {@link #findByClasseLaurea}.
   *
   * @param tipoCorso      "L" or "LM"
   * @param annoAccademico academic year filter, or {@code null} for latest available
   */
  public List<CorsoLaureaNazionale> findByTipoCorso(String tipoCorso, Integer annoAccademico) {
    int anno = annoAccademico != null ? annoAccademico : latestAnno();
    return repository.findByTipoCorsoAndAnnoAccademico(tipoCorso, anno);
  }

  /**
   * Returns magistrali (LM) sharing the same MUR disciplinary group as the given current degree
   * class — a pragmatic "same area" heuristic, not a verified admission requirement. No official
   * national dataset maps eligibility from an L class to specific LM classes (confirmed: admission
   * requirements are set per-course, per-university, in each course's own regolamento didattico).
   * The UI must present this as "programs in your area" with a disclaimer, not as guaranteed
   * eligibility.
   *
   * @param classeLaureaAttuale the student's current degree class code (e.g. "L-31")
   * @param annoAccademico      academic year filter, or {@code null} for latest available
   * @throws IllegalArgumentException if classeLaureaAttuale is not found in the dataset
   */
  public List<CorsoLaureaNazionale> findMagistraliAffini(String classeLaureaAttuale,
      Integer annoAccademico) {
    int anno = annoAccademico != null ? annoAccademico : latestAnno();
    String classeNormalizzata = normalizeClasseLaurea(classeLaureaAttuale);

    CorsoLaureaNazionale sample = repository
        .findFirstByClasseLaureaCodAndAnnoAccademico(classeNormalizzata, anno)
        .orElseThrow(() -> new IllegalArgumentException(
            "CorsoLaureaNazionaleService: unknown classeLaureaAttuale '"
                + classeNormalizzata + "' for anno " + anno));

    return repository.findByTipoCorsoAndGruppoNomeAndAnnoAccademicoOrderByNomeCorsoAsc(
        "LM", sample.getGruppoNome(), anno);
  }

  /**
   * Normalizes free-text degree class input from the user. "l-31", "L31", "31" all resolve to
   * "L-31". A purely numeric input is assumed to be a triennale class (L-), since this feature's
   * entry point is always "my current bachelor's class" — a student wouldn't search from an LM
   * class here.
   *
   * @param input raw user input
   * @return normalized degree class code
   */
  private String normalizeClasseLaurea(String input) {
    String trimmed = input.trim().toUpperCase().replace(" ", "");
    if (trimmed.matches("^\\d+$")) {
      return "L-" + trimmed;
    }
    if (trimmed.matches("^L\\d+$")) {
      return "L-" + trimmed.substring(1);
    }
    return trimmed;
  }

  private int latestAnno() {
    return repository.findLatestAnnoAccademico()
        .orElseThrow(() -> new IllegalStateException(
            "CorsoLaureaNazionaleService: no data available — has the offerta formativa job run?"));
  }
}