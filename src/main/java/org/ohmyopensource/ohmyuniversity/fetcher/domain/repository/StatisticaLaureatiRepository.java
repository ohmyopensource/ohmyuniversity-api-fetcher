package org.ohmyopensource.ohmyuniversity.fetcher.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaLaureati;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link StatisticaLaureati}.
 */
@Repository
public interface StatisticaLaureatiRepository
    extends JpaRepository<StatisticaLaureati, UUID> {

  Optional<StatisticaLaureati> findByAnnoLaureaAndCodiceAteneoAndClasseNumeroAndCorsoNome(
      int annoLaurea,
      String codiceAteneo,
      String classeNumero,
      String corsoNome);

  List<StatisticaLaureati> findByClasseNumeroOrderByAnnoLaureaAsc(String classeNumero);

  List<StatisticaLaureati> findByCodiceAteneoOrderByAnnoLaureaAsc(String codiceAteneo);

  List<StatisticaLaureati> findByAnnoLaureaAndCodiceAteneo(int annoLaurea, String codiceAteneo);

  List<StatisticaLaureati> findByAnnoLaureaAndClasseNumero(int annoLaurea, String classeNumero);

  List<StatisticaLaureati> findByCorsoNomeContainingIgnoreCaseOrderByAnnoLaureaDesc(
      String corsoNome);
}