-- =============================================================
-- V4 - Create statistica_immatricolati_classe table
--
-- Maps to: StatisticaImmatricolatiClasse.java
-- Data source: MUR/USTAT CKAN API — dataset "immatricolati"
--   File: 17_immatricolatixclasse (national aggregate, no university detail)
-- Refresh frequency: once a year (October 15th)
-- Upsert key: (anno, classe_numero)
-- Historical coverage: from 1998/1999
-- =============================================================

CREATE TABLE statistica_immatricolati_classe
(
    id            UUID         NOT NULL DEFAULT gen_random_uuid(),
    anno          VARCHAR(9)   NOT NULL,
    classe_numero VARCHAR(20)  NOT NULL,
    classe_nome   VARCHAR(255) NOT NULL,
    tot_m         INTEGER      NOT NULL DEFAULT 0,
    tot_f         INTEGER      NOT NULL DEFAULT 0,
    tot           INTEGER      NOT NULL DEFAULT 0,
    fetched_at    TIMESTAMP    NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,

    CONSTRAINT pk_statistica_immatricolati_classe PRIMARY KEY (id),
    CONSTRAINT uq_statistica_immatricolati_classe UNIQUE (anno, classe_numero)
);

CREATE INDEX idx_imm_classe_anno   ON statistica_immatricolati_classe (anno);
CREATE INDEX idx_imm_classe_numero ON statistica_immatricolati_classe (classe_numero);

COMMENT ON TABLE statistica_immatricolati_classe IS 'First-year enrolled students per degree class, national aggregate. Source: MUR/USTAT, refreshed annually. Historical data from 1998/1999.';
COMMENT ON COLUMN statistica_immatricolati_classe.anno IS 'Academic year in format "2025/2026".';
COMMENT ON COLUMN statistica_immatricolati_classe.classe_numero IS 'Degree class code (e.g. L-31, LMG/01). Includes both pre-reform and post-reform codes.';
COMMENT ON COLUMN statistica_immatricolati_classe.fetched_at IS 'Timestamp of the last successful fetch/upsert by the batch job.';
