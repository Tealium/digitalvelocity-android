package com.tealium.digitalvelocity.event;

import android.text.TextUtils;


public final class TraceUpdateEvent {

    private final String mTraceId;

    private TraceUpdateEvent(String traceId) {
        mTraceId = TextUtils.isEmpty(traceId) ? null : traceId;
    }

    public String getTraceId() {
        return mTraceId;
    }

    public static TraceUpdateEvent createLeaveTraceEvent() {
        return new TraceUpdateEvent(null);
    }

    public static TraceUpdateEvent createJoinTraceEvent(String newTraceId) {
        return new TraceUpdateEvent(newTraceId);
    }
}
