package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.immatricolati;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiAteneo;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaImmatricolatiAteneoRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.CkanClient;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.MurCsvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

/**
 * Reader, Processor and Writer for the second step of the immatricolati job:
 * first-year students per university (file 02_immatricolatixateneo).
 */

// ================================
// READER
// ================================

class ImmatricolatiPerAteneoReader implements ItemReader<ImmatricolatoAteneoRecord> {

  private static final Logger log = LoggerFactory.getLogger(ImmatricolatiPerAteneoReader.class);
  private static final String CLASSPATH_FALLBACK = "data/mur/02_immatricolatixateneo.csv";
  private static final String CSV_NAME_PATTERN = "immatricolatixateneo";

  private final CkanClient ckanClient;
  private final MurCsvReader murCsvReader;
  private final String ckanBaseUrl;
  private final String datasetId;
  private Iterator<ImmatricolatoAteneoRecord> iterator;

  ImmatricolatiPerAteneoReader(
      CkanClient ckanClient,
      MurCsvReader murCsvReader,
      @Value("${fetcher.mur.ckan-base-url}") String ckanBaseUrl,
      @Value("${fetcher.mur.immatricolati-dataset-id}") String datasetId) {
    this.ckanClient = ckanClient;
    this.murCsvReader = murCsvReader;
    this.ckanBaseUrl = ckanBaseUrl;
    this.datasetId = datasetId;
  }

  @Override
  public ImmatricolatoAteneoRecord read() throws Exception {
    if (iterator == null) {
      iterator = loadAll().iterator();
    }
    return iterator.hasNext() ? iterator.next() : null;
  }

  private List<ImmatricolatoAteneoRecord> loadAll() {
    Optional<String> url = ckanClient.findCsvUrl(ckanBaseUrl, datasetId, CSV_NAME_PATTERN);
    List<String[]> rows = murCsvReader.downloadAndParse(url.orElse(null), CLASSPATH_FALLBACK);

    List<ImmatricolatoAteneoRecord> records = new ArrayList<>();
    int skipped = 0;
    for (String[] fields : rows) {
      ImmatricolatoAteneoRecord r = ImmatricolatoAteneoRecord.fromCsvFields(fields);
      if (r != null) {
        records.add(r);
      } else {
        skipped++;
      }
    }
    if (skipped > 0) {
      log.warn("ImmatricolatiPerAteneoReader: skipped {} malformed rows", skipped);
    }
    log.info("ImmatricolatiPerAteneoReader: loaded {} raw records", records.size());
    return records;
  }
}

// ================================
// PROCESSOR
// ================================

class ImmatricolatiPerAteneoProcessor
    implements ItemProcessor<ImmatricolatoAteneoRecord, StatisticaImmatricolatiAteneo> {

  private static final Logger log =
      LoggerFactory.getLogger(ImmatricolatiPerAteneoProcessor.class);

  private final List<ImmatricolatoAteneoRecord> buffer = new ArrayList<>();
  private boolean aggregated = false;

  @Override
  public StatisticaImmatricolatiAteneo process(ImmatricolatoAteneoRecord item) {
    buffer.add(item);
    return null;
  }

  /**
   * Aggregates all buffered records by (anno, codiceAteneo), summing M and F counts.
   * Can only be called once — subsequent calls return an empty list.
   */
  public List<StatisticaImmatricolatiAteneo> flushAggregated() {
    if (aggregated) {
      return List.of();
    }
    aggregated = true;

    log.info("ImmatricolatiPerAteneoProcessor: aggregating {} raw records", buffer.size());

    Map<String, StatisticaImmatricolatiAteneo> map = new HashMap<>();
    Instant now = Instant.now();

    for (ImmatricolatoAteneoRecord r : buffer) {
      String key = r.getAnno() + "|" + r.getCodiceAteneo();
      StatisticaImmatricolatiAteneo entity = map.computeIfAbsent(key, k -> {
        StatisticaImmatricolatiAteneo e = new StatisticaImmatricolatiAteneo();
        e.setAnno(r.getAnno());
        e.setCodiceAteneo(r.getCodiceAteneo());
        e.setNomeAteneo(r.getNomeAteneo());
        e.setTotM(0);
        e.setTotF(0);
        e.setTot(0);
        e.setFetchedAt(now);
        return e;
      });

      if ("M".equalsIgnoreCase(r.getSesso())) {
        entity.setTotM(entity.getTotM() + r.getImmatricolati());
      } else if ("F".equalsIgnoreCase(r.getSesso())) {
        entity.setTotF(entity.getTotF() + r.getImmatricolati());
      }
      entity.setTot(entity.getTotM() + entity.getTotF());
    }

    log.info("ImmatricolatiPerAteneoProcessor: {} aggregated records", map.size());
    return new ArrayList<>(map.values());
  }
}

// ================================
// WRITER
// ================================

class ImmatricolatiPerAteneoWriter
    implements ItemWriter<StatisticaImmatricolatiAteneo> {

  private static final Logger log =
      LoggerFactory.getLogger(ImmatricolatiPerAteneoWriter.class);

  private final StatisticaImmatricolatiAteneoRepository repository;
  private final ImmatricolatiPerAteneoProcessor processor;

  ImmatricolatiPerAteneoWriter(
      StatisticaImmatricolatiAteneoRepository repository,
      ImmatricolatiPerAteneoProcessor processor) {
    this.repository = repository;
    this.processor = processor;
  }

  @Override
  public void write(Chunk<? extends StatisticaImmatricolatiAteneo> chunk) {
    // During chunk processing the processor returns null — chunk is empty.
    // We only persist when real entities are present.
    if (!chunk.isEmpty()) {
      upsertAll(chunk.getItems());
    }
  }

  /**
   * Called by the step listener after the reader is exhausted.
   * Retrieves aggregated entities from the processor and persists them.
   */
  public void flush() {
    List<StatisticaImmatricolatiAteneo> entities = processor.flushAggregated();
    if (entities.isEmpty()) {
      return;
    }
    log.info("ImmatricolatiPerAteneoWriter: flushing {} entities", entities.size());
    upsertAll(entities);
  }

  private void upsertAll(List<? extends StatisticaImmatricolatiAteneo> entities) {
    int created = 0;
    int updated = 0;
    for (StatisticaImmatricolatiAteneo entity : entities) {
      Optional<StatisticaImmatricolatiAteneo> existing =
          repository.findByAnnoAndCodiceAteneo(entity.getAnno(), entity.getCodiceAteneo());
      if (existing.isPresent()) {
        StatisticaImmatricolatiAteneo e = existing.get();
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
    log.info("ImmatricolatiPerAteneoWriter: created={}, updated={}", created, updated);
  }
}