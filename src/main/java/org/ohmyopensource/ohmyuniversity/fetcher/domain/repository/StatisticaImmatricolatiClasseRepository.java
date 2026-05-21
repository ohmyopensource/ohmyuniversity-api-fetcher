package org.ohmyopensource.ohmyuniversity.fetcher.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiClasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link StatisticaImmatricolatiClasse}.
 */
@Repository
public interface StatisticaImmatricolatiClasseRepository
    extends JpaRepository<StatisticaImmatricolatiClasse, UUID> {

  Optional<StatisticaImmatricolatiClasse> findByAnnoAndClasseNumero(
      String anno,
      String classeNumero);

  List<StatisticaImmatricolatiClasse> findByClasseNumeroOrderByAnnoAsc(String classeNumero);

  List<StatisticaImmatricolatiClasse> findByAnnoOrderByTotDesc(String anno);
}