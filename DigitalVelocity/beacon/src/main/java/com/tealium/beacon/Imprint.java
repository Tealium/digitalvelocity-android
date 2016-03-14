package com.tealium.beacon;

import com.estimote.sdk.Beacon;

final class Imprint {

    private final String id;
    private int rssi;
    private long updatedTimestamp;
    private long beganTimestamp;

    public Imprint(Beacon beacon, long now) {
        this.id = beacon.getProximityUUID() + "." + beacon.getMajor() + "." + beacon.getMinor();
        this.rssi = beacon.getRssi();
        this.beganTimestamp = now;
        this.updatedTimestamp = now;
    }

    public String getId() {
        return this.id;
    }

    public void update(int rssi, long now) {
        this.rssi = rssi;
        this.updatedTimestamp = now;
    }

    public int getRssi() {
        return rssi;
    }

    public long getBeganTimestamp() {
        return beganTimestamp;
    }

    public long getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Imprint)) {
            return false;
        }

        Imprint other = (Imprint) o;
        return this.id.equals(other.id);
    }
}
