package com.tealium.digitalvelocity.data.gson;

import android.support.annotation.NonNull;

import com.tealium.digitalvelocity.util.Util;
import com.tealium.digitalvelocity.push.event.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

public final class Notification extends ParseItem implements Comparable<Notification> {

    private static final int SRC_TABLE = 1;
    private static final int SRC_PUSH = 2;

    private String text;
    private int source;

    private Notification(String id, boolean isVisible, long createdAt, long updatedAt, String text) {
        super(id, isVisible, createdAt, updatedAt);
        this.text = text;
        this.source = SRC_PUSH;
    }

    public Notification(JSONObject o) throws JSONException {
        super(o);

        this.text = o.getString("title");
        this.source = SRC_TABLE;
    }

    public static Notification createNotification(PushMessage pushMessage) {

        if (Util.isEmptyOrNull(pushMessage.getMessage())) {
            return null;
        }

        return new Notification(
                Long.toString(pushMessage.getTimestamp(), 16) + "",
                true,
                pushMessage.getTimestamp(),
                pushMessage.getTimestamp(),
                pushMessage.getMessage());
    }

    public String getText() {
        return text;
    }

    public boolean isFromParseTable() {
        return this.source == SRC_TABLE;
    }

    public boolean isFromPush() {
        return this.source == SRC_PUSH;
    }

    @Override
    public int compareTo(@NonNull Notification another) {
        long result = another.getUpdatedAt() - this.getUpdatedAt();

        if (result > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        if (result < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return (int) result;
    }
}
