package com.tealium.digitalvelocity.push.event;

public class PushTokenUpdateEvent {

    private final String mGcmToken;

    public PushTokenUpdateEvent(String gcmToken) {
        mGcmToken = gcmToken;
    }

    public String getGcmToken() {
        return mGcmToken;
    }
}
