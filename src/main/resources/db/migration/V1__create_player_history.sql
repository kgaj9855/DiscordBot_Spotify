CREATE TABLE IF NOT EXISTS player_history (
    id BIGSERIAL PRIMARY KEY,
    track_id VARCHAR(64) NOT NULL,
    track_name VARCHAR(512) NOT NULL,
    track_uri VARCHAR(256),
    artist_id VARCHAR(64),
    artist_name VARCHAR(512),
    album_id VARCHAR(64),
    album_name VARCHAR(512),
    played_at TIMESTAMP WITH TIME ZONE NOT NULL,
    context_type VARCHAR(64),
    context_uri VARCHAR(256),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_player_history_track_played_at
    ON player_history (track_id, played_at);
