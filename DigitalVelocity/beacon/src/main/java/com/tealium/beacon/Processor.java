package com.tealium.beacon;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.tealium.beacon.event.BeaconEntered;
import com.tealium.beacon.event.BeaconExited;
import com.tealium.beacon.event.BeaconUpdate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.greenrobot.event.EventBus;

final class Processor implements BeaconManager.RangingListener {

    public static final String SP_NAME = "beacon";
    private static final String SP_KEY_IN_ID = "in_id";

    static {
        if (Constant.DEBUG) {
            Log.i(Constant.TAG, "-------------------- BEGIN --------------------");
        }
    }

    private final SharedPreferences sharedPreferences;
    private final Map<String, Imprint> imprintPool;

    private Imprint establishedImprint;
    private long lastMessage;
    private int rssiThreshold;
    private long enterThreshold;
    private long exitThreshold;
    private long updateTimeout;

    public Processor(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SP_NAME, 0);
        this.imprintPool = new HashMap<>(5);// Shouldn't be more than 5 imprints at a given time;

        this.rssiThreshold = Defaults.RSSI_THRESHOLD;
        this.enterThreshold = Defaults.POI_THRESHOLD_ENTER * 1000L;
        this.exitThreshold = Defaults.POI_THRESHOLD_EXIT * 1000L;
        this.updateTimeout = Defaults.POI_IN_PERIOD * 1000L;
    }

    public void setRssiThreshold(int rssiThreshold) {
        this.rssiThreshold = rssiThreshold;
        if (Constant.DEBUG) {
            Log.v(Constant.TAG, "# rssi threshold set to " + rssiThreshold);
        }
    }

    public void setEnterThreshold(long ms) {
        this.enterThreshold = ms;
        if (Constant.DEBUG) {
            Log.v(Constant.TAG, "# enter threshold set to " + ms);
        }
    }

    public void setExitThreshold(long ms) {
        this.exitThreshold = ms;
        if (Constant.DEBUG) {
            Log.v(Constant.TAG, "# exit threshold set to " + ms);
        }
    }

    public void setUpdateTimeout(long ms) {
        this.updateTimeout = ms;
        if (Constant.DEBUG) {
            Log.v(Constant.TAG, "# update timeout set to " + ms);
        }
    }

    @Override
    public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {

        if (Constant.DEBUG) {
            Log.v(Constant.TAG, stringify(beacons));
        }

        final long now = SystemClock.uptimeMillis();

        Imprint incoming = getClosestImprint(now, beacons);
        if (incoming == null) {
            if (Constant.DEBUG) {
                Log.d(Constant.TAG, "None close enough. (uptime=" +
                        Long.toString(SystemClock.uptimeMillis(), 16) + ")");
            }
            this.checkTimeout(now);
            this.checkUpdateNeeded();
            return;
        }

        if (Constant.DEBUG) {
            Log.d(Constant.TAG, "Closest: " + incoming.getId());
        }

        Imprint existing = this.imprintPool.get(incoming.getId());
        if (existing == null) {
            this.imprintPool.put(incoming.getId(), incoming);
        } else {
            existing.update(incoming.getRssi(), now);
        }

        this.checkPrimacy(now);
        this.checkUpdateNeeded();
    }

    public void onBluetoothDisable() {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            // Route to main thread
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    exit();
                }
            });
            return;
        }

        this.exit();
    }

    private void checkPrimacy(long now) {
        if (this.imprintPool.size() == 0) {
            return;
        }

        long delta;
        long smallestDelta = Long.MAX_VALUE;
        Imprint closest = null;

        Collection<Imprint> imprintCollection = imprintPool.values();
        Imprint[] imprints = imprintCollection.toArray(new Imprint[imprintCollection.size()]);

        for (Imprint imprint : imprints) {
            delta = now - imprint.getUpdatedTimestamp();

            if (delta < smallestDelta) {
                smallestDelta = delta;
                closest = imprint;
            }

            if (this.isTimedOut(imprint, now)) {
                this.imprintPool.remove(imprint.getId());
            }
        }

        if (closest.equals(this.establishedImprint)) {
            return;
        }

        if ((closest.getUpdatedTimestamp() - closest.getBeganTimestamp()) > this.enterThreshold) {
            final String beaconId = closest.getId();

            if (beaconId.equals(this.sharedPreferences.getString(SP_KEY_IN_ID, null))) {
                // NOTE: if was restart, still need to aquire the closest.
                if (Constant.DEBUG) {
                    Log.d(Constant.TAG, "NO ENTER, already in " + beaconId);
                }
            } else {
                // A true enter.
                this.exit();
                this.sharedPreferences.edit()
                        .putString(SP_KEY_IN_ID, beaconId)
                        .apply();

                if (Constant.DEBUG) {
                    Log.d(Constant.TAG, "ENTER " + beaconId);
                }

                EventBus.getDefault().post(new BeaconEntered(
                        closest.getId(),
                        closest.getRssi()));

                this.lastMessage = SystemClock.uptimeMillis();
            }

            this.establishedImprint = closest;
            this.imprintPool.clear();
            this.imprintPool.put(closest.getId(), closest);
        }
    }

    private void checkTimeout(long now) {
        if (this.establishedImprint != null && this.isTimedOut(establishedImprint, now)) {
            this.exit();
        }
    }

    private void checkUpdateNeeded() {

        if (this.establishedImprint == null) {
            return;
        }

        final long now = SystemClock.uptimeMillis();

        if ((now - this.lastMessage) < this.updateTimeout) {
            return;
        }

        EventBus.getDefault().post(new BeaconUpdate(
                this.establishedImprint.getId(),
                this.establishedImprint.getRssi()));
        this.lastMessage = now;

    }

    private void exit() {

        if (this.establishedImprint == null) {
            return;
        }

        this.sharedPreferences.edit().remove(SP_KEY_IN_ID).apply();

        if (Constant.DEBUG) {
            Log.d(Constant.TAG, "EXIT " + this.establishedImprint.getId() + " delta=" +
                    (SystemClock.uptimeMillis() - this.establishedImprint.getUpdatedTimestamp()));
        }

        EventBus.getDefault().post(new BeaconExited(
                this.establishedImprint.getId(),
                this.establishedImprint.getRssi(),
                true));
        this.lastMessage = SystemClock.uptimeMillis();
        this.establishedImprint = null;
    }

    private Imprint getClosestImprint(long now, List<Beacon> beacons) {

        if (beacons.size() == 0) {
            return null;
        }

        Beacon closest = beacons.get(0);
        for (Beacon beacon : beacons) {
            if (beacon.getRssi() > closest.getRssi()) {
                closest = beacon;
            }
        }

        if (closest.getRssi() < this.rssiThreshold) {
            return null;
        }

        return new Imprint(closest, now);
    }

    public boolean isTimedOut(Imprint imprint, long now) {
        return now - imprint.getUpdatedTimestamp() > this.exitThreshold;
    }

    private static long lastTS = 0;

    private static String stringify(List<Beacon> beacons) {
        String s = "[";
        Iterator<Beacon> i = beacons.iterator();
        Beacon beacon;

        while (i.hasNext()) {
            beacon = i.next();
            s += String.format(Locale.ROOT, "%d.%d(%d)",
                    beacon.getMajor(),
                    beacon.getMinor(),
                    beacon.getRssi());
            if (i.hasNext()) {
                s += ", ";
            }
        }

        if (lastTS == 0) {
            lastTS = SystemClock.uptimeMillis();
            return s + "]";
        }

        final long now = SystemClock.uptimeMillis();
        s += "] (" + (now - lastTS) + ")";
        lastTS = now;

        return s;
    }
}
