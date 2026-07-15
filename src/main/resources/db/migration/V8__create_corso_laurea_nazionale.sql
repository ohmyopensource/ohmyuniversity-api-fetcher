-- =============================================================
-- V8 - Create corso_laurea_nazionale table
--
-- Maps to: CorsoLaureaNazionale.java
-- Data source: MUR Open Data — dataset "metadati", resource "Offerta formativa 2010-2024"
--              (https://dati-ustat.mur.gov.it/dataset/metadati, corsidilaurea_2010-2024.csv)
-- Refresh frequency: low — MUR publishes per academic year, not real-time.
--                     Configurable via fetcher.schedule.offerta-formativa.
-- Upsert key: (anno_accademico, ateneo_nome, nome_corso, classe_laurea_cod)
-- =============================================================

CREATE TABLE corso_laurea_nazionale
(
    id                UUID         NOT NULL DEFAULT gen_random_uuid(),
    anno_accademico   INTEGER      NOT NULL,
    ateneo_nome       VARCHAR(255) NOT NULL,
    area              VARCHAR(255),
    gruppo_nome       TEXT,
    classe_laurea_cod VARCHAR(20)  NOT NULL,
    classe_laurea_des VARCHAR(255),
    nome_corso        VARCHAR(255) NOT NULL,
    tipo_corso        VARCHAR(20)  NOT NULL,
    provincia         VARCHAR(100),
    comune            VARCHAR(100),
    accesso           VARCHAR(50),
    didattica         VARCHAR(50),
    fetched_at        TIMESTAMP    NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,

    CONSTRAINT pk_corso_laurea_nazionale PRIMARY KEY (id),
    CONSTRAINT uq_corso_laurea_nazionale UNIQUE (anno_accademico, ateneo_nome, nome_corso,
                                                 classe_laurea_cod)
);

CREATE INDEX idx_corso_laurea_nazionale_classe ON corso_laurea_nazionale (classe_laurea_cod);
CREATE INDEX idx_corso_laurea_nazionale_tipo ON corso_laurea_nazionale (tipo_corso);
CREATE INDEX idx_corso_laurea_nazionale_anno ON corso_laurea_nazionale (anno_accademico);

COMMENT
ON TABLE corso_laurea_nazionale IS 'National degree programs active per academic year, imported from the MUR "Offerta formativa 2010-2024" open dataset. Reference data, not tied to any specific student.';
COMMENT
ON COLUMN corso_laurea_nazionale.anno_accademico IS 'Academic year the program was active in (source column ANNO_VALIDITA).';
COMMENT
ON COLUMN corso_laurea_nazionale.ateneo_nome IS 'University/campus display name as published by MUR (source column NomeOperativo). No separate numeric code is available in this resource.';
COMMENT
ON COLUMN corso_laurea_nazionale.area IS 'Broad disciplinary area (source column Area).';
COMMENT
ON COLUMN corso_laurea_nazionale.gruppo_nome IS 'Disciplinary group within the area (source column Gruppo_Nome). Source file pads this field with trailing whitespace — trimmed on ingestion, TEXT column as a safety margin.';
COMMENT
ON COLUMN corso_laurea_nazionale.classe_laurea_cod IS 'Degree class code (source column NUMERO, e.g. L-1, LM-12).';
COMMENT
ON COLUMN corso_laurea_nazionale.classe_laurea_des IS 'Degree class description (source column DES).';
COMMENT
ON COLUMN corso_laurea_nazionale.nome_corso IS 'Official degree program name (source column NOME_CORSO).';
COMMENT
ON COLUMN corso_laurea_nazionale.tipo_corso IS 'Derived from classe_laurea_cod prefix (L = triennale, LM = magistrale) — not present as a distinct source column.';
COMMENT
ON COLUMN corso_laurea_nazionale.provincia IS 'Province of the campus (source column PROVINCIA).';
COMMENT
ON COLUMN corso_laurea_nazionale.comune IS 'Municipality of the campus (source column COMUNE).';
COMMENT
ON COLUMN corso_laurea_nazionale.accesso IS 'Admission type (source column ACCESSO, e.g. "accesso libero", "locale"). Relevant for future-developments eligibility display.';
COMMENT
ON COLUMN corso_laurea_nazionale.didattica IS 'Teaching mode (source column DIDATTICA).';
COMMENT
ON COLUMN corso_laurea_nazionale.fetched_at IS 'Timestamp of the last successful upsert by the batch job.';
COMMENT
ON COLUMN corso_laurea_nazionale.created_at IS 'Timestamp of first insertion of this row.';
COMMENT
ON COLUMN corso_laurea_nazionale.updated_at IS 'Timestamp of the last update to this row.';