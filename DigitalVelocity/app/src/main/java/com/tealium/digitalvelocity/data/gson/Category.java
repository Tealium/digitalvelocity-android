package com.tealium.digitalvelocity.data.gson;

import android.support.annotation.NonNull;

import com.tealium.digitalvelocity.parse.ParseHelper;

import org.json.JSONException;
import org.json.JSONObject;

public final class Category extends ParseItem implements Comparable<Category> {
    private String name;
    private int priority;
    private Long eventDate;

    public Category(JSONObject o) throws JSONException {
        super(o);

        this.name = o.getString("title");
        this.priority = o.getInt("priority");

        final long extracted = ParseHelper.parseDate(o.optJSONObject("eventDate"), 0);
        if (extracted != 0) {
            this.eventDate = extracted;
        }
    }

    public Category(String id, long createdAt, long updatedAt, String name, int priority) {
        super(id, true, createdAt, updatedAt);
        this.name = name;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public Long getEventDate() {
        return eventDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return this.getId().equals(category.getId());
    }

    @Override
    public int hashCode() {
        int result = this.getId().hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + priority;
        return result;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + this.getId() + '\'' +
                ", name='" + name + '\'' +
                ", priority=" + priority +
                '}';
    }

    @Override
    public int compareTo(@NonNull Category another) {
        return this.priority - another.priority;
    }
}
