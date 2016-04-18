package com.tealium.digitalvelocity.parse;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.tealium.digitalvelocity.BuildConfig;
import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class ParseHelper {

    private ParseHelper() {

    }

    public static final MediaType MEDIA_TYPE_JSON;
    private static final SimpleDateFormat FORMAT;

    public static final class Column {
        private Column() {
        }

        public static final String COLOR_BRIGHTNESS = "vip_color_brightness";
        public static final String COLOR_HUE = "vip_color_hue";
        public static final String COLOR_SATURATION = "vip_color_saturation";
        public static final String ID = "objectId";
        public static final String IMAGE = "vip_image";
        public static final String MUSIC = "vip_music";
        public static final String NAME = "vip_name";
        public static final String VIDEO = "vip_video";
        public static final String VISIBLE = "visible";

    }

    static {
        MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

        FORMAT = new SimpleDateFormat("yyyy-LL-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT);
        FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    static Request createConfigRequest() {
        return createClassRequest("Config",
                "welcomeYear," +
                        "welcomeDescription," +
                        "welcomeSubtitle," +
                        "purge," +
                        "syncRate," +
                        "rssiThreshold," +
                        "startMonitoring," +
                        "stopMonitoring," +
                        "startMonitoringDate," +
                        "stopMonitoringDate," +
                        "enterThreshold," +
                        "exitThreshold," +
                        "scanCycle," +
                        "poiRefreshCycle",
                createWhereStatement(Table.Config));
    }

    static Request createAttendeeRequest(String email) throws JSONException {
        return createClassRequest("Attendee", null,
                new JSONObject().put("email", email).toString());
    }

    static Request createSurveyRequest() {
        return createClassRequest("Survey", "title,questionIds", createWhereStatement(Table.Survey));
    }

    static Request createQuestionRequest() {
        return createClassRequest("Question", "title,answers", createWhereStatement(Table.Question));
    }

    static Request createEventRequest() {
        return createClassRequest("Event",
                "objectId," +
                        "title," +
                        "description," +
                        "url," +
                        "imageData," +
                        "imageFontAwesome," +
                        "start," +
                        "end," +
                        "categoryId," +
                        "locationId," +
                        "subTitle," +
                        "roomName," +
                        "visible",
                createWhereStatement(Table.Event));
    }

    static Request createCompanyRequest() {
        return createClassRequest("Company",
                "objectId,title,subTitle,categoryId,url,imageData,visible,email,emailMessage",
                createWhereStatement(Table.Company));
    }

    static Request createLocationRequest() {
        return createClassRequest(
                "Location",
                "objectId,imageData,title,subTitle,latitude,longitude,priority,visible",
                createWhereStatement(Table.Location));
    }

    static Request createCategoryRequest() {
        return createClassRequest("Category", "objectId,title,eventDate,priority", null);
    }

    static Request createClassRequest(String className, String keys, String where) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("api.parse.com")
                .appendPath("1")
                .appendPath("classes")
                .appendPath(className);


        if (keys != null) {
            builder.appendQueryParameter("keys", keys);
        }

        if (where != null) {
            builder.appendQueryParameter("where", where);
        }

        Request.Builder reqBuilder = new Request.Builder().url(builder.build().toString());

        addAuthHeaders(reqBuilder);


        return reqBuilder.build();
    }

    static Request createPUSHRegistrationRequest(Context context, String senderId, String token) {
        try {

            final Model model = Model.getInstance();

            JSONObject body = new JSONObject()
                    .put("channels", new JSONArray()
                            .put("vid-" + model.getVisitorId())
                            .put("everyone"))
                    .put("email", model.getUserEmail())
                    .put("timeZone", TimeZone.getDefault().getID())
                    .put("deviceType", "android")
                    .put("pushType", "gcm")
                    .put("GCMSenderId", senderId)
                    .put("installationId", Model.getInstance().getUUID())
                    .put("deviceToken", token)
                    .put("appName", context.getString(R.string.app_name))
                    .put("appVersion", Util.getAppVersionName(context))
                    .put("appIdentifier", context.getPackageName());

            if (BuildConfig.DEBUG) {
                body.accumulate("channels", "android-dev");
            }

            Request.Builder request = new Request.Builder()
                    .url(new Uri.Builder()
                            .scheme("https")
                            .authority("api.parse.com")
                            .appendPath("1")
                            .appendPath("installations")
                            .build().toString())
                    .post(RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"),
                            body.toString()));

            addAuthHeaders(request);

            return request.build();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static long parseDate(JSONObject dateObject, long defaultValue) {

        if (dateObject == null || !dateObject.has("iso")) {
            return defaultValue;
        }

        try {
            return parseDate(dateObject.getString("iso"), defaultValue);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static long parseDate(String s, long defaultValue) {
        try {
            return FORMAT.parse(s).getTime();
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    private static void addAuthHeaders(Request.Builder req) {
        final Model model = Model.getInstance();
        req.addHeader("X-Parse-Application-Id", model.getKeyManager().getParseAppId());
        req.addHeader("X-Parse-REST-API-Key", model.getKeyManager().getParseApiKey());
    }

    private static String createWhereStatement(Table table) {

        SharedPreferences sp = Model.getInstance().getSharedPreferences();

        try {
            return new JSONObject()
                    .put("updatedAt", new JSONObject().put("$gte",
                            FORMAT.format(new Date(sp.getLong(table.getSPKey(), 0)))))
                    .toString();
        } catch (JSONException e) {
            return "{}";
        }
    }


}
