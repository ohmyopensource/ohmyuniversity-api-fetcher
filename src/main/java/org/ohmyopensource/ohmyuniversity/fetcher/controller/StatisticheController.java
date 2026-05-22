package org.ohmyopensource.ohmyuniversity.fetcher.controller;

import java.util.List;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.ImmatricolatiAteneoResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.ImmatricolatiClasseResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.IscrittiResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.LaureatiResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.TassoCompletamentoResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.service.StatisticheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for orientation statistics.
 *
 * All endpoints are read-only and publicly accessible — no authentication required.
 * This data is intended for prospective students (guest users) exploring university options.
 */
@RestController
@RequestMapping("/api/statistiche")
public class StatisticheController {

  private final StatisticheService statisticheService;

  public StatisticheController(StatisticheService statisticheService) {
    this.statisticheService = statisticheService;
  }

  // ================================
  // Iscritti
  // ================================

  /**
   * Returns enrolled students filtered by one of: classe, ateneo, or corso (partial match).
   * If anno is also provided alongside classe or ateneo, it is used as an additional filter.
   * At least one of classe, ateneo, or corso must be specified.
   *
   * @param classe  degree class code, e.g. "L-31"
   * @param ateneo  university code, e.g. "101"
   * @param corso   partial course name (case-insensitive)
   * @param anno    academic year, e.g. "2023/2024"
   * @return 200 with list of iscritti records, 400 if no filter specified
   */
  @GetMapping("/iscritti")
  public ResponseEntity<List<IscrittiResponse>> getIscritti(
      @RequestParam(required = false) String classe,
      @RequestParam(required = false) String ateneo,
      @RequestParam(required = false) String corso,
      @RequestParam(required = false) String anno) {

    if (classe == null && ateneo == null && corso == null) {
      return ResponseEntity.badRequest().build();
    }

    List<IscrittiResponse> result;

    if (classe != null && anno != null) {
      result = statisticheService.findIscrittiPerClasseEAnno(classe, anno)
          .stream().map(this::toIscrittiResponse).toList();
    } else if (ateneo != null && anno != null) {
      result = statisticheService.findIscrittiPerAteneoEAnno(ateneo, anno)
          .stream().map(this::toIscrittiResponse).toList();
    } else if (classe != null) {
      result = statisticheService.findIscrittiPerClasse(classe)
          .stream().map(this::toIscrittiResponse).toList();
    } else if (ateneo != null) {
      result = statisticheService.findIscrittiPerAteneo(ateneo)
          .stream().map(this::toIscrittiResponse).toList();
    } else {
      result = statisticheService.searchIscrittiPerCorso(corso)
          .stream().map(this::toIscrittiResponse).toList();
    }

    return ResponseEntity.ok(result);
  }

  // ================================
  // Laureati
  // ================================

  /**
   * Returns graduates filtered by one of: classe, ateneo, or corso (partial match).
   * If annoLaurea is also provided alongside classe or ateneo, it is used as
   * an additional filter.
   * At least one of classe, ateneo, or corso must be specified.
   *
   * @param classe     degree class code, e.g. "L-31"
   * @param ateneo     university code, e.g. "101"
   * @param corso      partial course name (case-insensitive)
   * @param annoLaurea calendar graduation year, e.g. 2023
   * @return 200 with list of laureati records, 400 if no filter specified
   */
  @GetMapping("/laureati")
  public ResponseEntity<List<LaureatiResponse>> getLaureati(
      @RequestParam(required = false) String classe,
      @RequestParam(required = false) String ateneo,
      @RequestParam(required = false) String corso,
      @RequestParam(required = false) Integer annoLaurea) {

    if (classe == null && ateneo == null && corso == null) {
      return ResponseEntity.badRequest().build();
    }

    List<LaureatiResponse> result;

    if (ateneo != null && annoLaurea != null) {
      result = statisticheService.findLaureatiPerAteneoEAnno(ateneo, annoLaurea)
          .stream().map(this::toLaureatiResponse).toList();
    } else if (classe != null) {
      result = statisticheService.findLaureatiPerClasse(classe)
          .stream().map(this::toLaureatiResponse).toList();
    } else if (ateneo != null) {
      result = statisticheService.findLaureatiPerAteneo(ateneo)
          .stream().map(this::toLaureatiResponse).toList();
    } else {
      result = statisticheService.searchLaureatiPerCorso(corso)
          .stream().map(this::toLaureatiResponse).toList();
    }

    return ResponseEntity.ok(result);
  }

  // ================================
  // Immatricolati
  // ================================

  /**
   * Returns first-year students by degree class.
   * If classe is provided, returns the historical series for that class.
   * If anno is provided without classe, returns all classes ranked by total for that year.
   * At least one of classe or anno must be specified.
   *
   * @param classe degree class code, e.g. "L-31"
   * @param anno   academic year, e.g. "2023/2024"
   * @return 200 with list, 400 if no filter specified
   */
  @GetMapping("/immatricolati/classe")
  public ResponseEntity<List<ImmatricolatiClasseResponse>> getImmatricolatiClasse(
      @RequestParam(required = false) String classe,
      @RequestParam(required = false) String anno) {

    if (classe == null && anno == null) {
      return ResponseEntity.badRequest().build();
    }

    List<ImmatricolatiClasseResponse> result;

    if (classe != null) {
      result = statisticheService.findImmatricolatiPerClasse(classe)
          .stream().map(this::toImmatricolatiClasseResponse).toList();
    } else {
      result = statisticheService.findImmatricolatiClassePerAnno(anno)
          .stream().map(this::toImmatricolatiClasseResponse).toList();
    }

    return ResponseEntity.ok(result);
  }

  /**
   * Returns first-year students by university.
   * If ateneo is provided, returns the historical series for that university.
   * If anno is provided without ateneo, returns all universities ranked by total for that year.
   * At least one of ateneo or anno must be specified.
   *
   * @param ateneo university code, e.g. "101"
   * @param anno   academic year, e.g. "2023/2024"
   * @return 200 with list, 400 if no filter specified
   */
  @GetMapping("/immatricolati/ateneo")
  public ResponseEntity<List<ImmatricolatiAteneoResponse>> getImmatricolatiAteneo(
      @RequestParam(required = false) String ateneo,
      @RequestParam(required = false) String anno) {

    if (ateneo == null && anno == null) {
      return ResponseEntity.badRequest().build();
    }

    List<ImmatricolatiAteneoResponse> result;

    if (ateneo != null) {
      result = statisticheService.findImmatricolatiPerAteneo(ateneo)
          .stream().map(this::toImmatricolatiAteneoResponse).toList();
    } else {
      result = statisticheService.findImmatricolatiAteneoPerAnno(anno)
          .stream().map(this::toImmatricolatiAteneoResponse).toList();
    }

    return ResponseEntity.ok(result);
  }

  // ================================
  // Tasso di completamento
  // ================================

  /**
   * Returns the estimated completion rate for all courses of a given degree class,
   * comparing enrolled students in the given academic year against graduates 3 years later.
   * Results are sorted by completion rate descending.
   *
   * @param classe         degree class code, e.g. "L-31" (required)
   * @param annoIscrizione academic year, e.g. "2019/2020" (required)
   * @return 200 with ranked list, 400 if params are missing or malformed
   */
  @GetMapping("/tasso-completamento")
  public ResponseEntity<List<TassoCompletamentoResponse>> getTassoCompletamento(
      @RequestParam String classe,
      @RequestParam String annoIscrizione) {

    try {
      List<TassoCompletamentoResponse> result =
          statisticheService.calcolaTassoCompletamento(classe, annoIscrizione);
      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // ================================
  // Private mapping helpers
  // ================================

  private IscrittiResponse toIscrittiResponse(
      org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaIscritti e) {
    IscrittiResponse r = new IscrittiResponse();
    r.setAnno(e.getAnno());
    r.setCodiceAteneo(e.getCodiceAteneo());
    r.setNomeAteneo(e.getNomeAteneo());
    r.setClasseNumero(e.getClasseNumero());
    r.setCorsoNome(e.getCorsoNome());
    r.setTotM(e.getTotM());
    r.setTotF(e.getTotF());
    r.setTot(e.getTot());
    r.setPreRiforma(e.isPreRiforma());
    return r;
  }

  private LaureatiResponse toLaureatiResponse(
      org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaLaureati e) {
    LaureatiResponse r = new LaureatiResponse();
    r.setAnnoLaurea(e.getAnnoLaurea());
    r.setCodiceAteneo(e.getCodiceAteneo());
    r.setNomeAteneo(e.getNomeAteneo());
    r.setClasseNumero(e.getClasseNumero());
    r.setCorsoNome(e.getCorsoNome());
    r.setTotM(e.getTotM());
    r.setTotF(e.getTotF());
    r.setTot(e.getTot());
    return r;
  }

  private ImmatricolatiClasseResponse toImmatricolatiClasseResponse(
      org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiClasse e) {
    ImmatricolatiClasseResponse r = new ImmatricolatiClasseResponse();
    r.setAnno(e.getAnno());
    r.setClasseNumero(e.getClasseNumero());
    r.setClasseNome(e.getClasseNome());
    r.setTotM(e.getTotM());
    r.setTotF(e.getTotF());
    r.setTot(e.getTot());
    return r;
  }

  private ImmatricolatiAteneoResponse toImmatricolatiAteneoResponse(
      org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiAteneo e) {
    ImmatricolatiAteneoResponse r = new ImmatricolatiAteneoResponse();
    r.setAnno(e.getAnno());
    r.setCodiceAteneo(e.getCodiceAteneo());
    r.setNomeAteneo(e.getNomeAteneo());
    r.setTotM(e.getTotM());
    r.setTotF(e.getTotF());
    r.setTot(e.getTot());
    return r;
  }
}