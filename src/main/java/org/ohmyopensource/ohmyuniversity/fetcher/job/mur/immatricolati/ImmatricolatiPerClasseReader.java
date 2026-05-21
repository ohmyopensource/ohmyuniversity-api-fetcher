package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.immatricolati;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.StatisticaImmatricolatiClasse;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.StatisticaImmatricolatiClasseRepository;
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
 * Reader, Processor and Writer for the first step of the immatricolati job:
 * first-year students per degree class (file 17_immatricolatixclasse).
 *
 * All three components are defined in this file because they share
 * the same M+F aggregation logic and follow the same pattern as the iscritti job.
 */

// ================================
// READER
// ================================

class ImmatricolatiPerClasseReader implements ItemReader<ImmatricolatoClasseRecord> {

  private static final Logger log = LoggerFactory.getLogger(ImmatricolatiPerClasseReader.class);
  private static final String CLASSPATH_FALLBACK = "data/mur/17_immatricolatixclasse.csv";
  private static final String CSV_NAME_PATTERN = "immatricolatixclasse";

  private final CkanClient ckanClient;
  private final MurCsvReader murCsvReader;
  private final String ckanBaseUrl;
  private final String datasetId;
  private Iterator<ImmatricolatoClasseRecord> iterator;

  ImmatricolatiPerClasseReader(
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
  public ImmatricolatoClasseRecord read() throws Exception {
    if (iterator == null) {
      iterator = loadAll().iterator();
    }
    return iterator.hasNext() ? iterator.next() : null;
  }

  private List<ImmatricolatoClasseRecord> loadAll() {
    Optional<String> url = ckanClient.findCsvUrl(ckanBaseUrl, datasetId, CSV_NAME_PATTERN);
    List<String[]> rows = murCsvReader.downloadAndParse(url.orElse(null), CLASSPATH_FALLBACK);

    List<ImmatricolatoClasseRecord> records = new ArrayList<>();
    int skipped = 0;
    for (String[] fields : rows) {
      ImmatricolatoClasseRecord r = ImmatricolatoClasseRecord.fromCsvFields(fields);
      if (r != null) {
        records.add(r);
      } else {
        skipped++;
      }
    }
    if (skipped > 0) {
      log.warn("ImmatricolatiPerClasseReader: skipped {} malformed rows", skipped);
    }
    log.info("ImmatricolatiPerClasseReader: loaded {} raw records", records.size());
    return records;
  }
}

// ================================
// PROCESSOR
// ================================

class ImmatricolatiPerClasseProcessor
    implements ItemProcessor<ImmatricolatoClasseRecord, StatisticaImmatricolatiClasse> {

  private static final Logger log =
      LoggerFactory.getLogger(ImmatricolatiPerClasseProcessor.class);

  private final List<ImmatricolatoClasseRecord> buffer = new ArrayList<>();
  private boolean aggregated = false;

  @Override
  public StatisticaImmatricolatiClasse process(ImmatricolatoClasseRecord item) {
    buffer.add(item);
    return null;
  }

  /**
   * Aggregates all buffered records by (anno, classeNumero), summing M and F counts.
   * Can only be called once — subsequent calls return an empty list.
   */
  public List<StatisticaImmatricolatiClasse> flushAggregated() {
    if (aggregated) {
      return List.of();
    }
    aggregated = true;

    log.info("ImmatricolatiPerClasseProcessor: aggregating {} raw records", buffer.size());

    Map<String, StatisticaImmatricolatiClasse> map = new HashMap<>();
    Instant now = Instant.now();

    for (ImmatricolatoClasseRecord r : buffer) {
      String key = r.getAnno() + "|" + r.getClasseNumero();
      StatisticaImmatricolatiClasse entity = map.computeIfAbsent(key, k -> {
        StatisticaImmatricolatiClasse e = new StatisticaImmatricolatiClasse();
        e.setAnno(r.getAnno());
        e.setClasseNumero(r.getClasseNumero());
        e.setClasseNome(r.getClasseNome());
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

    log.info("ImmatricolatiPerClasseProcessor: {} aggregated records", map.size());
    return new ArrayList<>(map.values());
  }
}

// ================================
// WRITER
// ================================

class ImmatricolatiPerClasseWriter
    implements ItemWriter<StatisticaImmatricolatiClasse> {

  private static final Logger log =
      LoggerFactory.getLogger(ImmatricolatiPerClasseWriter.class);

  private final StatisticaImmatricolatiClasseRepository repository;
  private final ImmatricolatiPerClasseProcessor processor;

  ImmatricolatiPerClasseWriter(
      StatisticaImmatricolatiClasseRepository repository,
      ImmatricolatiPerClasseProcessor processor) {
    this.repository = repository;
    this.processor = processor;
  }

  @Override
  public void write(Chunk<? extends StatisticaImmatricolatiClasse> chunk) {
    if (!chunk.isEmpty()) {
      upsertAll(chunk.getItems());
    }
  }

  /**
   * Called by the step listener after the reader is exhausted.
   * Retrieves aggregated entities from the processor and persists them.
   */
  public void flush() {
    List<StatisticaImmatricolatiClasse> entities = processor.flushAggregated();
    if (entities.isEmpty()) {
      return;
    }
    log.info("ImmatricolatiPerClasseWriter: flushing {} entities", entities.size());
    upsertAll(entities);
  }

  private void upsertAll(List<? extends StatisticaImmatricolatiClasse> entities) {
    int created = 0;
    int updated = 0;
    for (StatisticaImmatricolatiClasse entity : entities) {
      Optional<StatisticaImmatricolatiClasse> existing =
          repository.findByAnnoAndClasseNumero(entity.getAnno(), entity.getClasseNumero());
      if (existing.isPresent()) {
        StatisticaImmatricolatiClasse e = existing.get();
        e.setClasseNome(entity.getClasseNome());
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
    log.info("ImmatricolatiPerClasseWriter: created={}, updated={}", created, updated);
  }
}