package org.ohmyopensource.ohmyuniversity.fetcher.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CategoriaOrdine;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.OrdineProfessionale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link OrdineProfessionale} entities.
 */
@Repository
public interface OrdineProfessionaleRepository
    extends JpaRepository<OrdineProfessionale, UUID> {

  /**
   * Find an ordine by its unique business key.
   * Used for upsert logic in OrdiniWriter.
   *
   * @param codice the unique code (e.g. "AVVOCATI")
   * @return the existing record if found
   */
  Optional<OrdineProfessionale> findByCodice(String codice);

  /**
   * Find all ordini in a given category.
   *
   * @param categoria the category to filter by
   * @return list of matching ordini
   */
  List<OrdineProfessionale> findByCategoria(CategoriaOrdine categoria);

  /**
   * Find all ordini that require a specific degree class.
   * Uses a PostgreSQL array contains query on the requisiti_laurea column.
   *
   * @param classeLaurea the degree class code (e.g. "LMG/01", "LM-41")
   * @return list of ordini that require that degree class
   */
  @Query(value = "SELECT * FROM ordine_professionale WHERE :classeLaurea = ANY(requisiti_laurea)",
      nativeQuery = true)
  List<OrdineProfessionale> findByRequisitiLaureaContaining(
      @Param("classeLaurea") String classeLaurea);
}