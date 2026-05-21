package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.iscritti;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.CkanClient;
import org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common.MurCsvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

/**
 * Reader for the enrollments-by-program job.
 *
 * The three historical files (13ter: 2010-2016, 13bis: 2016-2019, 13: 2019-2025)
 * share the same column structure and are merged into a single list.
 * The processor will handle M+F aggregation and idempotent upsert.
 */
public class IscrittixCorsoReader implements ItemReader<IscrittoRecord> {

  private static final Logger log = LoggerFactory.getLogger(IscrittixCorsoReader.class);

  /**
   * Fallback files in the classpath — the historical series split into 3 files,
   * ordered from oldest to most recent.
   */
  private static final List<String> CLASSPATH_FALLBACKS = List.of(
      "data/mur/13ter_iscrittixcorso.csv",
      "data/mur/13bis_iscrittixcorso_.csv",
      "data/mur/13_iscrittixcorso.csv"
  );

  private final CkanClient ckanClient;
  private final MurCsvReader murCsvReader;
  private final String ckanBaseUrl;
  private final String datasetId;

  private Iterator<IscrittoRecord> iterator;

  public IscrittixCorsoReader(
      CkanClient ckanClient,
      MurCsvReader murCsvReader,
      @Value("${fetcher.mur.ckan-base-url}") String ckanBaseUrl,
      @Value("${fetcher.mur.iscritti-dataset-id}") String datasetId) {
    this.ckanClient = ckanClient;
    this.murCsvReader = murCsvReader;
    this.ckanBaseUrl = ckanBaseUrl;
    this.datasetId = datasetId;
  }

  @Override
  public IscrittoRecord read() throws Exception {
    if (iterator == null) {
      List<IscrittoRecord> all = loadAll();
      log.info("IscrittixCorsoReader: total {} raw records loaded", all.size());
      iterator = all.iterator();
    }
    return iterator.hasNext() ? iterator.next() : null;
  }

  private List<IscrittoRecord> loadAll() {
    List<String[]> allRows = new ArrayList<>();

    List<String> patterns = List.of("13ter_iscrittixcorso", "13bis_iscrittixcorso",
        "13_iscrittixcorso");

    boolean atLeastOneDownloaded = false;
    for (int i = 0; i < patterns.size(); i++) {
      String pattern = patterns.get(i);
      Optional<String> url = ckanClient.findCsvUrl(ckanBaseUrl, datasetId, pattern);
      if (url.isPresent()) {
        List<String[]> rows = murCsvReader.downloadAndParse(url.get(), CLASSPATH_FALLBACKS.get(i));
        allRows.addAll(rows);
        atLeastOneDownloaded = true;
      }
    }

    if (!atLeastOneDownloaded) {
      log.warn("IscrittixCorsoReader: CKAN unavailable, loading all from classpath fallbacks");
      allRows = murCsvReader.readMultipleFromClasspath(CLASSPATH_FALLBACKS);
    }

    return parseRecords(allRows);
  }

  private List<IscrittoRecord> parseRecords(List<String[]> rows) {
    List<IscrittoRecord> records = new ArrayList<>();
    int skipped = 0;
    for (String[] fields : rows) {
      IscrittoRecord record = IscrittoRecord.fromCsvFields(fields);
      if (record != null) {
        records.add(record);
      } else {
        skipped++;
      }
    }
    if (skipped > 0) {
      log.warn("IscrittixCorsoReader: skipped {} malformed rows", skipped);
    }
    return records;
  }
}