package com.tealium.digitalvelocity.data.gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ParseItem {

    private static final SimpleDateFormat format =
            new SimpleDateFormat("yyyy-LL-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);


    private String id;
    private boolean isVisible;
    private long createdAt;
    private long updatedAt;

    public ParseItem(String id, boolean isVisible, long createdAt, long updatedAt) {
        this.id = id;
        this.isVisible = isVisible;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ParseItem(JSONObject o) throws JSONException {
        this.id = o.getString("objectId");
        this.isVisible = o.optBoolean("visible", true);
        try {
            this.createdAt = format.parse(o.getString("createdAt")).getTime();
            this.updatedAt = format.parse(o.getString("updatedAt")).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public final String getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public final boolean isVisible() {
        return isVisible;
    }

    protected static long extractTimeStamp(JSONObject dateObject) throws JSONException {
        try {
            return format.parse(dateObject.getString("iso")).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
