package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.laureati;

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
 * Reader for the laureati-per-corso job.
 *
 * Loads the three historical CSV files in sequence (2001-2009, 2010-2017, 2018-2024)
 * either via CKAN API or from the classpath fallback if the MUR portal is unreachable.
 */
public class LaureatiPerCorsoReader implements ItemReader<LaureatiRecord> {

  private static final Logger log = LoggerFactory.getLogger(LaureatiPerCorsoReader.class);

  private static final List<String> CLASSPATH_FALLBACKS = List.of(
      "data/mur/12a_laureati_corso_2001-2009.csv",
      "data/mur/12b_laureati_corso_2010-2017.csv",
      "data/mur/12c_laureati_corso_2018-2024.csv"
  );

  private static final List<String> CKAN_PATTERNS = List.of(
      "12a_laureati_corso",
      "12b_laureati_corso",
      "12c_laureati_corso"
  );

  private final CkanClient ckanClient;
  private final MurCsvReader murCsvReader;
  private final String ckanBaseUrl;
  private final String datasetId;
  private Iterator<LaureatiRecord> iterator;

  public LaureatiPerCorsoReader(
      CkanClient ckanClient,
      MurCsvReader murCsvReader,
      @Value("${fetcher.mur.ckan-base-url}") String ckanBaseUrl,
      @Value("${fetcher.mur.laureati-dataset-id}") String datasetId) {
    this.ckanClient = ckanClient;
    this.murCsvReader = murCsvReader;
    this.ckanBaseUrl = ckanBaseUrl;
    this.datasetId = datasetId;
  }

  @Override
  public LaureatiRecord read() throws Exception {
    if (iterator == null) {
      iterator = loadAll().iterator();
    }
    return iterator.hasNext() ? iterator.next() : null;
  }

  private List<LaureatiRecord> loadAll() {
    List<String[]> allRows = new ArrayList<>();
    boolean atLeastOneDownloaded = false;

    for (int i = 0; i < CKAN_PATTERNS.size(); i++) {
      Optional<String> url = ckanClient.findCsvUrl(ckanBaseUrl, datasetId, CKAN_PATTERNS.get(i));
      if (url.isPresent()) {
        List<String[]> rows = murCsvReader.downloadAndParse(url.get(), CLASSPATH_FALLBACKS.get(i));
        allRows.addAll(rows);
        atLeastOneDownloaded = true;
      }
    }

    if (!atLeastOneDownloaded) {
      log.warn("LaureatiPerCorsoReader: CKAN unavailable, loading all from classpath fallbacks");
      allRows = murCsvReader.readMultipleFromClasspath(CLASSPATH_FALLBACKS);
    }

    List<LaureatiRecord> records = new ArrayList<>();
    int skipped = 0;
    for (String[] fields : allRows) {
      LaureatiRecord r = LaureatiRecord.fromCsvFields(fields);
      if (r != null) {
        records.add(r);
      } else {
        skipped++;
      }
    }
    if (skipped > 0) {
      log.warn("LaureatiPerCorsoReader: skipped {} malformed rows", skipped);
    }
    log.info("LaureatiPerCorsoReader: loaded {} raw records", records.size());
    return records;
  }
}