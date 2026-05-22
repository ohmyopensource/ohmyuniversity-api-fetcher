package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.laureati;

import java.util.List;
import java.util.Optional;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaLaureati;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaLaureatiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

/**
 * Writer for the laureati-per-corso job.
 *
 * Implements idempotent upsert: if a row with the same natural key
 * (annoLaurea, codiceAteneo, classeNumero, corsoNome) already exists,
 * its counters are updated in place; otherwise a new record is created.
 */
public class LaureatiPerCorsoWriter implements ItemWriter<StatisticaLaureati> {

  private static final Logger log = LoggerFactory.getLogger(LaureatiPerCorsoWriter.class);

  private final StatisticaLaureatiRepository repository;
  private final LaureatiPerCorsoProcessor processor;

  public LaureatiPerCorsoWriter(
      StatisticaLaureatiRepository repository,
      LaureatiPerCorsoProcessor processor) {
    this.repository = repository;
    this.processor = processor;
  }

  @Override
  public void write(Chunk<? extends StatisticaLaureati> chunk) {
    if (!chunk.isEmpty()) {
      upsertAll(chunk.getItems());
    }
  }

  /**
   * Called by the step listener after the reader is exhausted.
   * Retrieves aggregated entities from the processor and persists them.
   */
  public void flush() {
    List<StatisticaLaureati> entities = processor.flushAggregated();
    if (entities.isEmpty()) {
      return;
    }
    log.info("LaureatiPerCorsoWriter: flushing {} aggregated entities", entities.size());
    upsertAll(entities);
  }

  private void upsertAll(List<? extends StatisticaLaureati> entities) {
    int created = 0;
    int updated = 0;
    for (StatisticaLaureati entity : entities) {
      Optional<StatisticaLaureati> existing = repository
          .findByAnnoLaureaAndCodiceAteneoAndClasseNumeroAndCorsoNome(
              entity.getAnnoLaurea(),
              entity.getCodiceAteneo(),
              entity.getClasseNumero(),
              entity.getCorsoNome());

      if (existing.isPresent()) {
        StatisticaLaureati e = existing.get();
        e.setNomeAteneo(entity.getNomeAteneo());
        e.setTotM(entity.getTotM());
        e.setTotF(entity.getTotF());
        e.setTot(entity.getTot());
        e.setFetchedAt(entity.getFetchedAt());
        repository.save(e);
        updated++;
      } else {
        repository.save(entity);
        created++;
      }
    }
    log.info("LaureatiPerCorsoWriter: created={}, updated={}", created, updated);
  }
}