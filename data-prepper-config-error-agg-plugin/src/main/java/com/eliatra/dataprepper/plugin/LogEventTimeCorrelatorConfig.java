package com.eliatra.dataprepper.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

public class LogEventTimeCorrelatorConfig {

    @JsonProperty("group_ref_key_prefix")
    String groupRefKeyPrefix;

    @JsonProperty("start_key")
    String startKey;

    @JsonProperty("start_value")
    String startValue;

    @JsonProperty(value = "group_duration", defaultValue = "5s")
    Duration groupDuration = Duration.ofSeconds(5);

    @JsonProperty(value = "timestamp_key", defaultValue = "timestamp")
    String timestampKey;

    public String getStartKey() {
        return startKey;
    }

    public void setStartKey(String startKey) {
        this.startKey = startKey;
    }

    public String getStartValue() {
        return startValue;
    }

    public void setStartValue(String startValue) {
        this.startValue = startValue;
    }

    public Duration getGroupDuration() {
        return groupDuration;
    }

    public void setGroupDuration(Duration groupDuration) {
        this.groupDuration = groupDuration;
    }

    public String getTimestampKey() {
        return timestampKey;
    }

    public void setTimestampKey(String timestampKey) {
        this.timestampKey = timestampKey;
    }

    public String getGroupRefKeyPrefix() {
        return groupRefKeyPrefix;
    }

    public void setGroupRefKeyPrefix(String groupRefKeyPrefix) {
        this.groupRefKeyPrefix = groupRefKeyPrefix;
    }
}
