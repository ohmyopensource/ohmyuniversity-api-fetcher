package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.offerta;

import java.util.Optional;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CorsoLaureaNazionale;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.CorsoLaureaNazionaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

class OffertaFormativaWriter implements ItemWriter<CorsoLaureaNazionale> {

  private static final Logger log = LoggerFactory.getLogger(OffertaFormativaWriter.class);

  private final CorsoLaureaNazionaleRepository repository;

  OffertaFormativaWriter(CorsoLaureaNazionaleRepository repository) {
    this.repository = repository;
  }

  @Override
  public void write(Chunk<? extends CorsoLaureaNazionale> chunk) {
    int created = 0;
    int updated = 0;

    for (CorsoLaureaNazionale entity : chunk.getItems()) {
      Optional<CorsoLaureaNazionale> existing = repository
          .findByAnnoAccademicoAndAteneoNomeAndNomeCorsoAndClasseLaureaCod(
              entity.getAnnoAccademico(), entity.getAteneoNome(),
              entity.getNomeCorso(), entity.getClasseLaureaCod());

      if (existing.isPresent()) {
        CorsoLaureaNazionale e = existing.get();
        e.setArea(entity.getArea());
        e.setGruppoNome(entity.getGruppoNome());
        e.setClasseLaureaDes(entity.getClasseLaureaDes());
        e.setTipoCorso(entity.getTipoCorso());
        e.setProvincia(entity.getProvincia());
        e.setComune(entity.getComune());
        e.setAccesso(entity.getAccesso());
        e.setDidattica(entity.getDidattica());
        e.setFetchedAt(entity.getFetchedAt());
        e.setUpdatedAt(entity.getFetchedAt());
        repository.save(e);
        updated++;
      } else {
        entity.setCreatedAt(entity.getFetchedAt());
        entity.setUpdatedAt(entity.getFetchedAt());
        repository.save(entity);
        created++;
      }
    }

    log.info("OffertaFormativaWriter: chunk processed — created={}, updated={}", created, updated);
  }
}