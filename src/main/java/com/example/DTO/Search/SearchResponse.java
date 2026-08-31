package com.example.DTO.Search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {

    private PagingResponse<TrackDto> tracks;
    private PagingResponse<ArtistDto> artists;
    private PagingResponse<AlbumDto> albums;
    private PagingResponse<PlaylistDto> playlists;

    public PagingResponse<TrackDto> getTracks() {
        return tracks;
    }

    public void setTracks(PagingResponse<TrackDto> tracks) {
        this.tracks = tracks;
    }

    public PagingResponse<ArtistDto> getArtists() {
        return artists;
    }

    public void setArtists(PagingResponse<ArtistDto> artists) {
        this.artists = artists;
    }

    public PagingResponse<AlbumDto> getAlbums() {
        return albums;
    }

    public void setAlbums(PagingResponse<AlbumDto> albums) {
        this.albums = albums;
    }

    public PagingResponse<PlaylistDto> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(PagingResponse<PlaylistDto> playlists) {
        this.playlists = playlists;
    }
}
