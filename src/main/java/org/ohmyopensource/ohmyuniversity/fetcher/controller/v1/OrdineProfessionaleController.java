package org.ohmyopensource.ohmyuniversity.fetcher.controller.v1;

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
 * REST controller exposing Italian professional orders data.
 *
 * <p>All endpoints are read-only — data is populated exclusively by the batch job.
 * No authentication is required since professional orders are public information.
 *
 * <p>Intended consumers: the frontend (to show students which orders they can join)
 * and other microservices (to query orders by degree class).
 */
@RestController
@RequestMapping("/api/ordini-professionali")
public class OrdineProfessionaleController {

  private final OrdineProfessionaleService service;

  // ============ Constructor ============

  /**
   * Creates the controller with the required service dependency.
   *
   * @param service service handling professional order query logic
   */
  public OrdineProfessionaleController(OrdineProfessionaleService service) {
    this.service = service;
  }

  // ============ Class Methods ============

  /**
   * Returns all professional orders, optionally filtered by category or degree class.
   *
   * <p>When both {@code categoria} and {@code classeLaurea} are provided,
   * {@code classeLaurea} takes precedence. When neither is provided, all orders
   * are returned.
   *
   * @param categoria    optional category filter (e.g. {@code LEGALE}, {@code SANITARIO})
   * @param classeLaurea optional degree class filter (e.g. {@code LM-41}, {@code LMG/01})
   * @return {@code 200 OK} with the list of matching {@link OrdineProfessionaleResponse} entries
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
   * Returns a single professional order by its internal UUID.
   *
   * @param id the order UUID
   * @return {@code 200 OK} with the order data, or {@code 404 Not Found} if not found
   */
  @GetMapping("/{id}")
  public ResponseEntity<OrdineProfessionaleResponse> getById(@PathVariable UUID id) {
    return service.findById(id)
        .map(this::toResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Maps an {@link OrdineProfessionale} entity to an {@link OrdineProfessionaleResponse} DTO.
   *
   * @param entity the source entity
   * @return the populated response DTO
   */
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