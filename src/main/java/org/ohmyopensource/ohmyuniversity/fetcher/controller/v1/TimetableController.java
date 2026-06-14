package org.ohmyopensource.ohmyuniversity.fetcher.controller.v1;

import java.util.List;
import org.ohmyopensource.ohmyuniversity.fetcher.dto.TimetableResponse;
import org.ohmyopensource.ohmyuniversity.fetcher.service.TimetableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing timetable PDF links for university courses.
 *
 * <p>All endpoints are internal and served at {@code /api/v1/fetcher/timetables}.
 * The API gateway rewrites the public path {@code /v1/fetcher/**} to the internal
 * path {@code /api/v1/fetcher/**} before forwarding to this service.
 *
 * <p>Covers FR-1.3.3 — Lesson timetable scraping.
 */
@RestController
@RequestMapping("/api/v1/fetcher/timetables")
public class TimetableController {

  private static final Logger log = LoggerFactory.getLogger(TimetableController.class);

  private final TimetableService timetableService;

  // ============ Constructor ============

  /**
   * Creates the controller with the required service dependency.
   *
   * @param timetableService service handling timetable query and import logic
   */
  public TimetableController(TimetableService timetableService) {
    this.timetableService = timetableService;
  }

  // ============ Class Methods ============

  /**
   * Returns active timetable PDF links for a university, optionally filtered by
   * department or degree type.
   *
   * <p>When neither {@code departmentId} nor {@code degreeType} is provided, all
   * active links for the university are returned. When both are provided,
   * {@code departmentId} takes precedence.
   *
   * @param universityId the university identifier (e.g. {@code UNIMOL}); required
   * @param departmentId optional department slug filter
   *                     (e.g. {@code bioscienze-e-territorio})
   * @param degreeType   optional degree type filter
   *                     (e.g. {@code triennali}, {@code magistrali})
   * @return {@code 200 OK} with a list of {@link TimetableResponse} entries
   */
  @GetMapping
  public ResponseEntity<List<TimetableResponse>> getTimetables(
      @RequestParam String universityId,
      @RequestParam(required = false) String departmentId,
      @RequestParam(required = false) String degreeType) {

    log.debug("TimetableController: GET timetables universityId={} departmentId={} degreeType={}",
        universityId, departmentId, degreeType);

    List<TimetableResponse> result;

    if (departmentId != null) {
      result = timetableService.getTimetablesByDepartment(universityId, departmentId);
    } else if (degreeType != null) {
      result = timetableService.getTimetablesByDegreeType(universityId, degreeType);
    } else {
      result = timetableService.getTimetables(universityId);
    }

    return ResponseEntity.ok(result);
  }
}