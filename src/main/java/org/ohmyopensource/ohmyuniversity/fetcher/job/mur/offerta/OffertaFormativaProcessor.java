package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.offerta;

import java.time.Instant;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.CorsoLaureaNazionale;
import org.springframework.batch.infrastructure.item.ItemProcessor;

class OffertaFormativaProcessor
    implements ItemProcessor<OffertaFormativaRecord, CorsoLaureaNazionale> {

  @Override
  public CorsoLaureaNazionale process(OffertaFormativaRecord item) {
    CorsoLaureaNazionale entity = new CorsoLaureaNazionale();
    entity.setAnnoAccademico(item.getAnnoAccademico());
    entity.setAteneoNome(item.getAteneoNome());
    entity.setArea(item.getArea());
    entity.setGruppoNome(item.getGruppoNome());
    entity.setClasseLaureaCod(item.getClasseLaureaCod());
    entity.setClasseLaureaDes(item.getClasseLaureaDes());
    entity.setNomeCorso(item.getNomeCorso());
    entity.setTipoCorso(item.deriveTipoCorso());
    entity.setProvincia(item.getProvincia());
    entity.setComune(item.getComune());
    entity.setAccesso(item.getAccesso());
    entity.setDidattica(item.getDidattica());
    entity.setFetchedAt(Instant.now());
    return entity;
  }
}