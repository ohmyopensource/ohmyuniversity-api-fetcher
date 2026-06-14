package org.ohmyopensource.ohmyuniversity.fetcher.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.TimetableLink;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.repository.TimetableLinkRepository;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.TimetableResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates timetable link operations for the fetcher microservice.
 *
 * <p>Provides two main capabilities:
 *
 * <p>First, query methods used by
 * {@link org.ohmyopensource.ohmyuniversity.fetcher.controller.v1.TimetableController} to expose
 * timetable PDF links to the API gateway.
 *
 * <p>Second, an import method used by the scheduled job to read the cleaned
 * timetable JSON file for a given university and upsert all entries into the
 * {@code timetable_link} table.
 */
@Service
public class TimetableService {

  private static final Logger log = LoggerFactory.getLogger(TimetableService.class);

  private final TimetableLinkRepository repository;
  private final JsonMapper objectMapper;

  // ============ Constructor ============

  /**
   * Creates the timetable service with required dependencies.
   *
   * @param repository   repository for {@link TimetableLink} entities
   * @param jsonMapper Jackson mapper used to parse the timetable JSON resource
   */
  public TimetableService(TimetableLinkRepository repository, JsonMapper jsonMapper) {
    this.repository = repository;
    this.objectMapper = jsonMapper;
  }

  // ============ Class Methods ============

  /**
   * Returns all active timetable links for the given university.
   *
   * @param universityId the university identifier (e.g. {@code UNIMOL})
   * @return list of {@link TimetableResponse} ordered by department
   */
  @Transactional(readOnly = true)
  public List<TimetableResponse> getTimetables(String universityId) {
    return repository
        .findByUniversityIdAndIsActiveTrueOrderByDepartmentIdAsc(universityId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * Returns all active timetable links for the given university and department.
   *
   * @param universityId the university identifier
   * @param departmentId the department slug
   * @return list of {@link TimetableResponse} for the department
   */
  @Transactional(readOnly = true)
  public List<TimetableResponse> getTimetablesByDepartment(
      String universityId, String departmentId) {
    return repository
        .findByUniversityIdAndDepartmentIdAndIsActiveTrue(universityId, departmentId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * Returns all active timetable links for the given university and degree type.
   *
   * @param universityId the university identifier
   * @param degreeType   the degree type (e.g. {@code triennali})
   * @return list of {@link TimetableResponse} for the degree type
   */
  @Transactional(readOnly = true)
  public List<TimetableResponse> getTimetablesByDegreeType(
      String universityId, String degreeType) {
    return repository
        .findByUniversityIdAndDegreeTypeAndIsActiveTrue(universityId, degreeType)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  /**
   * Imports timetable PDF links from the cleaned JSON resource file for the given university.
   *
   * <p>The JSON file is expected at
   * {@code classpath:universities/<universityId>/<universityId>_timetables.json}
   * (case-sensitive, lowercase university identifier).
   *
   * <p>For each PDF entry in the file, the method performs an upsert: if a row
   * already exists for the combination of {@code universityId},
   * {@code timetablePageUrl} and {@code pdfUrl}, it is updated in place;
   * otherwise a new row is inserted.
   *
   * @param universityId the university identifier (e.g. {@code UNIMOL})
   * @throws IllegalArgumentException if the timetable JSON file cannot be found
   *                                  or parsed
   */
  @Transactional
  public void importTimetables(String universityId) {
    String resourcePath = String.format(
        "universities/%s/%s_timetables.json",
        universityId.toLowerCase(),
        universityId.toLowerCase());

    log.info("TimetableService: importing timetables for university={} from={}",
        universityId, resourcePath);

    JsonNode root;
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new IllegalArgumentException(
            "Timetable resource not found: " + resourcePath);
      }
      root = objectMapper.readTree(is);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Failed to parse timetable resource: " + resourcePath, e);
    }

    int inserted = 0;
    int updated = 0;

    for (JsonNode timetable : root.path("timetables")) {
      String departmentId = timetable.path("department_id").asText();
      String departmentName = timetable.path("department_name").asText();
      String degreeType = timetable.path("degree_type").asText();
      String pageUrl = timetable.path("timetable_page_url").asText();
      boolean directPdf = timetable.path("direct_pdf").asBoolean(false);

      for (JsonNode pdf : timetable.path("pdfs")) {
        String pdfUrl = pdf.path("pdf_url").asText();
        String label = pdf.path("label").asText("");
        if (pdfUrl.isBlank()) {
          continue;
        }

        Instant now = Instant.now();
        var existing = repository.findByUniversityIdAndTimetablePageUrlAndPdfUrl(
            universityId, pageUrl, pdfUrl);

        if (existing.isPresent()) {
          TimetableLink link = existing.get();
          link.setLabel(label.isBlank() ? null : label);
          link.setActive(true);
          link.setFetchedAt(now);
          link.setUpdatedAt(now);
          repository.save(link);
          updated++;
        } else {
          TimetableLink link = new TimetableLink();
          link.setUniversityId(universityId);
          link.setDepartmentId(departmentId);
          link.setDepartmentName(departmentName);
          link.setDegreeType(degreeType);
          link.setTimetablePageUrl(pageUrl);
          link.setDirectPdf(directPdf);
          link.setPdfUrl(pdfUrl);
          link.setLabel(label.isBlank() ? null : label);
          link.setActive(true);
          link.setLastChecked(now);
          link.setFetchedAt(now);
          link.setCreatedAt(now);
          link.setUpdatedAt(now);
          repository.save(link);
          inserted++;
        }
      }
    }

    log.info("TimetableService: import complete for university={} inserted={} updated={}",
        universityId, inserted, updated);
  }

  /**
   * Maps a {@link TimetableLink} entity to a {@link TimetableResponse} DTO.
   *
   * @param link the source entity
   * @return the populated response DTO
   */
  private TimetableResponse toResponse(TimetableLink link) {
    TimetableResponse r = new TimetableResponse();
    r.setId(link.getId().toString());
    r.setUniversityId(link.getUniversityId());
    r.setDepartmentId(link.getDepartmentId());
    r.setDepartmentName(link.getDepartmentName());
    r.setDegreeType(link.getDegreeType());
    r.setTimetablePageUrl(link.getTimetablePageUrl());
    r.setPdfUrl(link.getPdfUrl());
    r.setLabel(link.getLabel());
    r.setActive(link.isActive());
    r.setFetchedAt(link.getFetchedAt().toString());
    return r;
  }
}