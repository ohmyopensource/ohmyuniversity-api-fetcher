-- =============================================================
-- V3 - Create statistica_iscritti table
--
-- Maps to: StatisticaIscritti.java
-- Data source: MUR/USTAT CKAN API — dataset "iscritti"
--   Files: 13ter_iscrittixcorso (2010-2016),
--          13bis_iscrittixcorso (2016-2019),
--          13_iscrittixcorso    (2019-2025)
-- Refresh frequency: once a year (October 1st)
-- Upsert key: (anno, codice_ateneo, classe_numero, corso_nome)
-- =============================================================

CREATE TABLE statistica_iscritti
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    anno          VARCHAR(9)   NOT NULL,
    codice_ateneo VARCHAR(10)  NOT NULL,
    nome_ateneo   VARCHAR(255) NOT NULL,
    classe_numero VARCHAR(20)  NOT NULL,
    corso_nome    VARCHAR(255) NOT NULL,
    tot_m         INTEGER      NOT NULL DEFAULT 0,
    tot_f         INTEGER      NOT NULL DEFAULT 0,
    tot           INTEGER      NOT NULL DEFAULT 0,
    pre_riforma   BOOLEAN      NOT NULL DEFAULT FALSE,
    fetched_at    TIMESTAMP    NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,

    CONSTRAINT pk_statistica_iscritti PRIMARY KEY (id),
    CONSTRAINT uq_statistica_iscritti UNIQUE (anno, codice_ateneo, classe_numero, corso_nome)
);

CREATE INDEX idx_iscritti_anno    ON statistica_iscritti (anno);
CREATE INDEX idx_iscritti_classe  ON statistica_iscritti (classe_numero);
CREATE INDEX idx_iscritti_ateneo  ON statistica_iscritti (codice_ateneo);
CREATE INDEX idx_iscritti_corso   ON statistica_iscritti (corso_nome);

COMMENT ON TABLE statistica_iscritti IS 'Enrolled students per course, university and degree class. Source: MUR/USTAT, refreshed annually.';
COMMENT ON COLUMN statistica_iscritti.anno IS 'Academic year in format "2024/2025".';
COMMENT ON COLUMN statistica_iscritti.classe_numero IS 'Degree class code (e.g. L-31, LM-18, LMG/01). Value "99999" indicates pre-reform courses without an assigned class.';
COMMENT ON COLUMN statistica_iscritti.pre_riforma IS 'TRUE when classe_numero is "99999" — pre-reform DM 509/99 courses without an assigned degree class.';
COMMENT ON COLUMN statistica_iscritti.tot_m IS 'Total male enrolled students, aggregated across genders and multiple campuses.';
COMMENT ON COLUMN statistica_iscritti.tot_f IS 'Total female enrolled students, aggregated across genders and multiple campuses.';
COMMENT ON COLUMN statistica_iscritti.fetched_at IS 'Timestamp of the last successful fetch/upsert by the batch job.';