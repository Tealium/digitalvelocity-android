package com.tealium.digitalvelocity.data.gson;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public final class AgendaItem extends ParseItem implements Comparable<AgendaItem> {

    private String title;
    private String subtitle;
    private String description;
    private String url;
    private String imageURL;
    private String locationId;
    private String fontAwesomeValue;
    private Category category;
    private String roomName;
    private int start;
    private int end;

    public AgendaItem(JSONObject o, Category category) throws JSONException {
        super(o);

        this.title = o.getString("title");
        this.category = category;
        this.start = o.getInt("start");
        this.end = o.getInt("end");
        this.subtitle = o.optString("subTitle", null);
        this.locationId = o.optString("locationId", null);
        this.url = o.optString("url", null);
        this.description = o.optString("description", null);
        this.fontAwesomeValue = o.optString("imageFontAwesome", null);
        this.roomName = o.optString("roomName", null);

        JSONObject imageData = o.optJSONObject("imageData");
        if (imageData != null) {
            this.imageURL = imageData.optString("url", null);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTimeLocDescription() {
        return String.format(Locale.ROOT,
                "%d:%02d - %d:%02d%s",
                this.start / 100,
                this.start % 100,
                this.end / 100,
                this.end % 100,
                (this.roomName == null ? "" : " | " + roomName));
    }

    public Category getCategory() {
        return category;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getFontAwesomeValue() {
        return fontAwesomeValue;
    }

    public String getLocationId() {
        return locationId;
    }

    @Override
    public int compareTo(@NonNull AgendaItem another) {
        return this.start - another.start;
    }


}

