package com.tealium.digitalvelocity.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.tealium.beacon.Defaults;
import com.tealium.beacon.EstimoteManager;
import com.tealium.digitalvelocity.AlarmReceiver;
import com.tealium.digitalvelocity.BuildConfig;
import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.gson.AgendaItem;
import com.tealium.digitalvelocity.data.gson.Question;
import com.tealium.digitalvelocity.event.Purge;
import com.tealium.digitalvelocity.event.SyncCompleteEvent;
import com.tealium.digitalvelocity.keymanager.KeyManager;
import com.tealium.digitalvelocity.parse.ParseHelper;
import com.tealium.digitalvelocity.parse.SyncManager;
import com.tealium.digitalvelocity.push.PushManager;
import com.tealium.digitalvelocity.util.Constant;
import com.tealium.digitalvelocity.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.greenrobot.event.EventBus;


public final class Model {

    private static final int BUFFER_SIZE = 8192;
    private static Model sInstance;

    private final SharedPreferences mSharedPreferences;
    private final SharedPreferences mImgQueue;
    private final SharedPreferences mAgendaFavorites;
    private final SharedPreferences mVipPreferences;
    private final KeyManager mKeyManager;
    private final Context mContext;
    private final Typeface mDefaultTypeface;
    private final Typeface mSemiBoldTypeface;
    private final Typeface mLightTypeface;
    private final Typeface mFontAwesome;
    private final String mVisitorId;

    private Model(Context context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        mContext = context.getApplicationContext();
        mSharedPreferences = context.getSharedPreferences(Constant.SP.NAME, 0);
        mImgQueue = context.getSharedPreferences(Constant.IMG_QUEUE.NAME, 0);
        mAgendaFavorites = context.getSharedPreferences(Constant.AGENDA_FAVORITES, 0);
        mVipPreferences = context.getSharedPreferences(Constant.VIP_PREFERENCES, 0);

        String uuid = mSharedPreferences.getString(Constant.SP.KEY_UUID, null);

        if (uuid == null) {
            mSharedPreferences.edit().putString(
                    Constant.SP.KEY_UUID,
                    uuid = UUID.randomUUID().toString())
                    .commit();
        }

        mVisitorId = uuid.replace("-", "");

        mDefaultTypeface = Typeface.createFromAsset(
                context.getAssets(), "fonts/MyriadPro-Regular.otf");
        mSemiBoldTypeface = Typeface.createFromAsset(
                context.getAssets(), "fonts/MyriadPro-Semibold.otf");
        mLightTypeface = Typeface.createFromAsset(
                context.getAssets(), "fonts/MyriadPro-Light.otf");
        mFontAwesome = Typeface.createFromAsset(
                context.getAssets(), "fonts/fontawesome-webfont.ttf");

        try {
            mKeyManager = new KeyManager(mContext);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        EstimoteManager.setup(mContext, mKeyManager.getEstimoteProximityUuid());

        final EventBus bus = EventBus.getDefault();

        bus.register(new SyncManager(context));
        bus.register(new IOManager(context));

        if (isFirstLaunchSinceUpdate()) {
            // Should re-download all data to be safe
            clearCacheKeys();
            bus.post(new Purge());
        }

        updateEstimoteManager();
    }

    public boolean isSurveyComplete(String surveyID) {
        return mSharedPreferences.contains(Constant.SP.KEY_SURVEY_PREFIX + surveyID);
    }

    public void setSurveyComplete(String surveyID, Map<Question, String> answers) {

        final String key = Constant.SP.KEY_SURVEY_PREFIX + surveyID;

        final SharedPreferences.Editor editor = mSharedPreferences.edit()
                .putBoolean(key, true);

        for (Map.Entry<Question, String> entry : answers.entrySet()) {
            editor.putString(
                    Constant.SP.KEY_SURVEY_QUESTION_PREFIX + entry.getKey().getId(),
                    entry.getValue());
        }

        editor.apply();
    }

    public static synchronized Model setup(Context context) {
        if (sInstance == null) {
            sInstance = new Model(context);
        }
        return sInstance;
    }

    public static Model getInstance() {
        return sInstance;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public Typeface getDefaultTypeface() {
        return mDefaultTypeface;
    }

    public Typeface getSemiBoldTypeface() {
        return mSemiBoldTypeface;
    }

    public Typeface getLightTypeface() {
        return mLightTypeface;
    }

    public Typeface getFontAwesome() {
        return mFontAwesome;
    }

    public KeyManager getKeyManager() {
        return mKeyManager;
    }

    public String getVisitorId() {
        return mVisitorId;
    }

    public String getUserEmail() {
        return mSharedPreferences.getString(TrackingManager.Key.EMAIL, null);
    }

    public String getTraceId() {
        return mSharedPreferences.getString(Constant.SP.KEY_TRACE_ID, null);
    }

    public String getWelcomeYear() {
        return mSharedPreferences.getString(
                Constant.SP.KEY_WELCOME_YEAR,
                mContext.getString(R.string.main_label_year_text));
    }

    public String getWelcomeDescription() {
        return mSharedPreferences.getString(
                Constant.SP.KEY_WELCOME_DESCRIPTION,
                mContext.getString(R.string.main_label_slogan_text));
    }

    public String getWelcomeSubtitle() {
        return mSharedPreferences.getString(
                Constant.SP.KEY_WELCOME_SUBTITLE,
                mContext.getString(R.string.main_label_date_location_text));
    }

    public String getUUID() {
        return mSharedPreferences.getString(Constant.SP.KEY_UUID, null);
    }

    public String getDemoAccount() {
        return mSharedPreferences.getString(Constant.SP.KEY_DEMO_ACCOUNT, null);
    }

    public String getDemoProfile() {
        return mSharedPreferences.getString(Constant.SP.KEY_DEMO_PROFILE, null);
    }

    public String getDemoEnvironment() {
        return mSharedPreferences.getString(Constant.SP.KEY_DEMO_ENVIRONMENT, null);
    }

    public Map<String, ?> getVipData() {
        return mVipPreferences.getAll();
    }

    public String getSurveyAnswer(Question question) {
        return mSharedPreferences.getString(Constant.SP.KEY_SURVEY_QUESTION_PREFIX + question.getId(), null);
    }

    public void updateVipInfo(JSONObject o) throws JSONException {
        final SharedPreferences.Editor editor = mVipPreferences.edit();
        for (Iterator<String> i = o.keys(); i.hasNext(); ) {
            final String key = i.next();
            Object value = o.get(key);
            if (value instanceof JSONArray) {
                final JSONArray valueJsonArray = (JSONArray) value;
                final Set<String> valueAsSet = new HashSet<>(valueJsonArray.length());
                for (int j = 0; j < valueJsonArray.length(); j++) {
                    valueAsSet.add(valueJsonArray.getString(j));
                }
                editor.putStringSet(key, valueAsSet);
            } else {
                editor.putString(key, "" + value);
            }
        }
        editor.commit();
    }

    /**
     * @return instanceId if account/profile/environment are appropriate
     */
    public String getDemoInstanceId() {
        return mSharedPreferences.getString(Constant.SP.KEY_DEMO_INSTANCE_ID, null);
    }

    public long getParseSyncRate() {
        return mSharedPreferences.getLong(Constant.SP.KEY_PARSE_SYNC_RATE, 15);
    }

    public boolean canPromptBluetooth() {
        return mSharedPreferences.getBoolean(Constant.SP.KEY_CAN_PROMPT_BLUETOOTH, true);
    }

    public void setUsageDataEnabled(boolean isEnabled) {
        mSharedPreferences.edit()
                .putBoolean(Constant.SP.KEY_USAGE_DATA_ENABLED, isEnabled)
                .apply();
    }

    public boolean isUsageDataEnabled() {
        return mSharedPreferences.getBoolean(Constant.SP.KEY_USAGE_DATA_ENABLED, true);
    }

    public int getMonitoringStartHour() {
        return mSharedPreferences.getInt(
                Constant.SP.KEY_MONITORING_START_HOUR, 0);
    }

    public boolean isParsePushRegistered() {
        return mSharedPreferences.getBoolean(Constant.SP.KEY_IS_PARSE_PUSH_REGISTERED, false);
    }

    public void setParsePushRegistered(boolean isRegistered) {
        mSharedPreferences.edit()
                .putBoolean(Constant.SP.KEY_IS_PARSE_PUSH_REGISTERED, isRegistered)
                .commit();
    }

    public String getGcmToken() {
        return PushManager.getGcmPushToken(mContext);
    }

    public String getGcmSenderId() {
        return PushManager.getGcmSenderId(mContext);
    }

    public String getSnowShoeUrl() {
        return mSharedPreferences.getString(Constant.SP.KEY_SNOWSHOE_URL, null);
    }

    public String getTealiumAccount() {
        return mSharedPreferences.getString(Constant.SP.KEY_OVERRIDE_TEALIUM_ACCOUNT, "tealium");
    }

    public String getTealiumProfile() {
        return mSharedPreferences.getString(Constant.SP.KEY_OVERRIDE_TEALIUM_PROFILE, "digitalvelocity");
    }

    public String getTealiumEnv() {
        return mSharedPreferences.getString(Constant.SP.KEY_OVERRIDE_TEALIUM_ENV, "BuildConfig.TEALIUM_ENV");
    }

    public long getMonitoringStartDate() {
        return mSharedPreferences.getLong(
                Constant.SP.KEY_MONITORING_START_DATE, 0);
    }

    public int getMonitoringStopHour() {
        return mSharedPreferences.getInt(
                Constant.SP.KEY_MONITORING_STOP_HOUR, 23);
    }

    public long getMonitoringStopDate() {
        return mSharedPreferences.getLong(
                Constant.SP.KEY_MONITORING_STOP_DATE, Long.MAX_VALUE);
    }

    public void setConfig(JSONObject o) {


        final SharedPreferences.Editor editor = mSharedPreferences.edit()
                .putString(Constant.SP.KEY_SNOWSHOE_URL, o.optString(ParseHelper.Column.WELCOME_URL, null))
                .putLong(Constant.SP.KEY_PARSE_SYNC_RATE, o.optLong("syncRate", 15) * 60000L)
                .putString(Constant.SP.KEY_WELCOME_YEAR, o.optString("welcomeYear", null))
                .putString(Constant.SP.KEY_WELCOME_DESCRIPTION, o.optString("welcomeDescription", null))
                .putString(Constant.SP.KEY_WELCOME_SUBTITLE, o.optString("welcomeSubtitle", null))
                .putInt(Constant.SP.KEY_MONITORING_START_HOUR, o.optInt("startMonitoring", 0))
                .putLong(Constant.SP.KEY_MONITORING_START_DATE,
                        ParseHelper.parseDate(o.optJSONObject("startMonitoringDate"), 0))
                .putInt(Constant.SP.KEY_MONITORING_STOP_HOUR, o.optInt("stopMonitoring", 23))
                .putLong(Constant.SP.KEY_MONITORING_STOP_DATE,
                        ParseHelper.parseDate(o.optJSONObject("stopMonitoringDate"), Long.MAX_VALUE))
                // Estimote Manager
                .putInt(Constant.SP.KEY_RSSI_THRESHOLD, o.optInt("rssiThreshold", Defaults.RSSI_THRESHOLD))
                .putLong(Constant.SP.KEY_POI_IN_PERIOD,
                        o.optLong("poiRefreshCycle", Defaults.POI_IN_PERIOD))
                .putLong(Constant.SP.KEY_POI_SCAN_DELAY_PERIOD,
                        o.optLong("scanCycle", Defaults.POI_SCAN_DELAY_PERIOD))
                .putLong(Constant.SP.KEY_POI_THRESHOLD_ENTER,
                        o.optLong("enterThreshold", Defaults.POI_THRESHOLD_ENTER))
                .putLong(Constant.SP.KEY_POI_THRESHOLD_EXIT,
                        o.optLong("exitThreshold", Defaults.POI_THRESHOLD_EXIT));

        setOrRemoveIfNull(editor, Constant.SP.KEY_OVERRIDE_TEALIUM_ACCOUNT, o.optString("accountOverride"));
        setOrRemoveIfNull(editor, Constant.SP.KEY_OVERRIDE_TEALIUM_PROFILE, o.optString("profileOverride"));
        setOrRemoveIfNull(editor, Constant.SP.KEY_OVERRIDE_TEALIUM_ENV, o.optString("envOverride"));

        editor.commit();

        this.updateEstimoteManager();

        AlarmReceiver.setup(mContext);
    }

    /**
     * @param editor non-null
     * @param key    for persistence in the editor
     * @param value  the value to write if not empty (empty string or null)
     */
    private static void setOrRemoveIfNull(SharedPreferences.Editor editor, String key, String value) {
        if (TextUtils.isEmpty(value)) {
            editor.remove(key);
        } else {
            editor.putString(key, value);
        }
    }

    private void updateEstimoteManager() {
        EstimoteManager.getInstance()
                .setRSSIThreshold(mSharedPreferences.getInt(
                        Constant.SP.KEY_RSSI_THRESHOLD, Integer.MIN_VALUE))
                .setEnterThreshold(mSharedPreferences.getLong(
                        Constant.SP.KEY_POI_THRESHOLD_ENTER, Defaults.POI_THRESHOLD_ENTER) * 1000L)
                .setExitThreshold(mSharedPreferences.getLong(
                        Constant.SP.KEY_POI_THRESHOLD_EXIT, Defaults.POI_THRESHOLD_EXIT) * 1000L)
                .setInPOIPeriod(mSharedPreferences.getLong(
                        Constant.SP.KEY_POI_IN_PERIOD, Defaults.POI_IN_PERIOD) * 1000L)
                .setScanDelay(mSharedPreferences.getLong(
                        Constant.SP.KEY_POI_SCAN_DELAY_PERIOD, Defaults.POI_SCAN_DELAY_PERIOD) * 1000L);
    }

    public void setBluetoothPromptEnabled(boolean enabled) {
        mSharedPreferences.edit()
                .putBoolean(Constant.SP.KEY_CAN_PROMPT_BLUETOOTH, enabled)
                .apply();
    }

    public void setUserEmail(String userEmail) {

        if (Util.isEmptyOrNull(userEmail)) {
            mSharedPreferences.edit().remove(TrackingManager.Key.EMAIL);
            return;
        }

        mSharedPreferences.edit().putString(TrackingManager.Key.EMAIL, userEmail).apply();
    }

    public void setTraceId(String newId) {

        SharedPreferences.Editor editor = mSharedPreferences.edit();

        if (Util.isEmptyOrNull(newId)) {
            editor.remove(Constant.SP.KEY_TRACE_ID).apply();
        } else {
            editor.putString(Constant.SP.KEY_TRACE_ID, newId).apply();
        }
    }

    public void setDemoAccountProfileEnvironment(String accountName, String profileName, String environmentName) {
        mSharedPreferences.edit()
                .putString(Constant.SP.KEY_DEMO_ACCOUNT, accountName)
                .putString(Constant.SP.KEY_DEMO_PROFILE, profileName)
                .putString(Constant.SP.KEY_DEMO_ENVIRONMENT, environmentName)
                .putString(Constant.SP.KEY_DEMO_INSTANCE_ID, Util.createInstanceId(
                        accountName, profileName, environmentName))
                .apply();
    }

    public void enqueueImageDownload(@NonNull String id, String url) {
        if (url == null) {
            return;
        }

        if (BuildConfig.DEBUG) Log.v(Constant.TAG, "# Enqueued " + id + " to download " + url);

        mImgQueue.edit().putString(id, url).apply();
        performRequest(id, url);
    }

    public boolean isImageEnqueued(String id) {
        return mImgQueue.contains(id);
    }

    public boolean resumeImageDownloads() {

        Map<String, ?> entries = mImgQueue.getAll();
        if (entries.size() == 0) {
            return false;
        }

        if (BuildConfig.DEBUG) Log.d(Constant.TAG, "Resuming " + entries.size() +
                " image downloads...");

        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            performRequest(entry.getKey(), entry.getValue() + "");
        }

        return true;
    }

    public boolean isAgendaFavorite(AgendaItem item) {
        return mAgendaFavorites.getBoolean(item.getId(), false);
    }

    public boolean isFirstLaunchSinceUpdate() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);
            if (pInfo.versionCode != mSharedPreferences.getInt(Constant.SP.KEY_APP_VERSION_CODE, 0)) {
                // Update occurred
                mSharedPreferences.edit()
                        .putInt(Constant.SP.KEY_APP_VERSION_CODE, pInfo.versionCode)
                        .apply();
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            // to be safe, should never happen
            return true;
        }
    }

    public void setAgendaFavorite(AgendaItem item, boolean isFavorite) {
        mAgendaFavorites.edit()
                .putBoolean(item.getId(), isFavorite)
                .apply();
    }

    public boolean isContactVisible() {
        return mSharedPreferences.getBoolean(Constant.SP.KEY_CONTACT_VISIBLE, false);
    }

    public String getContactFacebook() {
        return mSharedPreferences.getString(Constant.SP.KEY_CONTACT_FACEBOOK, null);
    }

    public String getContactEmailHeader() {
        return mSharedPreferences.getString(Constant.SP.KEY_CONTACT_EMAIL_HEADER, null);
    }

    public String getContactEmailMessage() {
        return mSharedPreferences.getString(Constant.SP.KEY_CONTACT_EMAIL_MESSAGE, null);
    }

    public String getContactEmail() {
        return mSharedPreferences.getString(Constant.SP.KEY_CONTACT_EMAIL, null);
    }

    public String getContactPhoneNumber() {
        return mSharedPreferences.getString(Constant.SP.KEY_CONTACT_PHONE_NUMBER, null);
    }

    public String getContactTwitter() {
        return mSharedPreferences.getString(Constant.SP.KEY_CONTACT_TWITTER, null);
    }

    public void setContactInfo(JSONObject contactInfo) {
        if (contactInfo == null) {
            mSharedPreferences.edit()
                    .remove(Constant.SP.KEY_CONTACT_EMAIL)
                    .remove(Constant.SP.KEY_CONTACT_EMAIL_HEADER)
                    .remove(Constant.SP.KEY_CONTACT_EMAIL_MESSAGE)
                    .remove(Constant.SP.KEY_CONTACT_FACEBOOK)
                    .remove(Constant.SP.KEY_CONTACT_PHONE_NUMBER)
                    .remove(Constant.SP.KEY_CONTACT_TWITTER)
                    .remove(Constant.SP.KEY_CONTACT_VISIBLE)
                    .commit();
            return;
        }

        mSharedPreferences.edit()
                .putString(
                        Constant.SP.KEY_CONTACT_EMAIL,
                        contactInfo.optString(ParseHelper.Column.EMAIL, null))
                .putString(
                        Constant.SP.KEY_CONTACT_EMAIL_HEADER,
                        contactInfo.optString(ParseHelper.Column.EMAIL_HEADER, null))
                .putString(
                        Constant.SP.KEY_CONTACT_EMAIL_MESSAGE,
                        contactInfo.optString(ParseHelper.Column.EMAIL_MESSAGE, null))
                .putString(
                        Constant.SP.KEY_CONTACT_FACEBOOK,
                        contactInfo.optString(ParseHelper.Column.FACEBOOK, null))
                .putString(
                        Constant.SP.KEY_CONTACT_PHONE_NUMBER,
                        contactInfo.optString(ParseHelper.Column.PHONE_NUMBER, null))
                .putString(
                        Constant.SP.KEY_CONTACT_TWITTER,
                        contactInfo.optString(ParseHelper.Column.TWITTER, null))
                .putBoolean(
                        Constant.SP.KEY_CONTACT_VISIBLE,
                        contactInfo.optBoolean(ParseHelper.Column.VISIBLE, false))
                .commit();
    }

    /**
     * Removes the keys used to download fresh data, will download all data next sync (instead of updated-since).
     */
    public void clearCacheKeys() {
        mSharedPreferences.edit()
                .remove(Constant.SP.KEY_LAST_SYNC_COMPANY)
                .remove(Constant.SP.KEY_LAST_SYNC_CONFIG)
                .remove(Constant.SP.KEY_LAST_SYNC_LOCATION)
                .remove(Constant.SP.KEY_LAST_SYNC_EVENT)
                .remove(Constant.SP.KEY_LAST_SYNC_CATEGORY)
                .apply();
    }

    private void performRequest(final String id, final String imageURI) {

        new AsyncTask<Void, Void, Throwable>() {
            @Override
            protected Throwable doInBackground(Void... params) {

                HttpURLConnection connection = null;

                try {
                    URL url = new URL(imageURI);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    /* Parse vended text/plain MIME type WTF?
                    final String contentType = connection.getContentType();
                    if (!contentType.startsWith("image/")) {
                        Log.e(Constant.TAG, "Error loading " + imageURI + ", unknown MIME type " + contentType);
                        mImgQueue.edit().remove(id).commit();
                        return null;
                    }*/

                    final File dst = new File(mContext.getFilesDir(), id + '.' +
                            imageURI.substring(imageURI.lastIndexOf('.') + 1));
                    if (BuildConfig.DEBUG) {
                        Log.d(Constant.TAG, "# Creating file " + dst);
                    }
                           /* contentType.substring(
                                    contentType.lastIndexOf("/") + 1, contentType.length()));*/

                    InputStream in = connection.getInputStream();
                    FileOutputStream out = new FileOutputStream(dst);
                    final byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    in.close();
                    out.close();

                    mImgQueue.edit().remove(id).commit();

                    EventBus.getDefault().post(new SyncCompleteEvent.Image(id));

                    if (BuildConfig.DEBUG) Log.v(Constant.TAG, "# Downloaded " + id);

                    return null;
                } catch (Throwable t) {
                    Log.e(Constant.TAG, "Error for " + imageURI, t);
                    return t;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

            }
        }

                .

                        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    final static class Test {
        private Test() {
        }

        static void setOrRemoveIfNull(
                SharedPreferences.Editor editor,
                String key,
                String value) {
            setOrRemoveIfNull(editor, key, value);
        }

        static void clear(Model model) {
            model.mSharedPreferences.edit().clear().commit();
            model.mImgQueue.edit().clear().commit();
            model.mAgendaFavorites.edit().clear().commit();
            model.mVipPreferences.edit().clear().commit();
        }
    }
}
