package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.laureati;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaLaureati;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Processor for the laureati-per-corso job.
 *
 * Accumulates all raw records in memory and aggregates M+F counts
 * by (annoLaurea, codiceAteneo, classeNumero, corsoNome) on flush.
 * Returns null on every process() call — the actual write happens
 * via {@link #flushAggregated()} called from the step listener afterStep.
 */
public class LaureatiPerCorsoProcessor
    implements ItemProcessor<LaureatiRecord, StatisticaLaureati> {

  private static final Logger log = LoggerFactory.getLogger(LaureatiPerCorsoProcessor.class);

  private final List<LaureatiRecord> buffer = new ArrayList<>();
  private boolean aggregated = false;

  @Override
  public StatisticaLaureati process(LaureatiRecord item) {
    buffer.add(item);
    return null;
  }

  /**
   * Aggregates all buffered records by (annoLaurea, codiceAteneo, classeNumero, corsoNome),
   * summing M and F counts. Can only be called once — subsequent calls return an empty list.
   */
  public List<StatisticaLaureati> flushAggregated() {
    if (aggregated) {
      return List.of();
    }
    aggregated = true;

    log.info("LaureatiPerCorsoProcessor: aggregating {} raw records", buffer.size());

    Map<String, StatisticaLaureati> map = new HashMap<>();
    Instant now = Instant.now();

    for (LaureatiRecord r : buffer) {
      String key = r.getAnnoLaurea() + "|" + r.getCodiceAteneo() + "|"
          + r.getClasseNumero() + "|" + r.getCorsoNome();

      StatisticaLaureati entity = map.computeIfAbsent(key, k -> {
        StatisticaLaureati e = new StatisticaLaureati();
        e.setAnnoLaurea(r.getAnnoLaurea());
        e.setCodiceAteneo(r.getCodiceAteneo());
        e.setNomeAteneo(r.getNomeAteneo());
        e.setClasseNumero(r.getClasseNumero());
        e.setCorsoNome(r.getCorsoNome());
        e.setTotM(0);
        e.setTotF(0);
        e.setTot(0);
        e.setFetchedAt(now);
        return e;
      });

      if ("M".equalsIgnoreCase(r.getSesso())) {
        entity.setTotM(entity.getTotM() + r.getLaureati());
      } else if ("F".equalsIgnoreCase(r.getSesso())) {
        entity.setTotF(entity.getTotF() + r.getLaureati());
      }
      entity.setTot(entity.getTotM() + entity.getTotF());
    }

    log.info("LaureatiPerCorsoProcessor: {} aggregated records", map.size());
    return new ArrayList<>(map.values());
  }
}