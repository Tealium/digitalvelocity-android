package com.tealium.digitalvelocity.parse;

import com.tealium.digitalvelocity.util.Constant;


enum Table {
    Installation(null),
    Attendee(Constant.SP.KEY_LAST_SYNC_ATTENDEE),
    Config(Constant.SP.KEY_LAST_SYNC_CONFIG),
    Company(Constant.SP.KEY_LAST_SYNC_COMPANY),
    Category(Constant.SP.KEY_LAST_SYNC_CATEGORY),
    Location(Constant.SP.KEY_LAST_SYNC_LOCATION),
    Event(Constant.SP.KEY_LAST_SYNC_EVENT),
    Survey(Constant.SP.KEY_LAST_SYNC_SURVEY),
    Question(Constant.SP.KEY_LAST_SYNC_QUESTION);

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
