package com.tealium.digitalvelocity.data.gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Survey extends ParseItem {

    private final String title;
    private final List<String> questionIds;

    public Survey(JSONObject o) throws JSONException {
        super(o);
        title = o.getString("title");
        questionIds = Collections.unmodifiableList(parseQuestions(o.optJSONArray("questionIds")));
    }

    public String getTitle() {
        return title;
    }

    public List<String> getQuestionIds() {
        return questionIds;
    }

    private static List<String> parseQuestions(JSONArray questionIds) throws JSONException {
        final int size = questionIds == null ? 0 : questionIds.length();
        final List<String> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(questionIds.get(i).toString());
        }
        return ids;
    }
}
