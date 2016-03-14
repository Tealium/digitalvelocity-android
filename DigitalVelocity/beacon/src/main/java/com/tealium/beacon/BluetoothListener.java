package com.tealium.beacon;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

final class BluetoothListener extends BroadcastReceiver {

    private final Processor mProcessor;

    public BluetoothListener(Processor processor) {
        mProcessor = processor;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            final EstimoteManager mgr = EstimoteManager.getInstance();

            if (state == BluetoothAdapter.STATE_TURNING_OFF || state == BluetoothAdapter.STATE_OFF) {
                mProcessor.onBluetoothDisable();

                if (mgr.isListening()) {
                    mgr.stop();
                }
            }
        }
    }
}
