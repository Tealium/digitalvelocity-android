package com.tealium.digitalvelocity.parse;

import com.tealium.digitalvelocity.data.gson.Category;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

final class SyncData {

    private final Map<String, Category> categories;
    private boolean categoriesLoaded;
    private boolean isRequestMade;
    private JSONArray agendaData;
    private JSONArray sponsorData;

    SyncData() {
        this.categories = new HashMap<>();
    }

    void loadCategories(JSONArray array) throws JSONException {

        if (this.categoriesLoaded) {
            return;
        }

        Category category;
        for (int i = 0; i < array.length(); i++) {
            category = new Category(array.getJSONObject(i));
            categories.put(category.getId(), category);
        }

        this.categoriesLoaded = true;
    }

    Map<String, Category> getCategories() {
        return categories;
    }

    boolean isAgendaReady() {
        return this.categoriesLoaded && this.agendaData != null;
    }

    boolean isSponsorReady() {
        return this.categoriesLoaded && this.sponsorData != null;
    }

    public boolean isRequestMade() {
        return isRequestMade;
    }

    public void setRequestMade(boolean isRequestMade) {
        this.isRequestMade = isRequestMade;
    }

    JSONArray getAgendaData() {
        return agendaData;
    }

    JSONArray getSponsorData() {
        return sponsorData;
    }

    void setAgendaData(JSONArray agendaData) {
        this.agendaData = agendaData;
    }

    void setSponsorData(JSONArray sponsorData) {
        this.sponsorData = sponsorData;
    }

    void reset() {
        this.categoriesLoaded = false;
        this.isRequestMade = false;
        this.categories.clear();
        this.agendaData = null;
        this.sponsorData = null;
    }
}
