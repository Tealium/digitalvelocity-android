package com.tealium.digitalvelocity.data;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.tealium.beacon.EstimoteManager;
import com.tealium.beacon.event.BeaconEntered;
import com.tealium.beacon.event.BeaconExited;
import com.tealium.beacon.event.BeaconUpdate;
import com.tealium.digitalvelocity.BuildConfig;
import com.tealium.digitalvelocity.event.DemoChangeEvent;
import com.tealium.digitalvelocity.event.SyncRequest;
import com.tealium.digitalvelocity.event.TealiumConfigEvent;
import com.tealium.digitalvelocity.event.TraceUpdateEvent;
import com.tealium.digitalvelocity.event.TrackEvent;
import com.tealium.digitalvelocity.event.TrackUpdateEvent;
import com.tealium.digitalvelocity.event.UsageDataToggle;
import com.tealium.digitalvelocity.util.Constant;
import com.tealium.digitalvelocity.util.Util;
import com.tealium.library.Tealium;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.greenrobot.event.EventBus;

public final class TrackingManager implements Application.ActivityLifecycleCallbacks {

    private static final String TEALIUM_INSTANCE_MAIN = "com.tealium.digitalvelocity";
    private static final long TIMEOUT = 10000;

    private final Map<String, String> mStaticData;
    private final Handler mMainHandler;
    private final Runnable mSleepRunnable;
    private final boolean mHasBluetoothLE;
    private final Application mApplication;

    private boolean mIsInForeground;
    private long mLastPause;

    public TrackingManager(Application application) {

        mApplication = application;
        mMainHandler = new Handler(Looper.getMainLooper());
        mSleepRunnable = createSleepRunnable();

        final Model model = Model.getInstance();

        if (model.isUsageDataEnabled()) {
            final Tealium instance = Tealium.createInstance(TEALIUM_INSTANCE_MAIN, createConfig());

            final String traceId = model.getTraceId();
            if (traceId != null) {
                instance.joinTrace(traceId);
            }

            attemptToStartDemoInstance();
        }

        mHasBluetoothLE = EstimoteManager.getInstance().hasBluetoothLE();

        mStaticData = new HashMap<>(3);
        mStaticData.put("parse_channel", "vid-" + model.getVisitorId());
        mStaticData.put("has_bluetooth_le", "" + mHasBluetoothLE);

        String userEmail = model.getUserEmail();

        if (userEmail != null) {
            mStaticData.put(Key.EMAIL, userEmail.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * @return visitorId if it exists or null
     */
    public static String getVisitorId() {
        final Tealium tealium = Tealium.getInstance(TEALIUM_INSTANCE_MAIN);
        if (tealium != null) {
            return tealium.getDataSources().getVisitorId();
        }

        return null;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

        mMainHandler.removeCallbacks(mSleepRunnable);

        if (System.currentTimeMillis() - mLastPause > TIMEOUT) {
            onWake(activity, mLastPause == 0);
        }

        // App name must be the launcher activity's label to show the app correctly
        final String screenTitle = Util.getActivityTitle(activity);

        EventBus.getDefault().post(
                TrackEvent.createViewTrackEvent()
                        .add("event_name", "m_view")
                        .add("screen_title", screenTitle));
    }

    @Override
    public void onActivityPaused(Activity activity) {
        mLastPause = System.currentTimeMillis();
        mMainHandler.postDelayed(mSleepRunnable, 10000);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(DemoChangeEvent event) {

        final Model model = Model.getInstance();
        final String oldDemoInstanceId = model.getDemoInstanceId();
        final String newDemoInstanceId = event.getDemoInstanceId();

        if (TextUtils.equals(oldDemoInstanceId, newDemoInstanceId)) {
            return; // No change
        }

        Tealium demoInstance = Tealium.getInstance(oldDemoInstanceId);
        if (demoInstance != null) {
            Tealium.destroyInstance(oldDemoInstanceId);
        }

        model.setDemoAccountProfileEnvironment(
                event.getAccountName(),
                event.getProfileName(),
                event.getEnvironmentName());

        attemptToStartDemoInstance();
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(BeaconEntered event) {

        final Model model = Model.getInstance();
        final int possibleAdditionalDataSourceCount = 12;

        Map<String, Object> data = new HashMap<>(mStaticData.size() + possibleAdditionalDataSourceCount);
        data.putAll(mStaticData);
        data.putAll(model.getVipData());

        data.put("beacon_id", event.getId());
        data.put("event_name", "enter_poi");
        data.put("beacon_rssi", "" + event.getRssi());

        addDynamicKeys(data);

        safeTealiumTrackEvent("enter_poi", data);

        if (BuildConfig.DEBUG) {
            Log.d(Constant.TAG, "# BEACON ENTER: {id:" + event.getId() + ", rssi:" + event.getRssi() + '}');
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(BeaconUpdate event) {
        Map<String, Object> data = new HashMap<>(mStaticData.size() + 3);
        data.putAll(mStaticData);
        data.put("beacon_id", event.getId());
        data.put("event_name", "in_poi");
        data.put("beacon_rssi", "" + event.getRssi());
        addDynamicKeys(data);

        safeTealiumTrackEvent("in_poi", data);

        if (BuildConfig.DEBUG) {
            Log.d(Constant.TAG, "# BEACON UPDATE: {id:" + event.getId() + ", rssi:" + event.getRssi() + '}');
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(BeaconExited event) {
        Map<String, Object> data = new HashMap<>(mStaticData.size() + 3);
        data.putAll(mStaticData);
        data.put("beacon_id", event.getId());
        data.put("event_name", "exit_poi");
        data.put("beacon_rssi", "" + event.getRssi());
        addDynamicKeys(data);

        safeTealiumTrackEvent("exit_poi", data);

        if (BuildConfig.DEBUG) {
            Log.d(Constant.TAG, "# BEACON EXIT: {id:" + event.getId() + ", rssi:" + event.getRssi() + '}');
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(TrackEvent event) {

        for (Map.Entry<String, String> staticItem : mStaticData.entrySet()) {
            event.getData().put(staticItem.getKey(), staticItem.getValue());
        }

        addDynamicKeys(event.getData());

        if (TrackEvent.TYPE_VIEW.equals(event.getType())) {
            safeTealiumTrackView(null, event.getData());
        } else {
            safeTealiumTrackEvent(null, event.getData());
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(TrackUpdateEvent event) {

        if (Key.EMAIL.equals(event.getKey())) {
            Model.getInstance().setUserEmail(event.getValue());
        }

        if (event.getValue() == null) {
            mStaticData.remove(event.getKey());
            return;
        }

        mStaticData.put(event.getKey(), event.getValue());
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(UsageDataToggle event) {

        final Model model = Model.getInstance();
        model.setUsageDataEnabled(event.isEnabled());

        if (event.isEnabled()) {
            if (!doesTealiumMainExist()) {
                Tealium.createInstance(TEALIUM_INSTANCE_MAIN, createConfig())
                        .joinTrace(model.getTraceId());
            }

            attemptToStartDemoInstance();

        } else {
            model.setTraceId(null);
            Tealium.destroyInstance(TEALIUM_INSTANCE_MAIN);
            Tealium.destroyInstance(model.getDemoInstanceId());
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(TraceUpdateEvent event) {
        final String traceId = event.getTraceId();

        if (TextUtils.isEmpty(traceId)) {
            safeTealiumLeaveTrace();
            Model.getInstance().setTraceId(null);
        } else {
            safeTealiumJoinTrace(traceId);
            Model.getInstance().setTraceId(traceId);
        }
    }

    @SuppressWarnings("unused")
    public void onEventBackgroundThread(TealiumConfigEvent event) {

        Tealium instance = Tealium.getInstance(TEALIUM_INSTANCE_MAIN);
        if (instance != null) {
            if (!TextUtils.equals(instance.getAccountName(), event.getAccountName()) ||
                    !TextUtils.equals(instance.getProfileName(), event.getProfileName()) ||
                    !TextUtils.equals(instance.getEnvironmentName(), event.getEnvName())) {
                Tealium.destroyInstance(TEALIUM_INSTANCE_MAIN);
            } else {
                // No change; return
                return;
            }
        }

        Tealium.createInstance(TEALIUM_INSTANCE_MAIN, Tealium.Config.create(
                mApplication,
                event.getAccountName(),
                event.getProfileName(),
                event.getEnvName()));
    }

    private void onWake(Activity activity, boolean isLaunch) {

        mIsInForeground = true;

        Model model = Model.getInstance();
        model.resumeImageDownloads();

        EventBus bus = EventBus.getDefault();
        bus.post(new SyncRequest());
        bus.post(TrackEvent.createLinkTrackEvent()
                .add("event_name", (isLaunch ? "m_launch" : "m_wake")));

    }

    private void onSleep() {

        mIsInForeground = false;

        EventBus.getDefault().post(TrackEvent.createLinkTrackEvent()
                .add("event_name", "m_sleep"));
    }

    private void addDynamicKeys(Map<String, Object> data) {
        if (!mHasBluetoothLE || !Util.isBluetoothEnabled()) {
            data.put(Key.BEACON_DETECTION_DISABLED, "" + true);
        }
        data.put(Key.IS_APP_ACTIVE, mIsInForeground + "");
    }

    private Runnable createSleepRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                onSleep();
            }
        };
    }

    private static void safeTealiumTrackView(String eventName, Map<String, ?> data) {
        Tealium instance = Tealium.getInstance(TEALIUM_INSTANCE_MAIN);
        if (instance != null) {
            instance.trackView(eventName, data);
        }

        instance = Tealium.getInstance(Model.getInstance().getDemoInstanceId());
        if (instance != null) {
            instance.trackView(eventName, data);
        }
    }

    private static void safeTealiumTrackEvent(String eventName, Map<String, ?> data) {
        Tealium instance = Tealium.getInstance(TEALIUM_INSTANCE_MAIN);
        if (instance != null) {
            instance.trackEvent(eventName, data);
        }

        instance = Tealium.getInstance(Model.getInstance().getDemoInstanceId());
        if (instance != null) {
            instance.trackEvent(eventName, data);
        }
    }

    private static void safeTealiumJoinTrace(String traceId) {
        Tealium instance = Tealium.getInstance(TEALIUM_INSTANCE_MAIN);
        if (instance != null) {
            instance.joinTrace(traceId);
        }

        instance = Tealium.getInstance(Model.getInstance().getDemoInstanceId());
        if (instance != null) {
            instance.joinTrace(traceId);
        }
    }

    private static void safeTealiumLeaveTrace() {
        Tealium instance = Tealium.getInstance(TEALIUM_INSTANCE_MAIN);
        if (instance != null) {
            instance.leaveTrace();
        }

        instance = Tealium.getInstance(Model.getInstance().getDemoInstanceId());
        if (instance != null) {
            instance.leaveTrace();
        }
    }

    private static boolean doesTealiumMainExist() {
        return Tealium.getInstance(TEALIUM_INSTANCE_MAIN) != null;
    }

    private void attemptToStartDemoInstance() {

        final Model model = Model.getInstance();
        final String demoInstanceId = model.getDemoInstanceId();

        if (demoInstanceId != null && Tealium.getInstance(demoInstanceId) == null) {
            Tealium.createInstance(
                    demoInstanceId,
                    Tealium.Config.create(
                            mApplication,
                            model.getDemoAccount(),
                            model.getDemoProfile(),
                            model.getDemoEnvironment()))
                    .joinTrace(model.getTraceId());
        }
    }

    private Tealium.Config createConfig() {

        final Model model = Model.getInstance();

        return Tealium.Config.create(
                mApplication,
                model.getTealiumAccount(),
                model.getTealiumProfile(),
                model.getTealiumEnv());
    }

    public static final class Key {
        private Key() {
        }

        public static final String EMAIL = "email";
        public static final String BEACON_DETECTION_DISABLED = "beacon_detection_disabled";
        public static final String IS_APP_ACTIVE = "is_app_active";
    }

}
