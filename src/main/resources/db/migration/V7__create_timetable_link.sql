-- =============================================================
-- V7 - Create timetable_link table
--
-- Maps to: TimetableLink.java
-- Data source: src/main/resources/universities/<uni_id>/<uni_id>_timetables.json
-- Refresh frequency: nightly (configurable via fetcher.schedule.timetables)
-- Upsert key: (university_id, timetable_page_url, pdf_url)
-- =============================================================

CREATE TABLE timetable_link
(
    id                   UUID         NOT NULL DEFAULT gen_random_uuid(),
    university_id        VARCHAR(50)  NOT NULL,
    department_id        VARCHAR(100) NOT NULL,
    department_name      VARCHAR(255) NOT NULL,
    degree_type          VARCHAR(50)  NOT NULL,
    timetable_page_url   TEXT         NOT NULL,
    direct_pdf           BOOLEAN      NOT NULL DEFAULT FALSE,
    pdf_url              TEXT,
    label                VARCHAR(255),
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    last_checked         TIMESTAMP    NOT NULL,
    fetched_at           TIMESTAMP    NOT NULL,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,

    CONSTRAINT pk_timetable_link PRIMARY KEY (id),
    CONSTRAINT uq_timetable_link UNIQUE (university_id, timetable_page_url, pdf_url)
);

-- =============================================================
-- Indexes
-- =============================================================

CREATE INDEX idx_timetable_link_university  ON timetable_link (university_id);
CREATE INDEX idx_timetable_link_department  ON timetable_link (department_id);
CREATE INDEX idx_timetable_link_degree_type ON timetable_link (degree_type);

-- =============================================================
-- Comments
-- =============================================================

COMMENT ON TABLE timetable_link IS 'Timetable PDF links per university course, scraped periodically from university websites. One row per PDF found on a timetable page.';
COMMENT ON COLUMN timetable_link.university_id IS 'University identifier matching omu.universities config (e.g. UNIMOL).';
COMMENT ON COLUMN timetable_link.department_id IS 'Department slug as it appears in the university website URL (e.g. bioscienze-e-territorio).';
COMMENT ON COLUMN timetable_link.degree_type IS 'Degree type: triennali, magistrali, magistrali_ciclo_unico.';
COMMENT ON COLUMN timetable_link.timetable_page_url IS 'Stable URL of the timetable page. This URL does not change between semesters — only the PDF links inside it change.';
COMMENT ON COLUMN timetable_link.direct_pdf IS 'TRUE when the course links directly to a PDF without an intermediate timetable page (requires manual update each semester).';
COMMENT ON COLUMN timetable_link.pdf_url IS 'Direct URL to the current timetable PDF. NULL for direct_pdf=true entries awaiting manual update.';
COMMENT ON COLUMN timetable_link.label IS 'Human-readable label extracted from the PDF card on the timetable page (e.g. PRIMO ANNO, CV "SOFTWARE TECHNOLOGIES" SEDE DI TERMOLI).';
COMMENT ON COLUMN timetable_link.is_active IS 'FALSE when the PDF URL returned a non-200 response on last_checked — used to hide stale links from the API.';
COMMENT ON COLUMN timetable_link.last_checked IS 'Timestamp of the last HTTP HEAD check performed by the scraper job.';
COMMENT ON COLUMN timetable_link.fetched_at IS 'Timestamp of the last successful upsert by the batch job.';








