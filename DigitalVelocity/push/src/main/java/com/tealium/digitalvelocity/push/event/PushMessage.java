package com.tealium.digitalvelocity.push.event;

public class PushMessage {
    private final String message;
    private final long timestamp;

    public PushMessage(String message) {
        if ((this.message = message) == null || this.message.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
