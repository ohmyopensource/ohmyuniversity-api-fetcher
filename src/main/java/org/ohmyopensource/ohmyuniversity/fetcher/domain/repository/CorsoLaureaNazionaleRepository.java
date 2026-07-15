package org.ohmyopensource.ohmyuniversity.fetcher.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CorsoLaureaNazionale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CorsoLaureaNazionaleRepository extends JpaRepository<CorsoLaureaNazionale, UUID> {

  Optional<CorsoLaureaNazionale> findByAnnoAccademicoAndAteneoNomeAndNomeCorsoAndClasseLaureaCod(
      Integer annoAccademico, String ateneoNome, String nomeCorso, String classeLaureaCod);

  List<CorsoLaureaNazionale> findByClasseLaureaCodAndAnnoAccademico(
      String classeLaureaCod, Integer annoAccademico);

  List<CorsoLaureaNazionale> findByTipoCorsoAndAnnoAccademico(
      String tipoCorso, Integer annoAccademico);

  Optional<CorsoLaureaNazionale> findFirstByClasseLaureaCodAndAnnoAccademico(
      String classeLaureaCod, Integer annoAccademico);

  List<CorsoLaureaNazionale> findByTipoCorsoAndGruppoNomeAndAnnoAccademicoOrderByNomeCorsoAsc(
      String tipoCorso, String gruppoNome, Integer annoAccademico);

  /**
   * Returns the most recent academic year present in the dataset — used as the default when the
   * caller does not specify one, so the frontend shows only the latest offerta formativa instead of
   * 15 years of history stacked together.
   */
  @Query("SELECT MAX(c.annoAccademico) FROM CorsoLaureaNazionale c")
  Optional<Integer> findLatestAnnoAccademico();
}