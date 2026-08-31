package com.example.DTO.Search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaylistDto {

    private String id;
    private String name;
    private String description;
    private boolean collaborative;

    @JsonProperty("public")
    private Boolean publicPlaylist;

    private String uri;

    @JsonProperty("external_urls")
    private ExternalUrlsDto externalUrls;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCollaborative() {
        return collaborative;
    }

    public void setCollaborative(boolean collaborative) {
        this.collaborative = collaborative;
    }

    public Boolean getPublicPlaylist() {
        return publicPlaylist;
    }

    public void setPublicPlaylist(Boolean publicPlaylist) {
        this.publicPlaylist = publicPlaylist;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public ExternalUrlsDto getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(ExternalUrlsDto externalUrls) {
        this.externalUrls = externalUrls;
    }
}
