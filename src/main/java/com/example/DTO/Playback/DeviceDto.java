package com.example.DTO.Playback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceDto {

    private String id;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_private_session")
    private Boolean isPrivateSession;

    @JsonProperty("is_restricted")
    private Boolean isRestricted;

    private String name;
    private String type;

    @JsonProperty("volume_percent")
    private Integer volumePercent;

    @JsonProperty("supports_volume")
    private Boolean supportsVolume;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsPrivateSession() {
        return isPrivateSession;
    }

    public void setIsPrivateSession(Boolean isPrivateSession) {
        this.isPrivateSession = isPrivateSession;
    }

    public Boolean getIsRestricted() {
        return isRestricted;
    }

    public void setIsRestricted(Boolean isRestricted) {
        this.isRestricted = isRestricted;
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

    public Integer getVolumePercent() {
        return volumePercent;
    }

    public void setVolumePercent(Integer volumePercent) {
        this.volumePercent = volumePercent;
    }

    public Boolean getSupportsVolume() {
        return supportsVolume;
    }

    public void setSupportsVolume(Boolean supportsVolume) {
        this.supportsVolume = supportsVolume;
    }
}
