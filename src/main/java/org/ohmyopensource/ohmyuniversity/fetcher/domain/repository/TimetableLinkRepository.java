package org.ohmyopensource.ohmyuniversity.fetcher.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.ohmyopensource.ohmyuniversity.fetcher.domain.entity.TimetableLink;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link TimetableLink} entities.
 *
 * <p>Provides query methods used by the timetable scraper job and the
 * {@code TimetableController} to read and upsert timetable PDF links.
 */
public interface TimetableLinkRepository extends JpaRepository<TimetableLink, UUID> {

  /**
   * Returns all active timetable links for the given university.
   *
   * @param universityId the university identifier (e.g. {@code UNIMOL})
   * @return list of active {@link TimetableLink} entries ordered by department
   */
  List<TimetableLink> findByUniversityIdAndIsActiveTrueOrderByDepartmentIdAsc(
      String universityId);

  /**
   * Returns all active timetable links for the given university and department.
   *
   * @param universityId the university identifier
   * @param departmentId the department slug
   * @return list of active {@link TimetableLink} entries for the department
   */
  List<TimetableLink> findByUniversityIdAndDepartmentIdAndIsActiveTrue(
      String universityId, String departmentId);

  /**
   * Returns all active timetable links for the given university and degree type.
   *
   * @param universityId the university identifier
   * @param degreeType   the degree type (e.g. {@code triennali})
   * @return list of active {@link TimetableLink} entries for the degree type
   */
  List<TimetableLink> findByUniversityIdAndDegreeTypeAndIsActiveTrue(
      String universityId, String degreeType);

  /**
   * Looks up a specific timetable link by its upsert key.
   *
   * <p>Used by the scraper job to determine whether to insert a new row or
   * update an existing one.
   *
   * @param universityId       the university identifier
   * @param timetablePageUrl   the stable timetable page URL
   * @param pdfUrl             the direct PDF URL
   * @return an {@link Optional} containing the existing link, or empty if not found
   */
  Optional<TimetableLink> findByUniversityIdAndTimetablePageUrlAndPdfUrl(
      String universityId, String timetablePageUrl, String pdfUrl);
}