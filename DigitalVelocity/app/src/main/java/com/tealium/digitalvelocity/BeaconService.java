package com.tealium.digitalvelocity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.tealium.beacon.EstimoteManager;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.util.Constant;
import com.tealium.digitalvelocity.util.Util;

import java.util.Calendar;

public final class BeaconService extends Service {

    private static BeaconService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Util.attemptToStartMonitoringBeacons(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public synchronized static void start(Context context) {
        if (instance == null) {
            context.startService(new Intent(context, BeaconService.class));
        }
    }
}
