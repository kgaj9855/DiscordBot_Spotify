package com.example.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentPlaylist {

    private String href;
    private Integer limit;
    private String next;
    private Integer offset;
    private String previous;
    private Integer total;
    private List<PlaylistItem> items;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<PlaylistItem> getItems() {
        return items;
    }

    public void setItems(List<PlaylistItem> items) {
        this.items = items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlaylistItem {

        private Boolean collaborative;
        private String description;

        @JsonProperty("external_urls")
        private Map<String, String> externalUrls;

        private String href;
        private String id;
        private List<Image> images;
        private String name;
        private Owner owner;

        @JsonProperty("public")
        private Boolean publicPlaylist;

        @JsonProperty("snapshot_id")
        private String snapshotId;

        private ItemSummary items;
        private String type;
        private String uri;

        public Boolean getCollaborative() {
            return collaborative;
        }

        public void setCollaborative(Boolean collaborative) {
            this.collaborative = collaborative;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, String> getExternalUrls() {
            return externalUrls;
        }

        public void setExternalUrls(Map<String, String> externalUrls) {
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

        public List<Image> getImages() {
            return images;
        }

        public void setImages(List<Image> images) {
            this.images = images;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Owner getOwner() {
            return owner;
        }

        public void setOwner(Owner owner) {
            this.owner = owner;
        }

        public Boolean getPublicPlaylist() {
            return publicPlaylist;
        }

        public void setPublicPlaylist(Boolean publicPlaylist) {
            this.publicPlaylist = publicPlaylist;
        }

        public String getSnapshotId() {
            return snapshotId;
        }

        public void setSnapshotId(String snapshotId) {
            this.snapshotId = snapshotId;
        }

        public ItemSummary getItems() {
            return items;
        }

        public void setItems(ItemSummary items) {
            this.items = items;
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {

        @JsonProperty("display_name")
        private String displayName;

        private String id;
        private String href;
        private String type;
        private String uri;

        @JsonProperty("external_urls")
        private Map<String, String> externalUrls;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
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

        public Map<String, String> getExternalUrls() {
            return externalUrls;
        }

        public void setExternalUrls(Map<String, String> externalUrls) {
            this.externalUrls = externalUrls;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemSummary {

        private String href;
        private Integer total;

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }
    }
}