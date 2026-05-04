CREATE TABLE mur_enrolled_stats (
                                    id               BIGSERIAL PRIMARY KEY,
                                    academic_year    VARCHAR(9)   NOT NULL UNIQUE,
                                    enrolled_female  INTEGER      NOT NULL,
                                    enrolled_male    INTEGER      NOT NULL,
                                    enrolled_total   INTEGER      NOT NULL,
                                    data_source      VARCHAR(255),
                                    notes            VARCHAR(500),
                                    fetched_at       TIMESTAMP    NOT NULL
);