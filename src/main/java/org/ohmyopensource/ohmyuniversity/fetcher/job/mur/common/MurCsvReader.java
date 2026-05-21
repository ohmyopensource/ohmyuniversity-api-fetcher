package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common;

import io.netty.channel.ChannelOption;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Utility for downloading and parsing CSV files from dati-ustat.mur.gov.it.
 *
 * WebClient is configured with a 200MB in-memory buffer limit to handle
 * large CSV files. The default WebClient limit is 256KB which is insufficient.
 *
 * Fallback strategy: if the HTTP download fails (MUR down, network unavailable),
 * the reader falls back to the file bundled in the classpath under {@code data/mur/}.
 * This ensures the job never fails due to external unavailability and that
 * historical data already shipped in the classpath is always available as a baseline.
 */
@Component
public class MurCsvReader {

  private static final Logger log = LoggerFactory.getLogger(MurCsvReader.class);
  private static final String SEPARATOR = ";";
  private static final Charset UTF8 = StandardCharsets.UTF_8;

  private static final int MAX_BUFFER_BYTES = 200 * 1024 * 1024;

  private final WebClient webClient;

  public MurCsvReader() {
    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(config -> config.defaultCodecs().maxInMemorySize(MAX_BUFFER_BYTES))
        .build();

    this.webClient = WebClient.builder()
        .exchangeStrategies(strategies)
        .build();
  }

  /**
   * Downloads the CSV from the given URL and returns all data rows already split,
   * excluding the header row. Falls back to the classpath file if the download fails.
   *
   * @param csvUrl            direct CSV download URL (obtained from CkanClient)
   * @param classpathFallback classpath-relative path, e.g. "data/mur/13_iscrittixcorso.csv"
   * @return list of String[] arrays, one per data row (header excluded)
   */
  public List<String[]> downloadAndParse(String csvUrl, String classpathFallback) {
    if (csvUrl != null) {
      try {
        log.info("MurCsvReader: downloading CSV from {}", csvUrl);
        byte[] bytes = webClient.get()
            .uri(csvUrl)
            .retrieve()
            .bodyToMono(byte[].class)
            .onErrorResume(e -> {
              log.warn("MurCsvReader: HTTP download failed: {}", e.getMessage());
              return Mono.empty();
            })
            .block();

        if (bytes != null && bytes.length > 0) {
          List<String[]> rows = parseBytes(bytes);
          log.info("MurCsvReader: downloaded and parsed {} data rows from URL", rows.size());
          return rows;
        }
      } catch (Exception e) {
        log.warn("MurCsvReader: failed to download from URL, trying classpath fallback: {}",
            e.getMessage());
      }
    }

    return readFromClasspath(classpathFallback);
  }

  /**
   * Reads multiple CSV files from the classpath in sequence and merges them into a single list.
   * Useful for the iscritti job whose historical series is split across 3 files.
   * The header is skipped in every file independently.
   *
   * @param classpathPaths list of classpath paths, read in the order provided
   * @return unified list of String[] arrays
   */
  public List<String[]> readMultipleFromClasspath(List<String> classpathPaths) {
    List<String[]> all = new ArrayList<>();
    for (String path : classpathPaths) {
      List<String[]> rows = readFromClasspath(path);
      all.addAll(rows);
      log.info("MurCsvReader: loaded {} rows from classpath:{}", rows.size(), path);
    }
    log.info("MurCsvReader: total {} rows from {} classpath files", all.size(),
        classpathPaths.size());
    return all;
  }

  // ================================
  // Private helpers
  // ================================

  private List<String[]> readFromClasspath(String path) {
    log.info("MurCsvReader: reading from classpath:{}", path);
    try {
      InputStream stream = new ClassPathResource(path).getInputStream();
      return parseBytes(stream.readAllBytes());
    } catch (Exception e) {
      throw new RuntimeException("MurCsvReader: cannot read classpath resource: " + path, e);
    }
  }

  private List<String[]> parseBytes(byte[] bytes) {
    List<String[]> rows = new ArrayList<>();
    String content = new String(bytes, UTF8);

    // Strip UTF-8 BOM if present (EF BB BF)
    if (content.startsWith("\uFEFF")) {
      content = content.substring(1);
    }

    String[] lines = content.split("\\r?\\n");
    boolean headerSkipped = false;

    for (String line : lines) {
      if (line.isBlank()) {
        continue;
      }
      if (!headerSkipped) {
        headerSkipped = true;
        continue;
      }
      rows.add(line.split(SEPARATOR, -1));
    }

    return rows;
  }
}