package com.example.DTO.Playback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaybackStateResponse {

    private DeviceDto device;

    @JsonProperty("repeat_state")
    private String repeatState;

    @JsonProperty("shuffle_state")
    private Boolean shuffleState;

    private ContextDto context;
    private Long timestamp;

    @JsonProperty("progress_ms")
    private Long progressMs;

    @JsonProperty("is_playing")
    private Boolean isPlaying;

    @JsonProperty("currently_playing_type")
    private String currentlyPlayingType;

    public DeviceDto getDevice() {
        return device;
    }

    public void setDevice(DeviceDto device) {
        this.device = device;
    }

    public String getRepeatState() {
        return repeatState;
    }

    public void setRepeatState(String repeatState) {
        this.repeatState = repeatState;
    }

    public Boolean getShuffleState() {
        return shuffleState;
    }

    public void setShuffleState(Boolean shuffleState) {
        this.shuffleState = shuffleState;
    }

    public ContextDto getContext() {
        return context;
    }

    public void setContext(ContextDto context) {
        this.context = context;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getProgressMs() {
        return progressMs;
    }

    public void setProgressMs(Long progressMs) {
        this.progressMs = progressMs;
    }

    public Boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(Boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public String getCurrentlyPlayingType() {
        return currentlyPlayingType;
    }

    public void setCurrentlyPlayingType(String currentlyPlayingType) {
        this.currentlyPlayingType = currentlyPlayingType;
    }
}
