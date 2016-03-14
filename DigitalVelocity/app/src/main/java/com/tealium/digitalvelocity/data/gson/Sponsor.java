package com.tealium.digitalvelocity.data.gson;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public final class Sponsor extends ParseItem implements Comparable<Sponsor> {
    private String name;
    private String description;
    private String url;
    private String logoUri;
    private Category category;

    public Sponsor(JSONObject o, Category category) throws JSONException {
        super(o);

        this.name = o.getString("title");
        this.description = o.optString("subTitle", null);
        this.url = o.optString("url", null);
        this.category = category;

        JSONObject logo = o.optJSONObject("imageData");
        this.logoUri = logo == null ? null : logo.getString("url");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getLogoUri() {
        return logoUri;
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "Sponsor{" +
                "id='" + this.getId() + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", category=" + category +
                '}';
    }

    @Override
    public int compareTo(@NonNull Sponsor another) {
        return this.name.compareTo(another.name);
    }
}
