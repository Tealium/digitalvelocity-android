package com.tealium.beacon.event;


public class BeaconUpdate extends BaseEvent {
    public BeaconUpdate(String id, int rssi) {
        super(id, rssi);
    }
}
