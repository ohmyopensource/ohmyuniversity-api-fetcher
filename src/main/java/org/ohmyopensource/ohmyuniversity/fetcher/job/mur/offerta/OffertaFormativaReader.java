package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.offerta;

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
 * Reader for the "offerta formativa" job — national list of active degree
 * programs (a.a. 2010-2024), published by MUR as CSV.
 *
 * TODO(luca): CSV_NAME_PATTERN is a best guess following the naming
 * convention of the sibling historical resource ("corsidilaurea_2001-2009.csv").
 * Confirm the real download URL from the CKAN resource page before running
 * this job in dev.
 */
class OffertaFormativaReader implements ItemReader<OffertaFormativaRecord> {

  private static final Logger log = LoggerFactory.getLogger(OffertaFormativaReader.class);
  private static final String CLASSPATH_FALLBACK = "data/mur/corsidilaurea_2010-2024.csv";
  private static final String CSV_NAME_PATTERN = "corsidilaurea_2010-2024";

  private final CkanClient ckanClient;
  private final MurCsvReader murCsvReader;
  private final String ckanBaseUrl;
  private final String datasetId;
  private Iterator<OffertaFormativaRecord> iterator;

  OffertaFormativaReader(
      CkanClient ckanClient,
      MurCsvReader murCsvReader,
      @Value("${fetcher.mur.ckan-base-url}") String ckanBaseUrl,
      @Value("${fetcher.mur.offerta-dataset-id}") String datasetId) {
    this.ckanClient = ckanClient;
    this.murCsvReader = murCsvReader;
    this.ckanBaseUrl = ckanBaseUrl;
    this.datasetId = datasetId;
  }

  @Override
  public OffertaFormativaRecord read() throws Exception {
    if (iterator == null) {
      iterator = loadAll().iterator();
    }
    return iterator.hasNext() ? iterator.next() : null;
  }

  private List<OffertaFormativaRecord> loadAll() {
    Optional<String> url = ckanClient.findCsvUrl(ckanBaseUrl, datasetId, CSV_NAME_PATTERN);
    List<String[]> rows = murCsvReader.downloadAndParse(url.orElse(null), CLASSPATH_FALLBACK);

    List<OffertaFormativaRecord> records = new ArrayList<>();
    int skipped = 0;
    for (String[] fields : rows) {
      OffertaFormativaRecord r = OffertaFormativaRecord.fromCsvFields(fields);
      if (r != null) {
        records.add(r);
      } else {
        skipped++;
      }
    }
    if (skipped > 0) {
      log.warn("OffertaFormativaReader: skipped {} malformed rows", skipped);
    }
    log.info("OffertaFormativaReader: loaded {} records", records.size());
    return records;
  }
}