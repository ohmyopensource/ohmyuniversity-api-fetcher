--Estensione per generare UUID in Postgres
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE mur_enrolled_stats (
    id UUID PRIMARY KEY,
    academic_year VARCHAR(10) NOT NULL,
    female_enrolled INTEGER DEFAULT 0,
    male_enrolled INTEGER DEFAULT 0,
    total_enrolled INTEGER DEFAULT 0,
    data_source VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,

    --Indice per una ricerca veloce
    CONSTRAINT uk_academic_year_status UNIQUE (academic_year, status)
    --Unique è utile per impedire che ci siano due record con status "ACTIVE"

);

CREATE INDEX idx_mur_enrolled_stats_academic_year ON mur_enrolled_stats (academic_year);