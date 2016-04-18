package com.tealium.digitalvelocity.event;

import java.util.HashMap;
import java.util.Map;

public final class TrackEvent {

    public static final String TYPE_VIEW = "view";
    public static final String TYPE_EVENT = "link";

    private final String mType;
    private final Map<String, Object> mData;

    private TrackEvent(String type) {
        mType = type;
        mData = new HashMap<>();
    }

    public static TrackEvent createViewTrackEvent() {
        return new TrackEvent(TYPE_VIEW);
    }

    public static TrackEvent createLinkTrackEvent() {
        return new TrackEvent(TYPE_EVENT);
    }

    public String getType() {
        return mType;
    }

    public Map<String, Object> getData() {
        return mData;
    }

    public TrackEvent add(String key, String value) {
        mData.put(key, value);
        return this;
    }
}
