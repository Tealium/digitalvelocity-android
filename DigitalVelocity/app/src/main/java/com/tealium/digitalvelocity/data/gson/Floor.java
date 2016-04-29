package com.tealium.digitalvelocity.data.gson;


import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public final class Floor extends ParseItem implements Comparable<Floor> {
    private String name;
    private String imageUri;
    private int priority;

    public Floor(JSONObject o) throws JSONException {
        super(o);

        this.name = o.getString("title");
        this.priority = o.optInt("priority", 0);
        this.imageUri = o.getJSONObject("imageData").getString("url");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Floor floor = (Floor) o;

        return this.getId().equals(floor.getId());

    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    public String getName() {
        return name;
    }

    public String getImageUri() {
        return imageUri;
    }

    public static File getFloorsDir(Context context) {
        return new File(context.getFilesDir(), "floor");
    }

    @Override
    public int compareTo(@NonNull Floor another) {
        return this.priority - another.priority;
    }
}

