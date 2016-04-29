package com.tealium.digitalvelocity.data.gson;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class AgendaItem extends ParseItem implements Comparable<AgendaItem> {

    private String mTitle;
    private String mSubtitle;
    private String mDescription;
    private String mUrl;
    private String mImageUrl;
    private String mLocationId;
    private String mFontAwesomeValue;
    private Category mCategory;
    private String mRoomName;
    private long mStart;
    private long mEnd;

    public AgendaItem(JSONObject o, Category category) throws JSONException {
        super(o);

        mStart = extractTimeStamp(o.getJSONObject("startDate"));
        mEnd = extractTimeStamp(o.getJSONObject("endDate"));

        mTitle = o.getString("title");
        mCategory = category;
        mSubtitle = o.optString("subTitle", null);
        mLocationId = o.optString("locationId", null);
        mUrl = o.optString("url", null);
        mDescription = o.optString("description", null);
        mFontAwesomeValue = o.optString("imageFontAwesome", null);
        mRoomName = o.optString("roomName", null);

        JSONObject imageData = o.optJSONObject("imageData");
        if (imageData != null) {
            mImageUrl = imageData.optString("url", null);
        }
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public String getTimeLocDescription() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.ROOT);
        return String.format(Locale.ROOT,
                "%s - %s%s",
                format.format(new Date(mStart)),
                format.format(new Date(mEnd)),
                (mRoomName == null ? "" : " | " + mRoomName));
    }

    public Category getCategory() {
        return mCategory;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageURL() {
        return mImageUrl;
    }

    public String getFontAwesomeValue() {
        return mFontAwesomeValue;
    }

    public String getLocationId() {
        return mLocationId;
    }

    @Override
    public int compareTo(@NonNull AgendaItem another) {
        return (int) (mStart - another.mStart);
    }
}

