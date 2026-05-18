package org.ohmyopensource.ohmyuniversity.fetcher.job.ordini;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads ordini-professionali.json from the classpath and returns
 * one {@link OrdineRecord} at a time to the Spring Batch step.
 */
public class OrdiniReader implements ItemReader<OrdineRecord> {

  private static final Logger log = LoggerFactory.getLogger(OrdiniReader.class);
  private static final String DATA_PATH = "data/ordini-professionali.json";

  private final JsonMapper jsonMapper;
  private Iterator<OrdineRecord> iterator;

  public OrdiniReader(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Override
  public OrdineRecord read() throws Exception {
    if (iterator == null) {
      List<OrdineRecord> all = loadAll();
      log.info("OrdiniReader: loaded {} records from {}", all.size(), DATA_PATH);
      iterator = all.iterator();
    }
    return iterator.hasNext() ? iterator.next() : null;
  }

  private List<OrdineRecord> loadAll() {
    try {
      InputStream stream = new ClassPathResource(DATA_PATH).getInputStream();
      return jsonMapper.readValue(stream, new TypeReference<List<OrdineRecord>>() {});
    } catch (IOException e) {
      throw new RuntimeException("Failed to load " + DATA_PATH, e);
    }
  }
}
