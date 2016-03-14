package com.tealium.digitalvelocity.event;

import com.tealium.digitalvelocity.util.Util;

/**
 * Use to update key-values added to every dispatch.
 */
public class TrackUpdateEvent {
    private final String key;
    private final String value;

    public TrackUpdateEvent(String key, String value) {
        if (Util.isEmptyOrNull(this.key = key)) {
            throw new IllegalArgumentException();
        }
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
