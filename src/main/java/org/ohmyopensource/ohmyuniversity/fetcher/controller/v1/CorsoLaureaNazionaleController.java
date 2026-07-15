package org.ohmyopensource.ohmyuniversity.fetcher.controller.v1;

import java.util.List;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CorsoLaureaNazionale;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.CorsoLaureaNazionaleResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.service.CorsoLaureaNazionaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing national degree program data (MUR "Offerta formativa").
 *
 * <p>All endpoints are read-only — data is populated exclusively by the batch job.
 * No authentication is required, this is public reference data.
 *
 * <p>Intended consumer: the "sviluppi futuri" dashboard widget, to show a student
 * which degree programs (magistrali) are plausibly reachable from their current degree class. Note:
 * this endpoint returns raw MUR data filtered by class code — it does NOT yet apply the curated
 * classe_laurea_affinita mapping (e.g. resolving "which LM classes follow from an L-31"), that
 * layer comes next.
 *
 * <p>Served at {@code /api/v1/fetcher/corsi-laurea-nazionali}. The API gateway
 * rewrites the public path {@code /v1/fetcher/**} to the internal path {@code /api/v1/fetcher/**}
 * before forwarding to this service.
 */
@RestController
@RequestMapping("/api/v1/fetcher/corsi-laurea-nazionali")
public class CorsoLaureaNazionaleController {

  private final CorsoLaureaNazionaleService service;

  public CorsoLaureaNazionaleController(CorsoLaureaNazionaleService service) {
    this.service = service;
  }

  /**
   * Returns all national degree programs for a given degree class code, optionally scoped to a
   * specific academic year.
   *
   * @param classeLaurea   degree class code (e.g. "LM-32")
   * @param annoAccademico optional academic year filter; defaults to the most recent year present
   *                       in the dataset
   * @return {@code 200 OK} with the list of matching entries
   */
  @GetMapping
  public ResponseEntity<List<CorsoLaureaNazionaleResponse>> getByClasseLaurea(
      @RequestParam String classeLaurea,
      @RequestParam(required = false) Integer annoAccademico) {

    List<CorsoLaureaNazionale> results =
        service.findByClasseLaurea(classeLaurea, annoAccademico);

    return ResponseEntity.ok(results.stream().map(this::toResponse).toList());
  }

  /**
   * Returns magistrali (LM) in the same MUR disciplinary group as the given current degree class —
   * see {@link CorsoLaureaNazionaleService#findMagistraliAffini} for the "same area, not verified
   * eligibility" caveat.
   *
   * @param classeLaureaAttuale the student's current degree class code (e.g. "L-31")
   * @param annoAccademico      optional academic year filter; defaults to latest available
   * @return {@code 200 OK} with the list of matching entries, or {@code 404 Not Found} if
   * classeLaureaAttuale is unknown
   */
  @GetMapping("/magistrali-affini")
  public ResponseEntity<List<CorsoLaureaNazionaleResponse>> getMagistraliAffini(
      @RequestParam String classeLaureaAttuale,
      @RequestParam(required = false) Integer annoAccademico) {

    try {
      List<CorsoLaureaNazionale> results =
          service.findMagistraliAffini(classeLaureaAttuale, annoAccademico);
      return ResponseEntity.ok(results.stream().map(this::toResponse).toList());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  private CorsoLaureaNazionaleResponse toResponse(CorsoLaureaNazionale entity) {
    CorsoLaureaNazionaleResponse response = new CorsoLaureaNazionaleResponse();
    response.setId(entity.getId());
    response.setAnnoAccademico(entity.getAnnoAccademico());
    response.setAteneoNome(entity.getAteneoNome());
    response.setArea(entity.getArea());
    response.setGruppoNome(entity.getGruppoNome());
    response.setClasseLaureaCod(entity.getClasseLaureaCod());
    response.setClasseLaureaDes(entity.getClasseLaureaDes());
    response.setNomeCorso(entity.getNomeCorso());
    response.setTipoCorso(entity.getTipoCorso());
    response.setProvincia(entity.getProvincia());
    response.setComune(entity.getComune());
    response.setAccesso(entity.getAccesso());
    response.setDidattica(entity.getDidattica());
    response.setLinkRicerca(buildSearchLink(entity.getNomeCorso(), entity.getAteneoNome()));
    return response;
  }

  private String buildSearchLink(String nomeCorso, String ateneoNome) {
    String query = "\"" + nomeCorso + "\" " + ateneoNome
        + " requisiti di accesso regolamento didattico laurea magistrale";
    return "https://www.google.com/search?q="
        + java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);
  }
}