package com.tealium.digitalvelocity.data.gson;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public final class Coordinates extends ParseItem implements Comparable<Coordinates> {
    private String name;
    private double latitude;
    private double longitude;
    private int priority;

    public Coordinates(JSONObject o) throws JSONException {
        super(o);

        this.name = o.getString("title");
        this.latitude = o.getDouble("latitude");
        this.longitude = o.getDouble("longitude");
        this.priority = o.getInt("priority");
    }

    public String getName() {
        return name;
    }

    public LatLng toLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Coordinates that = (Coordinates) o;

        return this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull Coordinates another) {
        return this.priority = another.priority;
    }
}
