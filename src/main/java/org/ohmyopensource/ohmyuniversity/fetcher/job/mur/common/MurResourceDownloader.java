package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common;

import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Shared byte-level downloader for MUR open data resources, with fallback to a bundled classpath
 * file when the download fails.
 * <p>
 * Extracted out of {@link MurCsvReader} so any future format-specific reader (XLSX, TSV, JSON, ...)
 * can reuse the same HTTP client, buffer configuration and fallback strategy instead of duplicating
 * it.
 */
@Component
public class MurResourceDownloader {

  private static final Logger log = LoggerFactory.getLogger(MurResourceDownloader.class);
  private static final int MAX_BUFFER_BYTES = 200 * 1024 * 1024;

  private final WebClient webClient;

  public MurResourceDownloader() {
    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(config -> config.defaultCodecs().maxInMemorySize(MAX_BUFFER_BYTES))
        .build();

    this.webClient = WebClient.builder()
        .exchangeStrategies(strategies)
        .build();
  }

  /**
   * Downloads raw bytes from the given URL, falling back to a classpath resource if the download
   * fails or returns nothing.
   *
   * @param resourceUrl       direct download URL (obtained from CkanClient), may be null
   * @param classpathFallback classpath-relative path to the bundled fallback file
   * @return raw bytes, either downloaded or read from classpath
   */
  public byte[] downloadWithFallback(String resourceUrl, String classpathFallback) {
    if (resourceUrl != null) {
      try {
        log.info("MurResourceDownloader: downloading from {}", resourceUrl);
        byte[] bytes = webClient.get()
            .uri(resourceUrl)
            .retrieve()
            .bodyToMono(byte[].class)
            .onErrorResume(e -> {
              log.warn("MurResourceDownloader: HTTP download failed: {}", e.getMessage());
              return Mono.empty();
            })
            .block();

        if (bytes != null && bytes.length > 0) {
          log.info("MurResourceDownloader: downloaded {} bytes from URL", bytes.length);
          return bytes;
        }
      } catch (Exception e) {
        log.warn(
            "MurResourceDownloader: failed to download from URL, trying classpath fallback: {}",
            e.getMessage());
      }
    }

    return readClasspath(classpathFallback);
  }

  /**
   * Reads raw bytes directly from a bundled classpath resource.
   *
   * @param path classpath-relative path, e.g. "data/mur/13_iscrittixcorso.csv"
   * @return raw bytes
   */
  public byte[] readClasspath(String path) {
    log.info("MurResourceDownloader: reading from classpath:{}", path);
    try (InputStream stream = new ClassPathResource(path).getInputStream()) {
      return stream.readAllBytes();
    } catch (Exception e) {
      throw new RuntimeException("MurResourceDownloader: cannot read classpath resource: " + path,
          e);
    }
  }
}