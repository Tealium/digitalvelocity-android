package com.tealium.digitalvelocity.parse;

import com.tealium.digitalvelocity.util.Constant;

/**
* Created by chadhartman on 3/20/15.
*/
enum Table {
    Config(Constant.SP.KEY_LAST_SYNC_CONFIG),
    Company(Constant.SP.KEY_LAST_SYNC_COMPANY),
    Category(Constant.SP.KEY_LAST_SYNC_CATEGORY),
    Location(Constant.SP.KEY_LAST_SYNC_LOCATION),
    Event(Constant.SP.KEY_LAST_SYNC_EVENT);

    private final String spKey;
    private long lastSyncTS;

    Table(String spKey) {
        this.spKey = spKey;
    }

    public String getSPKey() {
        return spKey;
    }

    public long getLastSyncTS() {
        return lastSyncTS;
    }

    public void setLastSyncTS(long lastSyncTS) {
        this.lastSyncTS = lastSyncTS;
    }
}
