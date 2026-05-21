package org.ohmyopensource.ohmyuniversity.fetcher.job.mur.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for the public CKAN API exposed by dati-ustat.mur.gov.it.
 *
 * The MUR portal runs CKAN — the standard open data framework used by
 * European governments. The API requires no authentication and its contract
 * is stable by spec.
 *
 * {@link #findCsvUrl} calls {@code /api/3/action/package_show?id={datasetId}}
 * and returns the download URL of the CSV resource whose URL contains the given
 * filename pattern (e.g. "13_iscrittixcorso"), picking the one with the most
 * recent {@code last_modified} date when multiple resources match.
 *
 * Important: the pattern is matched against the resource {@code url} field
 * (which contains the actual filename), NOT the {@code name} field (which is a
 * human-readable label like "Iscritti per corso di studi - a.a. 2019/20-2024/25").
 */
@Component
public class CkanClient {

  private static final Logger log = LoggerFactory.getLogger(CkanClient.class);

  private final WebClient webClient;

  public CkanClient() {
    this.webClient = WebClient.create();
  }

  /**
   * Searches the resource list of a CKAN dataset for a CSV whose download URL
   * contains the given filename pattern (case-insensitive) and returns that URL.
   *
   * <p>When multiple resources match, the one with the most recent
   * {@code last_modified} value is returned.
   *
   * @param ckanBaseUrl   base URL of the CKAN portal, e.g. "https://dati-ustat.mur.gov.it"
   * @param datasetId     CKAN dataset slug, e.g. "iscritti"
   * @param filenamePattern substring to match against the resource download URL,
   *                        e.g. "13_iscrittixcorso" or "13bis_iscrittixcorso"
   * @return direct CSV download URL, or empty if not found or API unreachable
   */
  public Optional<String> findCsvUrl(
      String ckanBaseUrl,
      String datasetId,
      String filenamePattern) {

    String apiUrl = ckanBaseUrl + "/api/3/action/package_show?id=" + datasetId;
    log.info("CkanClient: querying {} for filename pattern '{}'", apiUrl, filenamePattern);

    try {
      CkanPackageResponse response = webClient.get()
          .uri(apiUrl)
          .retrieve()
          .bodyToMono(CkanPackageResponse.class)
          .block();

      if (response == null || !response.success || response.result == null) {
        log.warn("CkanClient: API returned empty or failed response for dataset '{}'", datasetId);
        return Optional.empty();
      }

      List<CkanResource> resources = response.result.resources;
      if (resources == null || resources.isEmpty()) {
        log.warn("CkanClient: no resources found in dataset '{}'", datasetId);
        return Optional.empty();
      }

      // Match against the URL field — it contains the actual filename.
      // The name field is a human-readable label and does not contain the filename.
      Optional<CkanResource> match = resources.stream()
          .filter(r -> r.format != null && r.format.equalsIgnoreCase("CSV"))
          .filter(r -> r.url != null
              && r.url.toLowerCase().contains(filenamePattern.toLowerCase()))
          .max(Comparator.comparing(
              r -> r.lastModified != null ? r.lastModified : "",
              Comparator.naturalOrder()));

      if (match.isEmpty()) {
        log.warn("CkanClient: no CSV resource with URL matching '{}' in dataset '{}'",
            filenamePattern, datasetId);
        return Optional.empty();
      }

      log.info("CkanClient: found resource '{}' -> {}", match.get().name, match.get().url);
      return Optional.ofNullable(match.get().url);

    } catch (Exception e) {
      log.error("CkanClient: failed to query CKAN API for dataset '{}': {}",
          datasetId, e.getMessage());
      return Optional.empty();
    }
  }

  // ================================
  // Internal DTOs for CKAN response
  // ================================

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class CkanPackageResponse {

    @JsonProperty("success")
    boolean success;

    @JsonProperty("result")
    CkanPackageResult result;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class CkanPackageResult {

    @JsonProperty("resources")
    List<CkanResource> resources;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class CkanResource {

    @JsonProperty("name")
    String name;

    @JsonProperty("url")
    String url;

    @JsonProperty("format")
    String format;

    @JsonProperty("last_modified")
    String lastModified;
  }
}