package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.iscritti;

import java.util.List;
import java.util.Optional;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaIscritti;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaIscrittiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

/**
 * Writer for the iscritti-per-corso job.
 *
 * Implements idempotent upsert: if a row with the same natural key
 * (anno, codiceAteneo, classeNumero, corsoNome) already exists, its counters
 * are updated in place; otherwise a new record is created.
 *
 * Because the processor accumulates all records in memory and aggregates
 * on flush, this writer receives chunks of null items during normal execution.
 * The actual save happens when {@link #flush()} is called from the
 * {@code StepExecutionListener.afterStep} hook defined in the job config.
 */
public class IscrittixCorsoWriter implements ItemWriter<StatisticaIscritti> {

  private static final Logger log = LoggerFactory.getLogger(IscrittixCorsoWriter.class);

  private final StatisticaIscrittiRepository repository;
  private final IscrittixCorsoProcessor processor;

  public IscrittixCorsoWriter(
      StatisticaIscrittiRepository repository,
      IscrittixCorsoProcessor processor) {
    this.repository = repository;
    this.processor = processor;
  }

  @Override
  public void write(Chunk<? extends StatisticaIscritti> chunk) {
    if (!chunk.isEmpty()) {
      upsertAll(chunk.getItems());
    }
  }

  /**
   * Called by the step listener after the reader is exhausted.
   * Retrieves aggregated entities from the processor and persists them.
   */
  public void flush() {
    List<StatisticaIscritti> entities = processor.flushAggregated();
    if (entities.isEmpty()) {
      return;
    }
    log.info("IscrittixCorsoWriter: flushing {} aggregated entities", entities.size());
    upsertAll(entities);
  }

  private void upsertAll(List<? extends StatisticaIscritti> entities) {
    int created = 0;
    int updated = 0;

    for (StatisticaIscritti entity : entities) {
      Optional<StatisticaIscritti> existing = repository
          .findByAnnoAndCodiceAteneoAndClasseNumeroAndCorsoNome(
              entity.getAnno(),
              entity.getCodiceAteneo(),
              entity.getClasseNumero(),
              entity.getCorsoNome());

      if (existing.isPresent()) {
        StatisticaIscritti e = existing.get();
        e.setTotM(entity.getTotM());
        e.setTotF(entity.getTotF());
        e.setTot(entity.getTot());
        e.setNomeAteneo(entity.getNomeAteneo());
        e.setPreRiforma(entity.isPreRiforma());
        e.setFetchedAt(entity.getFetchedAt());
        repository.save(e);
        updated++;
      } else {
        repository.save(entity);
        created++;
      }
    }

    log.info("IscrittixCorsoWriter: created={}, updated={}", created, updated);
  }
}