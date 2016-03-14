package com.tealium.beacon;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.util.Log;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;


public final class EstimoteManager {

    private static EstimoteManager sInstance;

    private final BeaconManager mBeaconManager;
    private final Region mAllEstimoteBeacons;
    private final BluetoothListener mBluetoothListener;
    private final Context mContext;
    private final Processor mProcessor;
    private final Logger mLogger;
    private boolean mIsListening;

    public static EstimoteManager getInstance() {
        return sInstance;
    }

    public static synchronized EstimoteManager setup(Context context, String proximityUuid) {
        if (sInstance == null) {
            sInstance = new EstimoteManager(context, proximityUuid);
        }
        return sInstance;
    }

    private EstimoteManager(Context context, String proximityUuid) {

        mContext = context.getApplicationContext();
        mAllEstimoteBeacons = new Region("regionId", proximityUuid, null, null);

        if (Constant.DEBUG) {
            mLogger = new Logger(context);
            mLogger.log(System.currentTimeMillis() + ",created\r\n");
        } else {
            mLogger = null;
        }


        BeaconManager beaconManager = new BeaconManager(context);
        if (beaconManager.hasBluetooth()) {
            mBeaconManager = beaconManager;
            mBeaconManager.setForegroundScanPeriod(1000, Defaults.POI_SCAN_DELAY_PERIOD);
            mBeaconManager.setRangingListener(this.mProcessor = new Processor(mContext));

            context.registerReceiver(
                    mBluetoothListener = new BluetoothListener(mProcessor),
                    new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        } else {
            mBeaconManager = null;
            mBluetoothListener = null;
            mProcessor = null;
        }
    }

    public boolean hasBluetoothLE() {
        return mBeaconManager != null;
    }

    public EstimoteManager setRSSIThreshold(int rssiThreshold) {

        if (!hasBluetoothLE()) {
            return this;
        }

        mProcessor.setRssiThreshold(rssiThreshold);

        return this;
    }

    public EstimoteManager setEnterThreshold(long enterThreshold) {

        if (!hasBluetoothLE()) {
            return this;
        }

        mProcessor.setEnterThreshold(enterThreshold);

        return this;
    }

    public EstimoteManager setExitThreshold(long exitThreshold) {

        if (!hasBluetoothLE()) {
            return this;
        }

        mProcessor.setExitThreshold(exitThreshold);

        return this;
    }

    public EstimoteManager setScanDelay(long scanDelay) {

        if (!hasBluetoothLE()) {
            return this;
        }

        mBeaconManager.setForegroundScanPeriod(1000, scanDelay);

        return this;
    }

    public EstimoteManager setInPOIPeriod(long inPOIPeriod) {

        if (!hasBluetoothLE()) {
            return this;
        }

        mProcessor.setUpdateTimeout(inPOIPeriod);

        return this;
    }

    public void start() {

        if (!hasBluetoothLE()) {
            return;
        }

        if (mIsListening) {
            throw new IllegalStateException();
        }

        mBeaconManager.connect(new com.estimote.sdk.BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                String message = System.currentTimeMillis() + ",";

                try {
                    mBeaconManager.startRanging(mAllEstimoteBeacons);
                    message += "ranging_start\r\n";
                } catch (RemoteException e) {
                    message += e + "\r\n";
                    Log.e(Constant.TAG, "Cannot start ranging", e);
                }

                if (mLogger != null) {
                    mLogger.log(message);
                }
            }
        });

        mIsListening = true;
    }

    public void stop() {

        if (!hasBluetoothLE()) {
            return;
        }

        if (!mIsListening) {
            throw new IllegalArgumentException();
        }

        String message = System.currentTimeMillis() + ",";

        // Should be invoked in #onStop.
        try {
            mBeaconManager.stopRanging(mAllEstimoteBeacons);
            message += "ranging_stop\r\n";
        } catch (RemoteException e) {
            message += e + "\r\n";
            Log.e(Constant.TAG, "Cannot stop but it does not matter now", e);
        }

        mIsListening = false;
        if (mLogger != null) {
            mLogger.log(message);
        }
    }

    public void onTerminate() {

        if (!hasBluetoothLE()) {
            return;
        }

        if (isListening()) {
            stop();
        }

        mContext.unregisterReceiver(mBluetoothListener);
    }

    public boolean isListening() {
        return mIsListening;
    }
}
