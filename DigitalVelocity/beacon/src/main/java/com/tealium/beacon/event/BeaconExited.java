package com.tealium.beacon.event;

public final class BeaconExited extends BaseEvent {

    private final boolean didTimeout;

    public BeaconExited(String id, int rssi, boolean didTimeout) {
        super(id, rssi);
        this.didTimeout = didTimeout;
    }

    public boolean didTimeout() {
        return didTimeout;
    }
}
