package com.tealium.digitalvelocity.agenda;

import com.tealium.digitalvelocity.util.Util;

public final class LocationClickEvent {

    private final String locationId;

    public LocationClickEvent(String locationId) {
        if (Util.isEmptyOrNull(this.locationId = locationId)) {
            throw new IllegalArgumentException();
        }
    }

    public String getLocationId() {
        return locationId;
    }
}
