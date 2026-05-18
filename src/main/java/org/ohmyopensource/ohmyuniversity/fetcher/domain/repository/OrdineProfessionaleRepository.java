package org.ohmyopensource.ohmyuniversity.fetcher.domain.repository;

import java.util.Optional;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.OrdineProfessionale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link OrdineProfessionale} entities.
 */
@Repository
public interface OrdineProfessionaleRepository
    extends JpaRepository<OrdineProfessionale, UUID> {

  /**
   * Find an ordine by its unique business key.
   * Used for upsert logic in OrdiniWriter — if it exists, update it;
   * otherwise create a new record.
   *
   * @param codice the unique code (e.g. "AVVOCATI")
   * @return the existing record if found
   */
  Optional<OrdineProfessionale> findByCodice(String codice);
}