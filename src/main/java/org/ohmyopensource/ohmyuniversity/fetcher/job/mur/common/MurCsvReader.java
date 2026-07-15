package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility for downloading and parsing CSV files from dati-ustat.mur.gov.it.
 * <p>
 * Byte-level downloading and classpath fallback are delegated to {@link MurResourceDownloader};
 * this class only owns CSV-specific parsing (separator, header skipping, BOM stripping).
 */
@Component
public class MurCsvReader {

  private static final Logger log = LoggerFactory.getLogger(MurCsvReader.class);
  private static final String SEPARATOR = ";";
  private static final Charset UTF8 = StandardCharsets.UTF_8;

  private final MurResourceDownloader downloader;

  public MurCsvReader(MurResourceDownloader downloader) {
    this.downloader = downloader;
  }

  /**
   * Downloads the CSV from the given URL and returns all data rows already split, excluding the
   * header row. Falls back to the classpath file if the download fails.
   *
   * @param csvUrl            direct CSV download URL (obtained from CkanClient)
   * @param classpathFallback classpath-relative path, e.g. "data/mur/13_iscrittixcorso.csv"
   * @return list of String[] arrays, one per data row (header excluded)
   */
  public List<String[]> downloadAndParse(String csvUrl, String classpathFallback) {
    byte[] bytes = downloader.downloadWithFallback(csvUrl, classpathFallback);
    List<String[]> rows = parseBytes(bytes);
    log.info("MurCsvReader: parsed {} data rows", rows.size());
    return rows;
  }

  /**
   * Reads multiple CSV files from the classpath in sequence and merges them into a single list.
   * Useful for the iscritti job whose historical series is split across 3 files. The header is
   * skipped in every file independently.
   *
   * @param classpathPaths list of classpath paths, read in the order provided
   * @return unified list of String[] arrays
   */
  public List<String[]> readMultipleFromClasspath(List<String> classpathPaths) {
    List<String[]> all = new ArrayList<>();
    for (String path : classpathPaths) {
      List<String[]> rows = parseBytes(downloader.readClasspath(path));
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

  private List<String[]> parseBytes(byte[] bytes) {
    List<String[]> rows = new ArrayList<>();
    String content = decode(bytes);

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

  /**
   * Decodes raw bytes as UTF-8 and falls back to Windows-1252 if the result contains replacement
   * characters (U+FFFD) — the tell-tale sign of a source file encoded in Windows-1252/ISO-8859-1
   * (common for MUR CSV exports, e.g. accented letters in "Offerta formativa") being force-read as
   * UTF-8. Windows-1252 never fails to decode any byte sequence, so this fallback is always safe as
   * a last resort.
   */
  private String decode(byte[] bytes) {
    String utf8 = new String(bytes, UTF8);
    if (utf8.indexOf('\uFFFD') < 0) {
      return utf8;
    }
    log.warn("MurCsvReader: UTF-8 decoding produced replacement characters, "
        + "retrying with Windows-1252");
    return new String(bytes, Charset.forName("windows-1252"));
  }
}