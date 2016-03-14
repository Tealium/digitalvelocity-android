package com.tealium.digitalvelocity.keymanager;

import android.content.Context;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;

public final class KeyManager {

    private static final String KEY_PARSE_APP_ID = "parse.app_id";
    private static final String KEY_PARSE_API_KEY = "parse.api_key";
    private static final String KEY_ESTIMOTE_PROXIMITY_UUID = "estimote.proximity.uuid";
    private static final String KEY_SNOWSHOE_AUTHORITY = "snowshoe.authority";

    private String mParseAppId;
    private String mParseApiKey;
    private String mEstimoteProximityUuid;
    private String mSnowshoeAuthority;

    public KeyManager(Context context) throws IOException {

        JsonReader reader = null;

        try {
            reader = new JsonReader(
                    new InputStreamReader(context.getAssets().open("keys.json")));

            reader.beginObject();
            while (reader.hasNext()) {
                final String name = reader.nextName();

                switch (name) {
                    case KEY_PARSE_APP_ID:
                        mParseAppId = reader.nextString();
                        break;
                    case KEY_PARSE_API_KEY:
                        mParseApiKey = reader.nextString();
                        break;
                    case KEY_ESTIMOTE_PROXIMITY_UUID:
                        mEstimoteProximityUuid = reader.nextString();
                        break;
                    case KEY_SNOWSHOE_AUTHORITY:
                        mSnowshoeAuthority = reader.nextString();
                        break;
                    default:
                        // Read out the value even if it's not used
                        reader.nextString();
                        break;
                }
            }
            reader.endObject();

        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        final boolean isValid = mParseAppId != null &&
                mParseApiKey != null &&
                mEstimoteProximityUuid != null &&
                mSnowshoeAuthority != null;

        if (!isValid) {
            throw new IllegalStateException();
        }
    }

    public String getParseAppId() {
        return mParseAppId;
    }

    public String getParseApiKey() {
        return mParseApiKey;
    }

    public String getEstimoteProximityUuid() {
        return mEstimoteProximityUuid;
    }


    public String getSnowshoeAuthority() {
        return mSnowshoeAuthority;
    }
}
