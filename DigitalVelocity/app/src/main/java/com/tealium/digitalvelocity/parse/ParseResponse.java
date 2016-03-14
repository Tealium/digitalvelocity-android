package com.tealium.digitalvelocity.parse;

import org.json.JSONObject;

final class ParseResponse {

    private final Table table;
    private final JSONObject data;

    ParseResponse(Table table, JSONObject jsonObject) {
        if ((this.table = table) == null || ((this.data = jsonObject) == null)) {
            throw new IllegalArgumentException();
        }
    }

    public Table getTable() {
        return table;
    }

    public JSONObject getData() {
        return data;
    }
}
