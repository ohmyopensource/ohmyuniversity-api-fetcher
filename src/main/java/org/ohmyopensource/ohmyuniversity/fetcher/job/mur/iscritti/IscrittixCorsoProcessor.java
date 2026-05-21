package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.iscritti;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaIscritti;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.iscritti.IscrittixCorsoAggregator.AggregatedIscrittoRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Processor for the enrollments-by-program job.
 *
 * Architectural note: Spring Batch processes data chunk by chunk, so the processor
 * receives records one at a time. However, M+F aggregation requires visibility over
 * all records together. The adopted solution is a stateful processor:
 * it accumulates all records in memory, performs the aggregation when the first
 * {@code null} is received from the reader (end of stream), then returns
 * the aggregated entities one by one.
 */
public class IscrittixCorsoProcessor
    implements ItemProcessor<IscrittoRecord, StatisticaIscritti> {

  private static final Logger log = LoggerFactory.getLogger(IscrittixCorsoProcessor.class);
  private static final String PRE_RIFORMA_CODICE = "99999";

  private final List<IscrittoRecord> buffer = new ArrayList<>();
  private Iterator<StatisticaIscritti> aggregatedIterator;
  private boolean aggregated = false;

  @Override
  public StatisticaIscritti process(IscrittoRecord item) {
    buffer.add(item);

    return null;
  }

  /**
   * Called by the writer after the reader has exhausted all records.
   * Aggregates the buffer and returns the list of entities ready for upsert.
   */
  public List<StatisticaIscritti> flushAggregated() {
    if (aggregated) {
      return List.of();
    }
    aggregated = true;

    log.info("IscrittixCorsoProcessor: aggregating {} raw records", buffer.size());
    List<AggregatedIscrittoRecord> aggregated = IscrittixCorsoAggregator.aggregate(buffer);
    log.info("IscrittixCorsoProcessor: {} aggregated records after M+F merge", aggregated.size());

    Instant now = Instant.now();
    List<StatisticaIscritti> result = new ArrayList<>(aggregated.size());

    for (AggregatedIscrittoRecord agg : aggregated) {
      StatisticaIscritti entity = new StatisticaIscritti();
      entity.setAnno(agg.getAnno());
      entity.setCodiceAteneo(agg.getCodiceAteneo());
      entity.setNomeAteneo(agg.getNomeAteneo());
      entity.setClasseNumero(agg.getClasseNumero());
      entity.setCorsoNome(agg.getCorsoNome());
      entity.setTotM(agg.getTotM());
      entity.setTotF(agg.getTotF());
      entity.setTot(agg.getTot());
      entity.setPreRiforma(PRE_RIFORMA_CODICE.equals(agg.getClasseNumero()));
      entity.setFetchedAt(now);
      result.add(entity);
    }

    return result;
  }
}