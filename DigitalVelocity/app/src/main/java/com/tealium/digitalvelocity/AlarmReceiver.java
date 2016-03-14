package com.tealium.digitalvelocity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import com.tealium.beacon.EstimoteManager;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.util.Constant;
import com.tealium.digitalvelocity.util.Util;

import java.util.Calendar;
import java.util.Locale;

public final class AlarmReceiver extends BroadcastReceiver {

    private static final long INTERVAL_RECURRING = 86400000L;
    private static final String REQUEST_CODE = "request_code";
    private static final int REQUEST_START = 1;
    private static final int REQUEST_STOP = 2;

    // Necessary because registering the receiver will fire it immediately and cause a loop.
    private static boolean isBTListenerRegistered = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        final int code = intent.getIntExtra(REQUEST_CODE, 0);

        switch (code) {
            case REQUEST_START:
                Util.attemptToStartMonitoringBeacons(context);
                break;
            case REQUEST_STOP:
                final EstimoteManager estimoteManager = EstimoteManager.getInstance();
                if (estimoteManager.isListening()) {
                    estimoteManager.stop();
                }
                if (BuildConfig.DEBUG) Log.i(Constant.TAG, "Stopped listening.");
                break;
            default:
                Log.e(Constant.TAG, "Incorrect request code specified: " + code,
                        new IllegalArgumentException());
                break;
        }
    }

    public static void setup(Context context) {

        if (!isBTListenerRegistered) {
            context.registerReceiver(
                    createBluetoothListener(),
                    new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            isBTListenerRegistered = true;
        }

        final Model model = Model.getInstance();
        final int startHour = model.getMonitoringStartHour();
        final int stopHour = model.getMonitoringStopHour();
        final SharedPreferences sp = model.getSharedPreferences();

        if (startHour == sp.getInt(Constant.SP.KEY_ALARM_HOUR_START, Integer.MIN_VALUE) &&
                stopHour == sp.getInt(Constant.SP.KEY_ALARM_HOUR_STOP, Integer.MIN_VALUE)) {
            return; // no change
        }

        if (BuildConfig.DEBUG) Log.d(Constant.TAG, String.format(
                Locale.ROOT, "Scheduling to scan %02d00 - %02d00", startHour, stopHour));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, startHour);
        final long startTime = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, stopHour);
        final long stopTime = calendar.getTimeInMillis();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                startTime,
                INTERVAL_RECURRING,
                createPendingIntent(context, REQUEST_START));
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                stopTime,
                INTERVAL_RECURRING,
                createPendingIntent(context, REQUEST_STOP));

        sp.edit()
                .putInt(Constant.SP.KEY_ALARM_HOUR_START, startHour)
                .putInt(Constant.SP.KEY_ALARM_HOUR_STOP, stopHour)
                .commit();
    }

    private static BroadcastReceiver createBluetoothListener() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_ON) {
                        if(BuildConfig.DEBUG) Log.d(Constant.TAG, "! Bluetooth turned on");
                        Util.attemptToStartMonitoringBeacons(context);
                    }
                }
            }
        };
    }

    public static boolean isInMonitoringWindow() {
        final long now = System.currentTimeMillis();
        final Model model = Model.getInstance();
        final long startDate = model.getMonitoringStartDate();
        final long endDate = model.getMonitoringStopDate();

        if (startDate > now) {
            if (BuildConfig.DEBUG) Log.i(Constant.TAG, String.format(Locale.ROOT,
                    "! No ranging: before start date (now=%d < %d).", now, startDate));
            return false;
        } else if (now > endDate) {
            if (BuildConfig.DEBUG) Log.i(Constant.TAG, String.format(Locale.ROOT,
                    "! No ranging: after end date (now=%d > %d).", now, endDate));
            return false;
        }

        if (BuildConfig.DEBUG) Log.d(Constant.TAG, String.format(Locale.ROOT,
                "Ranging allowed (%d < now=%d < %d)", startDate, now, endDate));

        return true;
    }

    private static PendingIntent createPendingIntent(Context context, int reqCode) {
        Intent intent = new Intent(context, AlarmReceiver.class)
                .putExtra(REQUEST_CODE, reqCode);

        return PendingIntent.getBroadcast(
                context, reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
