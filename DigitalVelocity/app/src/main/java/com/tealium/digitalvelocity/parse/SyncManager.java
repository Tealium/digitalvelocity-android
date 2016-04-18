package com.tealium.digitalvelocity.parse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tealium.digitalvelocity.BuildConfig;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.TrackingManager;
import com.tealium.digitalvelocity.data.gson.AgendaItem;
import com.tealium.digitalvelocity.data.gson.Category;
import com.tealium.digitalvelocity.data.gson.Coordinates;
import com.tealium.digitalvelocity.data.gson.Floor;
import com.tealium.digitalvelocity.data.gson.Question;
import com.tealium.digitalvelocity.data.gson.Sponsor;
import com.tealium.digitalvelocity.data.gson.Survey;
import com.tealium.digitalvelocity.event.Purge;
import com.tealium.digitalvelocity.event.SaveRequest;
import com.tealium.digitalvelocity.event.SyncCompleteEvent;
import com.tealium.digitalvelocity.event.SyncRequest;
import com.tealium.digitalvelocity.event.TrackUpdateEvent;
import com.tealium.digitalvelocity.push.PushManager;
import com.tealium.digitalvelocity.push.event.PushTokenUpdateEvent;
import com.tealium.digitalvelocity.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public final class SyncManager {

    private final SyncData mSyncData;
    private final Context mContext;

    public SyncManager(Context context) {
        mSyncData = new SyncData();
        mContext = context.getApplicationContext();
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(SyncRequest req) {
        if (!syncConfig()) {
            // In case config synced and nothing else did.
            syncData();
        }

        // Check push

        final Model model = Model.getInstance();
        final String gcmToken = model.getGcmToken();
        final boolean hasGCMToken = gcmToken != null;

        if (model.isParsePushRegistered() && hasGCMToken) {
            if (BuildConfig.DEBUG) {
                Log.d(Constant.TAG, "# Have GCM token & parse is registered.");
            }
            return;
        }

        if (hasGCMToken) {
            if (BuildConfig.DEBUG) {
                Log.d(Constant.TAG, "# Have GCM token, registering with parse.");
            }
            // Only need to register for parse.
            registerParseInBackground(gcmToken);
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(Constant.TAG, "# Fetching GCM token.");
        }

        final boolean generateSuccess = PushManager.generateGcmToken(mContext);

        if (BuildConfig.DEBUG) {
            Log.d(Constant.TAG, generateSuccess ? "# Fetching GCM token." : "# Play services are unavailable.");
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(ParseResponse parseResponse) {
        JSONArray results = parseResponse.getData().optJSONArray("results");
        if (results == null) {

            String data;

            try {
                data = parseResponse.getData().toString(4);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            Log.e(Constant.TAG, "Unexpected Response: " + data, new IllegalArgumentException());
            return;
        }

        try {
            switch (parseResponse.getTable()) {
                case Installation:
                    if (BuildConfig.DEBUG) {
                        Log.d(Constant.TAG, parseResponse.getData().toString(4));
                    }
                    break;
                case Config:
                    processConfigData(results);
                    break;
                case Company:
                    if (results.length() == 0) {
                        if (BuildConfig.DEBUG) Log.i(Constant.TAG, "Received 0 Companies.");
                        updateLastSyncTimestamp(Table.Company);
                        break;
                    }

                    if (!mSyncData.isRequestMade()) {
                        performRequest(ParseHelper.createCategoryRequest(), Table.Category);
                        mSyncData.setRequestMade(true);
                    }

                    mSyncData.setSponsorData(results);

                    if (mSyncData.isSponsorReady()) {
                        processCompanyData();
                    }
                    break;
                case Category:
                    mSyncData.loadCategories(results);

                    if (mSyncData.isAgendaReady()) {
                        processEventData();
                    }

                    if (mSyncData.isSponsorReady()) {
                        processCompanyData();
                    }
                    break;
                case Location:
                    processLocationData(results);
                    break;
                case Survey:
                    processParseItemData(Table.Survey, results);
                    break;
                case Question:
                    processParseItemData(Table.Question, results);
                    break;
                case Event:
                    if (results.length() == 0) {
                        if (BuildConfig.DEBUG) Log.i(Constant.TAG, "Received 0 Events.");
                        updateLastSyncTimestamp(Table.Event);
                        break;
                    }

                    if (!mSyncData.isRequestMade()) {
                        performRequest(ParseHelper.createCategoryRequest(), Table.Category);
                        mSyncData.setRequestMade(true);
                    }

                    mSyncData.setAgendaData(results);

                    if (mSyncData.isAgendaReady()) {
                        processEventData();
                    }
                    break;
                case Attendee:
                    processAttendeeData(results);
                    break;
            }
        } catch (Throwable t) {
            Log.e(Constant.TAG, null, t);
        }

    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(PushTokenUpdateEvent event) {
        registerParseInBackground(event.getGcmToken());
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(TrackUpdateEvent event) {
        // Update if e-mail updated
        if (!TrackingManager.Key.EMAIL.equals(event.getKey())) {
            return;
        }

        try {
            performRequest(ParseHelper.createAttendeeRequest(event.getValue()), Table.Attendee);
        } catch (JSONException e) {
            Log.e(Constant.TAG, e.getMessage(), e);
        }
    }

    private boolean syncConfig() {
        if (readyToSync(
                System.currentTimeMillis(),
                Model.getInstance().getParseSyncRate(),
                Table.Config)) {
            performRequest(ParseHelper.createConfigRequest(), Table.Config);
            return true;
        }

        return false;
    }

    private void syncData() {

        // Reset for incoming.
        mSyncData.reset();

        final Model model = Model.getInstance();
        final long now = System.currentTimeMillis();
        final long syncRate = model.getParseSyncRate();

        if (readyToSync(now, syncRate, Table.Event)) {
            performRequest(ParseHelper.createEventRequest(), Table.Event);
        }

        if (readyToSync(now, syncRate, Table.Location)) {
            performRequest(ParseHelper.createLocationRequest(), Table.Location);
        }

        if (readyToSync(now, syncRate, Table.Company)) {
            performRequest(ParseHelper.createCompanyRequest(), Table.Company);
        }

        if (readyToSync(now, syncRate, Table.Survey)) {
            performRequest(ParseHelper.createSurveyRequest(), Table.Survey);
        }

        if (readyToSync(now, syncRate, Table.Question)) {
            performRequest(ParseHelper.createQuestionRequest(), Table.Question);
        }

        if (readyToSync(now, syncRate, Table.Attendee) && model.getUserEmail() != null) {
            try {
                performRequest(ParseHelper.createAttendeeRequest(model.getUserEmail()), Table.Attendee);
            } catch (JSONException e) {
                Log.e(Constant.TAG, e.getMessage(), e);
            }
        }
    }

    private void processAttendeeData(JSONArray results) throws JSONException {
        /*
        *  [
            {
                "colorBrightness": "50",
                "colorHue": "120",
                "colorSaturation": "100",
                "createdAt": "2016-03-28T21:51:04.169Z",
                "music": "rap",
                "name": "Chad",
                "objectId": "E6AhpFi4sc",
                "updatedAt": "2016-03-31T16:07:47.129Z"
            },
        *
        * */

        if (results.length() == 0) {
            return;
        }

        Model.getInstance().updateVipInfo(results.getJSONObject(0));
        updateLastSyncTimestamp(Table.Attendee);
    }

    private void processConfigData(JSONArray results) throws JSONException {

        Model model = Model.getInstance();
        JSONObject config;
        SharedPreferences.Editor editor = Model.getInstance().getSharedPreferences().edit();

        if (results.length() > 0) {
            model.setConfig(config = results.getJSONObject(0));

            if (config.optBoolean("purge", false)) {
                EventBus.getDefault().post(new Purge());

                editor.remove(Constant.SP.KEY_LAST_SYNC_COMPANY)
                        .remove(Constant.SP.KEY_LAST_SYNC_EVENT)
                        .remove(Constant.SP.KEY_LAST_SYNC_LOCATION);
            }
        }

        final long now = System.currentTimeMillis();
        editor.putLong(Constant.SP.KEY_LAST_SYNC_CONFIG, now).apply();
        Table.Config.setLastSyncTS(now);

        if (BuildConfig.DEBUG) Log.i(Constant.TAG, "Received " +
                results.length() + " Config keys.");

        this.syncData();
    }

    private void processEventData() throws JSONException, ParseException {

        final Model model = Model.getInstance();
        final EventBus bus = EventBus.getDefault();
        JSONObject item;
        Category category;
        AgendaItem newItem;

        for (int i = 0; i < this.mSyncData.getAgendaData().length(); i++) {
            item = this.mSyncData.getAgendaData().getJSONObject(i);
            category = this.mSyncData.getCategories().get(item.getString("categoryId"));
            bus.post(new SaveRequest.AgendaItem(newItem = new AgendaItem(item, category)));
            model.enqueueImageDownload(newItem.getId(), newItem.getImageURL());
        }

        updateLastSyncTimestamp(Table.Event);

        if (BuildConfig.DEBUG) Log.i(Constant.TAG, "Received " +
                this.mSyncData.getAgendaData().length() + " Events.");
        this.mSyncData.setAgendaData(null);
        bus.post(new SyncCompleteEvent.ParseEvent());
    }

    private void processCompanyData() throws JSONException {

        final Model model = Model.getInstance();
        final EventBus bus = EventBus.getDefault();
        JSONObject item;
        Category category;
        Sponsor sponsor;

        for (int i = 0; i < this.mSyncData.getSponsorData().length(); i++) {
            item = this.mSyncData.getSponsorData().getJSONObject(i);
            category = this.mSyncData.getCategories().get(item.getString("categoryId"));
            bus.post(new SaveRequest.Sponsor(sponsor = new Sponsor(item, category)));
            model.enqueueImageDownload(sponsor.getId(), sponsor.getLogoUri());
        }

        updateLastSyncTimestamp(Table.Company);

        if (BuildConfig.DEBUG) Log.i(Constant.TAG, "Received " +
                this.mSyncData.getSponsorData().length() + " Companies.");
        this.mSyncData.setSponsorData(null);
        bus.post(new SyncCompleteEvent.ParseCompany());
    }

    private void processParseItemData(Table table, JSONArray array) throws JSONException {
        final EventBus bus = EventBus.getDefault();


        switch (table) {
            case Survey:
                for (int i = 0; i < array.length(); i++) {
                    bus.post(new SaveRequest.Survey(new Survey(array.getJSONObject(i))));
                }
                break;
            case Question:
                for (int i = 0; i < array.length(); i++) {
                    bus.post(new SaveRequest.Question(new Question(array.getJSONObject(i))));
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }


        updateLastSyncTimestamp(table);
        if (BuildConfig.DEBUG) {
            Log.i(Constant.TAG, "Received " + array.length() + " " + table.name() + "s.");
        }
    }

    private void processLocationData(JSONArray array) throws JSONException {

        final Model model = Model.getInstance();
        final EventBus bus = EventBus.getDefault();
        JSONObject item;
        Floor newFloor;

        for (int i = 0; i < array.length(); i++) {
            item = array.getJSONObject(i);

            if (item.has("latitude") && item.has("longitude")) {
                bus.post(new SaveRequest.Coordinates(new Coordinates(item)));
            } else if (item.has("imageData")) {
                bus.post(new SaveRequest.Floor(newFloor = new Floor(item)));
                model.enqueueImageDownload(newFloor.getId(), newFloor.getImageUri());
            } else {
                Log.e(Constant.TAG, "Ambiguous Location : " + item.toString(4));
            }
        }

        updateLastSyncTimestamp(Table.Location);

        if (BuildConfig.DEBUG) Log.i(Constant.TAG, "Received " + array.length() + " Locations.");
        bus.post(new SyncCompleteEvent.ParseLocation());
    }

    private static void updateLastSyncTimestamp(Table table) {
        final long now = System.currentTimeMillis();
        Model.getInstance().getSharedPreferences()
                .edit().putLong(table.getSPKey(), now)
                .apply();
        table.setLastSyncTS(now);
    }

    private static void performRequest(final Request request, final Table table) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Response res = client.newCall(request).execute();

                    if (BuildConfig.DEBUG) {
                        Log.v(Constant.TAG, request + ", Status: " + res.code());
                    }

                    JSONObject data = new JSONObject(res.body().string());

                    EventBus.getDefault().post(new ParseResponse(table, data));
                } catch (Throwable t) {
                    Log.e(Constant.TAG, "Error loading " + request, t);
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static boolean readyToSync(long now, long syncTimeout, Table table) {
        final long delta = now - table.getLastSyncTS();

        if (delta < syncTimeout) {
            if (BuildConfig.DEBUG) Log.d(Constant.TAG, String.format(
                    Locale.ROOT,
                    "%d minutes until " + table.name() + " refresh.",
                    (syncTimeout - delta) / (60000L)));
            return false;
        }

        return true;
    }

    private void registerParseInBackground(final String gcmToken) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Model model = Model.getInstance();

                    Request request = ParseHelper.createPUSHRegistrationRequest(
                            mContext,
                            model.getGcmSenderId(),
                            gcmToken);

                    Response response = new OkHttpClient().newCall(request).execute();
                    final int status = response.code();

                    if (status >= 200 && status < 300) {
                        if (BuildConfig.DEBUG) {
                            Log.d(Constant.TAG, "! Parse registered with code " + status);
                        }
                        Model.getInstance().setParsePushRegistered(true);
                    } else if (BuildConfig.DEBUG) {
                        Log.d(Constant.TAG, "! Failed to Parse register; code:" + status);
                    }
                } catch (Throwable t) {
                    Log.e(Constant.TAG, null, t);
                }
            }
        });
    }
}
