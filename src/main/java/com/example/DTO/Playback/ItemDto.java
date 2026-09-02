package com.example.DTO.Playback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDto {

    private AlbumDto album;

    private List<ArtistDto> artists;

    @JsonProperty("available_markets")
    private List<String> availableMarkets;

    @JsonProperty("disc_number")
    private Integer discNumber;

    @JsonProperty("duration_ms")
    private Integer durationMs;

    private Boolean explicit;

    @JsonProperty("external_ids")
    private ExternalIdsDto externalIds;

    @JsonProperty("external_urls")
    private ExternalUrlsDto externalUrls;

    private String href;
    private String id;

    @JsonProperty("is_playable")
    private Boolean isPlayable;

    private String name;
    private Integer popularity;

    @JsonProperty("preview_url")
    private String previewUrl;

    @JsonProperty("track_number")
    private Integer trackNumber;

    private String type;
    private String uri;

    @JsonProperty("is_local")
    private Boolean isLocal;


    // =========================
    // Getter / Setter
    // =========================

    public AlbumDto getAlbum() {
        return album;
    }

    public void setAlbum(AlbumDto album) {
        this.album = album;
    }

    public List<ArtistDto> getArtists() {
        return artists;
    }

    public void setArtists(List<ArtistDto> artists) {
        this.artists = artists;
    }

    public List<String> getAvailableMarkets() {
        return availableMarkets;
    }

    public void setAvailableMarkets(List<String> availableMarkets) {
        this.availableMarkets = availableMarkets;
    }

    public Integer getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(Integer discNumber) {
        this.discNumber = discNumber;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public Boolean getExplicit() {
        return explicit;
    }

    public void setExplicit(Boolean explicit) {
        this.explicit = explicit;
    }

    public ExternalIdsDto getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(ExternalIdsDto externalIds) {
        this.externalIds = externalIds;
    }

    public ExternalUrlsDto getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(ExternalUrlsDto externalUrls) {
        this.externalUrls = externalUrls;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIsPlayable() {
        return isPlayable;
    }

    public void setIsPlayable(Boolean isPlayable) {
        this.isPlayable = isPlayable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Integer getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(Integer trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Boolean getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(Boolean isLocal) {
        this.isLocal = isLocal;
    }


    // =========================
    // Album
    // =========================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlbumDto {

        @JsonProperty("album_type")
        private String albumType;

        @JsonProperty("total_tracks")
        private Integer totalTracks;

        @JsonProperty("available_markets")
        private List<String> availableMarkets;

        @JsonProperty("external_urls")
        private ExternalUrlsDto externalUrls;

        private String href;
        private String id;
        private List<ImageDto> images;
        private String name;

        @JsonProperty("release_date")
        private String releaseDate;

        @JsonProperty("release_date_precision")
        private String releaseDatePrecision;

        private String type;
        private String uri;

        private List<ArtistDto> artists;

        public String getAlbumType() {
            return albumType;
        }

        public void setAlbumType(String albumType) {
            this.albumType = albumType;
        }

        public Integer getTotalTracks() {
            return totalTracks;
        }

        public void setTotalTracks(Integer totalTracks) {
            this.totalTracks = totalTracks;
        }

        public List<String> getAvailableMarkets() {
            return availableMarkets;
        }

        public void setAvailableMarkets(List<String> availableMarkets) {
            this.availableMarkets = availableMarkets;
        }

        public ExternalUrlsDto getExternalUrls() {
            return externalUrls;
        }

        public void setExternalUrls(ExternalUrlsDto externalUrls) {
            this.externalUrls = externalUrls;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<ImageDto> getImages() {
            return images;
        }

        public void setImages(List<ImageDto> images) {
            this.images = images;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public String getReleaseDatePrecision() {
            return releaseDatePrecision;
        }

        public void setReleaseDatePrecision(String releaseDatePrecision) {
            this.releaseDatePrecision = releaseDatePrecision;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public List<ArtistDto> getArtists() {
            return artists;
        }

        public void setArtists(List<ArtistDto> artists) {
            this.artists = artists;
        }
    }


    // =========================
    // Artist
    // =========================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ArtistDto {

        @JsonProperty("external_urls")
        private ExternalUrlsDto externalUrls;

        private String href;
        private String id;
        private String name;
        private String type;
        private String uri;

        public ExternalUrlsDto getExternalUrls() {
            return externalUrls;
        }

        public void setExternalUrls(ExternalUrlsDto externalUrls) {
            this.externalUrls = externalUrls;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }


    // =========================
    // Image
    // =========================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageDto {

        private String url;
        private Integer height;
        private Integer width;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }
    }


    // =========================
    // External URLs
    // =========================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExternalUrlsDto {

        private String spotify;

        public String getSpotify() {
            return spotify;
        }

        public void setSpotify(String spotify) {
            this.spotify = spotify;
        }
    }


    // =========================
    // External IDs
    // =========================

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExternalIdsDto {

        private String isrc;
        private String ean;
        private String upc;

        public String getIsrc() {
            return isrc;
        }

        public void setIsrc(String isrc) {
            this.isrc = isrc;
        }

        public String getEan() {
            return ean;
        }

        public void setEan(String ean) {
            this.ean = ean;
        }

        public String getUpc() {
            return upc;
        }

        public void setUpc(String upc) {
            this.upc = upc;
        }
    }
}
