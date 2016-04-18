package com.tealium.digitalvelocity.data.gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Question extends ParseItem {

    private final String title;
    private final List<String> answers;

    public Question(JSONObject o) throws JSONException {
        super(o);
        title = o.getString("title");
        answers = Collections.unmodifiableList(parseAnswers(o.optJSONArray("answers")));
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public String getTitle() {
        return title;
    }

    public List<String> getAnswers() {
        return answers;
    }

    private static List<String> parseAnswers(JSONArray questionIds) throws JSONException {
        final int size = questionIds == null ? 0 : questionIds.length();
        final List<String> ids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ids.add(questionIds.get(i).toString());
        }
        return ids;
    }
}
