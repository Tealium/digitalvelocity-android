package com.tealium.beacon.event;

class BaseEvent {

    private final String id;
    private final int rssi;

    BaseEvent(String id, int rssi) {
        if ((this.id = id) == null || this.id.length() == 0) {
            throw new IllegalArgumentException();
        }

        this.rssi = rssi;
    }

    public final String getId() {
        return id;
    }

    public final int getRssi() {
        return rssi;
    }
}
