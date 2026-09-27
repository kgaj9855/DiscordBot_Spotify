CREATE TABLE IF NOT EXISTS track_catalog (
    track_id VARCHAR(64) PRIMARY KEY,
    track_name VARCHAR(512) NOT NULL,
    album_name VARCHAR(512),
    album_url VARCHAR(1024),
    artist_name VARCHAR(512) NOT NULL,
    popularity SMALLINT,
    release_date DATE,
    track_url VARCHAR(1024) NOT NULL,
    duration_ms INTEGER,
    explicit BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_track_catalog_popularity
        CHECK (popularity BETWEEN 0 AND 100),
    CONSTRAINT ck_track_catalog_duration
        CHECK (duration_ms IS NULL OR duration_ms > 0),
    CONSTRAINT uk_track_catalog_track_url
        UNIQUE (track_url)
);

CREATE TABLE IF NOT EXISTS track_catalog_category (
    track_id VARCHAR(64) NOT NULL,
    category VARCHAR(32) NOT NULL,
    source_rank INTEGER NOT NULL,
    PRIMARY KEY (track_id, category),
    CONSTRAINT fk_track_catalog_category_track
        FOREIGN KEY (track_id)
        REFERENCES track_catalog (track_id)
        ON DELETE CASCADE,
    CONSTRAINT ck_track_catalog_category
        CHECK (category IN ('chinese', 'kpop')),
    CONSTRAINT ck_track_catalog_category_rank
        CHECK (source_rank > 0)
);

CREATE INDEX IF NOT EXISTS idx_track_catalog_category_category_rank
    ON track_catalog_category (category, source_rank);

CREATE INDEX IF NOT EXISTS idx_track_catalog_artist_name
    ON track_catalog (artist_name);
