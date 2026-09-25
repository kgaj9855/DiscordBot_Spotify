package com.example.playerhistory.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "player_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_player_history_track_played_at",
                columnNames = {"track_id", "played_at"}))
public class PlayerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "track_id", nullable = false, length = 64)
    private String trackId;

    @Column(name = "track_name", nullable = false, length = 512)
    private String trackName;

    @Column(name = "track_uri", length = 256)
    private String trackUri;

    @Column(name = "artist_id", length = 64)
    private String artistId;

    @Column(name = "artist_name", length = 512)
    private String artistName;

    @Column(name = "album_id", length = 64)
    private String albumId;

    @Column(name = "album_name", length = 512)
    private String albumName;

    @Column(name = "played_at", nullable = false, columnDefinition = "timestamp with time zone")
    private Instant playedAt;

    @Column(name = "context_type", length = 64)
    private String contextType;

    @Column(name = "context_uri", length = 256)
    private String contextUri;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private Instant createdAt;

    protected PlayerHistory() {
    }

    public PlayerHistory(
            String trackId,
            String trackName,
            String trackUri,
            String artistId,
            String artistName,
            String albumId,
            String albumName,
            Instant playedAt,
            String contextType,
            String contextUri,
            Instant createdAt) {
        this.trackId = trackId;
        this.trackName = trackName;
        this.trackUri = trackUri;
        this.artistId = artistId;
        this.artistName = artistName;
        this.albumId = albumId;
        this.albumName = albumName;
        this.playedAt = playedAt;
        this.contextType = contextType;
        this.contextUri = contextUri;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getTrackId() {
        return trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getTrackUri() {
        return trackUri;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public Instant getPlayedAt() {
        return playedAt;
    }

    public String getContextType() {
        return contextType;
    }

    public String getContextUri() {
        return contextUri;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
