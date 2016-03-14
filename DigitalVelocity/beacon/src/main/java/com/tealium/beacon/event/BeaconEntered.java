package com.tealium.beacon.event;

public final class BeaconEntered extends BaseEvent {
    public BeaconEntered(String id, int rssi) {
        super(id, rssi);
    }
}
