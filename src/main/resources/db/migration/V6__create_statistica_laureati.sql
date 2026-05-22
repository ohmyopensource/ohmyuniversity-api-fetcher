-- =============================================================
-- V6 - Create statistica_laureati table
--
-- Maps to: StatisticaLaureati.java
-- Data source: MUR/USTAT CKAN API — dataset "laureati"
--   Files: 12a_laureati_corso (2001-2009),
--          12b_laureati_corso (2010-2017),
--          12c_laureati_corso (2018-2024)
-- Refresh frequency: once a year (November 1st)
-- Upsert key: (anno_laurea, codice_ateneo, classe_numero, corso_nome)
-- =============================================================

CREATE TABLE statistica_laureati
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    anno_laurea   INTEGER      NOT NULL,
    codice_ateneo VARCHAR(10)  NOT NULL,
    nome_ateneo   VARCHAR(255) NOT NULL,
    classe_numero VARCHAR(20)  NOT NULL,
    corso_nome    VARCHAR(255) NOT NULL,
    tot_m         INTEGER      NOT NULL DEFAULT 0,
    tot_f         INTEGER      NOT NULL DEFAULT 0,
    tot           INTEGER      NOT NULL DEFAULT 0,
    fetched_at    TIMESTAMP    NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,

    CONSTRAINT pk_statistica_laureati PRIMARY KEY (id),
    CONSTRAINT uq_statistica_laureati UNIQUE (anno_laurea, codice_ateneo, classe_numero, corso_nome)
);

CREATE INDEX idx_laureati_anno    ON statistica_laureati (anno_laurea);
CREATE INDEX idx_laureati_classe  ON statistica_laureati (classe_numero);
CREATE INDEX idx_laureati_ateneo  ON statistica_laureati (codice_ateneo);
CREATE INDEX idx_laureati_corso   ON statistica_laureati (corso_nome);

COMMENT ON TABLE statistica_laureati IS 'Graduates per course, university and degree class. Source: MUR/USTAT, refreshed annually.';
COMMENT ON COLUMN statistica_laureati.anno_laurea IS 'Calendar year of graduation (not academic year).';
COMMENT ON COLUMN statistica_laureati.classe_numero IS 'Degree class code (e.g. L-31, LM-18). Pre-reform codes are numeric strings.';
COMMENT ON COLUMN statistica_laureati.tot_m IS 'Total male graduates, aggregated across genders and multiple campuses.';
COMMENT ON COLUMN statistica_laureati.tot_f IS 'Total female graduates, aggregated across genders and multiple campuses.';
COMMENT ON COLUMN statistica_laureati.fetched_at IS 'Timestamp of the last successful fetch/upsert by the batch job.';