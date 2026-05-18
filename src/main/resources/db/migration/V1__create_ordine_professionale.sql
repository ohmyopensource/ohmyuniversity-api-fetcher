-- =============================================================
-- V1 - Create ordine_professionale table
--
-- Maps to: OrdineProfessionale.java
-- Data source: src/main/resources/data/ordini-professionali.json
-- Refresh frequency: twice a year (1 Feb and 1 Aug)
-- =============================================================

CREATE TABLE ordine_professionale
(
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    codice              VARCHAR     NOT NULL,
    nome                VARCHAR     NOT NULL,
    categoria           VARCHAR     NOT NULL,
    organismo_nazionale VARCHAR     NOT NULL,
    sito_ufficiale      VARCHAR,
    requisiti_laurea    TEXT[]      NOT NULL DEFAULT '{}',
    requisiti_note      VARCHAR(1000),
    ministero_vigilanza VARCHAR,
    aggiornato_al       DATE,
    fetched_at          TIMESTAMP   NOT NULL,
    created_at          TIMESTAMP   NOT NULL,
    updated_at          TIMESTAMP   NOT NULL,

    CONSTRAINT pk_ordine_professionale PRIMARY KEY (id),
    CONSTRAINT uq_ordine_professionale_codice UNIQUE (codice),
    CONSTRAINT chk_ordine_categoria CHECK (
        categoria IN (
                      'LEGALE', 'SANITARIO', 'TECNICO_SCIENTIFICO',
                      'ECONOMICO', 'SOCIALE', 'COMUNICAZIONE'
            )
        )
);

CREATE INDEX idx_ordine_professionale_categoria ON ordine_professionale (categoria);

COMMENT ON TABLE ordine_professionale IS 'Italian professional orders. Data loaded from static JSON, refreshed twice a year.';
COMMENT ON COLUMN ordine_professionale.codice IS 'Unique business key used for upsert (e.g. AVVOCATI, MEDICI_CHIRURGHI).';
COMMENT ON COLUMN ordine_professionale.requisiti_laurea IS 'Required degree class codes (e.g. LMG/01, LM-41). Empty array = no specific degree required.';
COMMENT ON COLUMN ordine_professionale.fetched_at IS 'Timestamp of the last successful fetch/upsert by the batch job.';