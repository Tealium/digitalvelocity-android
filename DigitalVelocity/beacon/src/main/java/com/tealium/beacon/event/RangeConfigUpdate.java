package com.tealium.beacon.event;


public final class RangeConfigUpdate {
    private final int startHour;
    private final int stopHour;
    private final long startDate;
    private final long stopDate;

    public RangeConfigUpdate(int startHour, long startDate, int stopHour, long stopDate) {
        this.startHour = startHour;
        this.stopHour = stopHour;
        this.startDate = startDate;
        this.stopDate = stopDate;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStopHour() {
        return stopHour;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getStopDate() {
        return stopDate;
    }
}
