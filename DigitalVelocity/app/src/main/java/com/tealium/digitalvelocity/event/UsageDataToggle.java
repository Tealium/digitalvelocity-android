package com.tealium.digitalvelocity.event;

public final class UsageDataToggle {

    private final boolean isEnabled;

    public UsageDataToggle(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
