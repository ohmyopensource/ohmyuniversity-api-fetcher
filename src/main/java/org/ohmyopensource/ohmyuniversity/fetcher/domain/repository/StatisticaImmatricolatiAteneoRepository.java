package org.ohmyopensource.ohmyuniversity.fetcher.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiAteneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link StatisticaImmatricolatiAteneo}.
 */
@Repository
public interface StatisticaImmatricolatiAteneoRepository
    extends JpaRepository<StatisticaImmatricolatiAteneo, UUID> {

  Optional<StatisticaImmatricolatiAteneo> findByAnnoAndCodiceAteneo(
      String anno,
      String codiceAteneo);

  List<StatisticaImmatricolatiAteneo> findByCodiceAteneoOrderByAnnoAsc(String codiceAteneo);

  List<StatisticaImmatricolatiAteneo> findByAnnoOrderByTotDesc(String anno);
}