package org.ohmyopensource.ohmyuniversity.fetcher.dto;

/**
 * Response DTO for timetable link endpoints.
 *
 * <p>Returned by {@code GET /api/v1/fetcher/timetables}.
 * Each entry represents one timetable PDF available for a course.
 */
public class TimetableResponse {

  private String id;
  private String universityId;
  private String departmentId;
  private String departmentName;
  private String degreeType;
  private String timetablePageUrl;
  private String pdfUrl;
  private String label;
  private boolean active;
  private String fetchedAt;

  // ============ Getters | Setters | Bool ============

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  public String getUniversityId() {
    return universityId;
  }
  public void setUniversityId(String universityId) {
    this.universityId = universityId;
  }

  public String getDepartmentId() {
    return departmentId;
  }
  public void setDepartmentId(String departmentId) {
    this.departmentId = departmentId;
  }

  public String getDepartmentName() {
    return departmentName;
  }
  public void setDepartmentName(String departmentName) {
    this.departmentName = departmentName;
  }

  public String getDegreeType() {
    return degreeType;
  }
  public void setDegreeType(String degreeType) {
    this.degreeType = degreeType;
  }

  public String getTimetablePageUrl() {
    return timetablePageUrl;
  }
  public void setTimetablePageUrl(String timetablePageUrl) {
    this.timetablePageUrl = timetablePageUrl;
  }

  public String getPdfUrl() {
    return pdfUrl;
  }
  public void setPdfUrl(String pdfUrl) {
    this.pdfUrl = pdfUrl;
  }

  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isActive() {
    return active;
  }
  public void setActive(boolean active) {
    this.active = active;
  }

  public String getFetchedAt() {
    return fetchedAt;
  }
  public void setFetchedAt(String fetchedAt) {
    this.fetchedAt = fetchedAt;
  }
}