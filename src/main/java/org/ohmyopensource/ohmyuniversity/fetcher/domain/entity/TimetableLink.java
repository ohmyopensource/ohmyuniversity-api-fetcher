package org.ohmyopensource.ohmyuniversity.fetcher.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a single timetable PDF link discovered on a university website.
 *
 * <p>Each row corresponds to one PDF found on a stable timetable page. The page URL does not
 * change between semesters; only the PDF URL inside it changes. The scraper job visits each
 * timetable page periodically, extracts the current PDF links, and upserts this table.
 *
 * <p>Entries with {@code directPdf = true} are courses that link directly to a PDF without
 * an intermediate timetable page. Their {@code pdfUrl} must be updated manually each semester.
 *
 * <p>Maps to the {@code timetable_link} table created by migration {@code V7}.
 */
@Entity
@Table(name = "timetable_link")
public class TimetableLink {

  /** Internal UUID primary key. */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;

  /**
   * University identifier matching the {@code omu.universities} configuration
   * (e.g. {@code UNIMOL}).
   */
  @Column(name = "university_id", nullable = false, length = 50)
  private String universityId;

  /**
   * Department slug as it appears in the university website URL
   * (e.g. {@code bioscienze-e-territorio}).
   */
  @Column(name = "department_id", nullable = false, length = 100)
  private String departmentId;

  /** Human-readable department name (e.g. {@code Bioscienze e Territorio}). */
  @Column(name = "department_name", nullable = false, length = 255)
  private String departmentName;

  /**
   * Degree type: {@code triennali}, {@code magistrali}, or
   * {@code magistrali_ciclo_unico}.
   */
  @Column(name = "degree_type", nullable = false, length = 50)
  private String degreeType;

  /**
   * Stable URL of the timetable page. This URL does not change between semesters —
   * only the PDF links inside it change.
   */
  @Column(name = "timetable_page_url", nullable = false, columnDefinition = "TEXT")
  private String timetablePageUrl;

  /**
   * Whether the course links directly to a PDF without an intermediate timetable page.
   * When {@code true}, {@code pdfUrl} must be updated manually each semester.
   */
  @Column(name = "direct_pdf", nullable = false)
  private boolean directPdf;

  /**
   * Direct URL to the current timetable PDF. {@code null} for entries with
   * {@code directPdf = true} that are awaiting manual update.
   */
  @Column(name = "pdf_url", columnDefinition = "TEXT")
  private String pdfUrl;

  /**
   * Human-readable label extracted from the PDF card on the timetable page
   * (e.g. {@code PRIMO ANNO}, {@code CV "SOFTWARE TECHNOLOGIES" SEDE DI TERMOLI}).
   */
  @Column(name = "label", length = 255)
  private String label;

  /**
   * Whether the PDF URL returned a successful HTTP response on the last check.
   * Set to {@code false} when the URL returns a non-200 response, hiding the
   * link from the public API until it is updated.
   */
  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  /** Timestamp of the last HTTP HEAD check performed by the scraper job. */
  @Column(name = "last_checked", nullable = false)
  private Instant lastChecked;

  /** Timestamp of the last successful upsert by the batch job. */
  @Column(name = "fetched_at", nullable = false)
  private Instant fetchedAt;

  /** Row creation timestamp, set once on insert. */
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /** Row last-update timestamp, refreshed on every upsert. */
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  // ============ Getters | Setters | Bool ============

  public UUID getId() {
    return id;
  }
  public void setId(UUID id) {
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

  public boolean isDirectPdf() {
    return directPdf;
  }
  public void setDirectPdf(boolean directPdf) {
    this.directPdf = directPdf;
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
    return isActive;
  }
  public void setActive(boolean active) {
    isActive = active;
  }

  public Instant getLastChecked() {
    return lastChecked;
  }
  public void setLastChecked(Instant lastChecked) {
    this.lastChecked = lastChecked;
  }

  public Instant getFetchedAt() {
    return fetchedAt;
  }
  public void setFetchedAt(Instant fetchedAt) {
    this.fetchedAt = fetchedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}