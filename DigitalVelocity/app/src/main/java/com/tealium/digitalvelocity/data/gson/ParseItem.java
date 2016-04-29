package com.tealium.digitalvelocity.data.gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class ParseItem {

    private static final SimpleDateFormat format =
            new SimpleDateFormat("yyyy-LL-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
    {
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private String mId;
    private boolean mIsVisible;
    private long mCreatedAt;
    private long mUpdatedAt;

    public ParseItem(String id, boolean isVisible, long createdAt, long updatedAt) {
        mId = id;
        mIsVisible = isVisible;
        mCreatedAt = createdAt;
        mUpdatedAt = updatedAt;
    }

    public ParseItem(JSONObject o) throws JSONException {
        mId = o.getString("objectId");
        mIsVisible = o.optBoolean("visible", true);
        try {
            mCreatedAt = format.parse(o.getString("createdAt")).getTime();
            mUpdatedAt = format.parse(o.getString("updatedAt")).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public final String getId() {
        return mId;
    }

    public long getCreatedAt() {
        return mCreatedAt;
    }

    public long getUpdatedAt() {
        return mUpdatedAt;
    }

    public final boolean isVisible() {
        return mIsVisible;
    }

    protected static long extractTimeStamp(JSONObject dateObject) throws JSONException {
        try {
            return format.parse(dateObject.getString("iso")).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
