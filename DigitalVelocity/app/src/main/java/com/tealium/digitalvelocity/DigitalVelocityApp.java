package com.tealium.digitalvelocity;

import android.os.Build;
import android.os.SystemClock;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.webkit.WebView;

import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.TrackingManager;
import com.tealium.digitalvelocity.push.gcm.PushListenerService;
import com.tealium.digitalvelocity.util.Constant;

import de.greenrobot.event.EventBus;

// Beacon consumer only works if Application implements it.
public final class DigitalVelocityApp extends MultiDexApplication {


    static {
        if (BuildConfig.DEBUG) Log.i(Constant.TAG,
                "==================== LAUNCH ====================");
    }


    @Override
    public void onCreate() {
        super.onCreate();

        long start = 0;
        if (BuildConfig.DEBUG) {
            start = SystemClock.uptimeMillis();
        }

        PushListenerService.setPendingIntentActivityClass(NotificationsActivity.class);

        Model.setup(this);
        TrackingManager tm = new TrackingManager(this);
        this.registerActivityLifecycleCallbacks(tm);
        EventBus.getDefault().register(tm);

        BeaconService.start(this);
        AlarmReceiver.setup(this);

        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
            Log.i(Constant.TAG, "Application onCreate took " +
                    (SystemClock.uptimeMillis() - start) + " ms.");
        }

    }
}
