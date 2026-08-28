package com.example.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultPlaylist {

    private String id;
    private String name;

    @JsonProperty("public")
    private Boolean publicPlaylist;

    private String uri;

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
}