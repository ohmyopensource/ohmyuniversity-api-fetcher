package org.ohmyopensource.ohmyuniversity.fetcher.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaIscritti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link StatisticaIscritti}.
 */
@Repository
public interface StatisticaIscrittiRepository
    extends JpaRepository<StatisticaIscritti, UUID> {

  Optional<StatisticaIscritti> findByAnnoAndCodiceAteneoAndClasseNumeroAndCorsoNome(
      String anno,
      String codiceAteneo,
      String classeNumero,
      String corsoNome);

  List<StatisticaIscritti> findByClasseNumeroOrderByAnnoAsc(String classeNumero);

  List<StatisticaIscritti> findByCodiceAteneoOrderByAnnoAsc(String codiceAteneo);

  List<StatisticaIscritti> findByAnnoAndCodiceAteneo(String anno, String codiceAteneo);

  List<StatisticaIscritti> findByAnnoAndClasseNumero(String anno, String classeNumero);

  List<StatisticaIscritti> findByCorsoNomeContainingIgnoreCaseOrderByAnnoDesc(String corsoNome);
}