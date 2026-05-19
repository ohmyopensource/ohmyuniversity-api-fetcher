package org.ohmyopensource.ohmyuniversity.fetcher.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CategoriaOrdine;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.OrdineProfessionale;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.OrdineProfessionaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for querying {@link OrdineProfessionale} data.
 *
 * Read-only service — all writes happen exclusively via the batch job.
 */
@Service
@Transactional(readOnly = true)
public class OrdineProfessionaleService {

  private final OrdineProfessionaleRepository repository;

  public OrdineProfessionaleService(OrdineProfessionaleRepository repository) {
    this.repository = repository;
  }

  /**
   * Return all professional orders.
   */
  public List<OrdineProfessionale> findAll() {
    return repository.findAll();
  }

  /**
   * Return a single professional order by its internal UUID.
   */
  public Optional<OrdineProfessionale> findById(UUID id) {
    return repository.findById(id);
  }

  /**
   * Return all professional orders in a given category.
   *
   * @param categoria the category to filter by
   */
  public List<OrdineProfessionale> findByCategoria(CategoriaOrdine categoria) {
    return repository.findByCategoria(categoria);
  }

  /**
   * Return all professional orders that require a specific degree class.
   * Used by students to find which orders they can join with their degree.
   *
   * @param classeLaurea the degree class code (e.g. "LMG/01", "LM-41")
   */
  public List<OrdineProfessionale> findByClasseLaurea(String classeLaurea) {
    return repository.findByRequisitiLaureaContaining(classeLaurea);
  }
}