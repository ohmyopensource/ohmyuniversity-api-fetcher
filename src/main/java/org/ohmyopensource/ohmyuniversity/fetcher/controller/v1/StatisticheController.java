package org.ohmyopensource.ohmyuniversity.fetcher.controller.v1;

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
 * REST controller exposing orientation statistics for prospective students.
 *
 * <p>All endpoints are read-only and publicly accessible — no authentication is required.
 * Data is populated exclusively by the MUR batch jobs and is intended for guest users
 * exploring university options.
 */
@RestController
@RequestMapping("/api/statistiche")
public class StatisticheController {

  private final StatisticheService statisticheService;

  // ============ Constructor ============

  /**
   * Creates the controller with the required service dependency.
   *
   * @param statisticheService service handling orientation statistics query logic
   */
  public StatisticheController(StatisticheService statisticheService) {
    this.statisticheService = statisticheService;
  }

  // ============ Class Methods ============

  /**
   * Returns enrolled students filtered by degree class, university code, or course name.
   *
   * <p>When {@code anno} is provided alongside {@code classe} or {@code ateneo}, it is used
   * as an additional filter. When {@code corso} is provided it performs a case-insensitive
   * partial match on the course name. At least one of {@code classe}, {@code ateneo}, or
   * {@code corso} must be specified.
   *
   * @param classe  optional degree class code filter (e.g. {@code L-31})
   * @param ateneo  optional university code filter (e.g. {@code 101})
   * @param corso   optional partial course name filter (case-insensitive)
   * @param anno    optional academic year filter (e.g. {@code 2023/2024}),
   *                applied only when {@code classe} or {@code ateneo} is also present
   * @return {@code 200 OK} with the list of matching records,
   *         or {@code 400 Bad Request} if no filter is specified
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

  /**
   * Returns graduates filtered by degree class, university code, or course name.
   *
   * <p>When {@code annoLaurea} is provided alongside {@code ateneo}, it is used as an
   * additional filter. When {@code corso} is provided it performs a case-insensitive partial
   * match on the course name. At least one of {@code classe}, {@code ateneo}, or
   * {@code corso} must be specified.
   *
   * @param classe     optional degree class code filter (e.g. {@code L-31})
   * @param ateneo     optional university code filter (e.g. {@code 101})
   * @param corso      optional partial course name filter (case-insensitive)
   * @param annoLaurea optional calendar graduation year filter (e.g. {@code 2023}),
   *                   applied only when {@code ateneo} is also present
   * @return {@code 200 OK} with the list of matching records,
   *         or {@code 400 Bad Request} if no filter is specified
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

  /**
   * Returns first-year enrolled students aggregated by degree class.
   *
   * <p>When {@code classe} is provided, returns the full historical series for that class.
   * When only {@code anno} is provided, returns all degree classes ranked by total
   * first-year students for that academic year. At least one of the two parameters
   * must be specified.
   *
   * @param classe optional degree class code filter (e.g. {@code L-31})
   * @param anno   optional academic year filter (e.g. {@code 2023/2024})
   * @return {@code 200 OK} with the list of matching records,
   *         or {@code 400 Bad Request} if no filter is specified
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
   * Returns first-year enrolled students aggregated by university.
   *
   * <p>When {@code ateneo} is provided, returns the full historical series for that
   * university. When only {@code anno} is provided, returns all universities ranked by
   * total first-year students for that academic year. At least one of the two parameters
   * must be specified.
   *
   * @param ateneo optional university code filter (e.g. {@code 101})
   * @param anno   optional academic year filter (e.g. {@code 2023/2024})
   * @return {@code 200 OK} with the list of matching records,
   *         or {@code 400 Bad Request} if no filter is specified
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

  /**
   * Returns the estimated completion rate for all courses of a given degree class.
   *
   * <p>Compares enrolled students in the given academic year against graduates three years
   * later. Results are sorted by completion rate descending. Both parameters are required.
   *
   * @param classe         degree class code (e.g. {@code L-31})
   * @param annoIscrizione academic year of enrolment (e.g. {@code 2019/2020})
   * @return {@code 200 OK} with the ranked list,
   *         or {@code 400 Bad Request} if parameters are missing or malformed
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

  /**
   * Maps a {@link org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaIscritti}
   * entity to an {@link IscrittiResponse} DTO.
   *
   * @param e the source entity
   * @return the populated response DTO
   */
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

  /**
   * Maps a
   * {@link org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaLaureati}
   * entity to a {@link LaureatiResponse} DTO.
   *
   * @param e the source entity
   * @return the populated response DTO
   */
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

  /**
   * Maps a
   * {@link org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiClasse}
   * entity to an {@link ImmatricolatiClasseResponse} DTO.
   *
   * @param e the source entity
   * @return the populated response DTO
   */
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

  /**
   * Maps a
   * {@link org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiAteneo}
   * entity to an {@link ImmatricolatiAteneoResponse} DTO.
   *
   * @param e the source entity
   * @return the populated response DTO
   */
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