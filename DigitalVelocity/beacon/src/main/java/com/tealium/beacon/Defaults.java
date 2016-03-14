package com.tealium.beacon;


public final class Defaults {

    private Defaults() {
    }

    public final static int RSSI_THRESHOLD = Integer.MIN_VALUE;
    public final static long POI_THRESHOLD_ENTER = 5;// in seconds
    public final static long POI_THRESHOLD_EXIT = 10;// in seconds
    public final static long POI_IN_PERIOD = 60;// in seconds
    public final static long POI_SCAN_DELAY_PERIOD = 1;// in seconds
}
