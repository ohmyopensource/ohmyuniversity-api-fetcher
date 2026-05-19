package org.ohmyopensource.ohmyuniversity.fetcher.controller;

import java.util.List;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CategoriaOrdine;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.OrdineProfessionale;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.OrdineProfessionaleResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.service.OrdineProfessionaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for professional orders data.
 *
 * <p>Read-only endpoints — all data is populated by the batch job.
 * No authentication required — this data is public information.
 *
 * <p>Intended consumers:
 * <ul>
 *   <li>Frontend — show students which orders they can join</li>
 *   <li>Other microservices — query orders by degree class</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ordini-professionali")
public class OrdineProfessionaleController {

  private final OrdineProfessionaleService service;

  public OrdineProfessionaleController(OrdineProfessionaleService service) {
    this.service = service;
  }

  /**
   * Get all professional orders.
   * Optionally filter by categoria or by classe di laurea.
   *
   * @param categoria    optional category filter
   * @param classeLaurea optional degree class filter (e.g. "LM-41", "LMG/01")
   * @return list of matching orders
   */
  @GetMapping
  public ResponseEntity<List<OrdineProfessionaleResponse>> getAll(
      @RequestParam(required = false) CategoriaOrdine categoria,
      @RequestParam(required = false) String classeLaurea) {

    List<OrdineProfessionale> results;

    if (classeLaurea != null && !classeLaurea.isBlank()) {
      results = service.findByClasseLaurea(classeLaurea);
    } else if (categoria != null) {
      results = service.findByCategoria(categoria);
    } else {
      results = service.findAll();
    }

    return ResponseEntity.ok(results.stream().map(this::toResponse).toList());
  }

  /**
   * Get a single professional order by its internal UUID.
   *
   * @param id the order UUID
   * @return 200 with order data, 404 if not found
   */
  @GetMapping("/{id}")
  public ResponseEntity<OrdineProfessionaleResponse> getById(@PathVariable UUID id) {
    return service.findById(id)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // ================================
  // Private mapping helpers
  // ================================

  private OrdineProfessionaleResponse toResponse(OrdineProfessionale entity) {
    OrdineProfessionaleResponse response = new OrdineProfessionaleResponse();
    response.setId(entity.getId());
    response.setCodice(entity.getCodice());
    response.setNome(entity.getNome());
    response.setCategoria(entity.getCategoria());
    response.setOrganismoNazionale(entity.getOrganismoNazionale());
    response.setSitoUfficiale(entity.getSitoUfficiale());
    response.setRequisitiLaurea(entity.getRequisitiLaurea());
    response.setRequisitiNote(entity.getRequisitiNote());
    response.setMinisteroVigilanza(entity.getMinisteroVigilanza());
    response.setAggiornatoAl(entity.getAggiornatoAl());
    return response;
  }
}