-- =============================================================
-- V5 - Create statistica_immatricolati_ateneo table
--
-- Maps to: StatisticaImmatricolatiAteneo.java
-- Data source: MUR/USTAT CKAN API — dataset "immatricolati"
--   File: 02_immatricolatixateneo (aggregate per university, no degree class detail)
-- Refresh frequency: once a year (October 15th, same job as V4)
-- Upsert key: (anno, codice_ateneo)
-- Historical coverage: from 1998/1999
-- =============================================================

CREATE TABLE statistica_immatricolati_ateneo
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    anno          VARCHAR(9)   NOT NULL,
    codice_ateneo VARCHAR(10)  NOT NULL,
    nome_ateneo   VARCHAR(255) NOT NULL,
    tot_m         INTEGER      NOT NULL DEFAULT 0,
    tot_f         INTEGER      NOT NULL DEFAULT 0,
    tot           INTEGER      NOT NULL DEFAULT 0,
    fetched_at    TIMESTAMP    NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,

    CONSTRAINT pk_statistica_immatricolati_ateneo PRIMARY KEY (id),
    CONSTRAINT uq_statistica_immatricolati_ateneo UNIQUE (anno, codice_ateneo)
);

CREATE INDEX idx_imm_ateneo_anno   ON statistica_immatricolati_ateneo (anno);
CREATE INDEX idx_imm_ateneo_codice ON statistica_immatricolati_ateneo (codice_ateneo);

COMMENT ON TABLE statistica_immatricolati_ateneo IS 'First-year enrolled students per university, aggregate without degree class detail. Source: MUR/USTAT, refreshed annually. Historical data from 1998/1999.';
COMMENT ON COLUMN statistica_immatricolati_ateneo.anno IS 'Academic year in format "2025/2026".';
COMMENT ON COLUMN statistica_immatricolati_ateneo.codice_ateneo IS 'Numeric MUR university code (e.g. "101" for Torino).';
COMMENT ON COLUMN statistica_immatricolati_ateneo.fetched_at IS 'Timestamp of the last successful fetch/upsert by the batch job.';
