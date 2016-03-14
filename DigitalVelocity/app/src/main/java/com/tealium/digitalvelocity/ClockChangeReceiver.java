package com.tealium.digitalvelocity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.util.Constant;


public final class ClockChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (BuildConfig.DEBUG) Log.d(Constant.TAG, "! Clock was changed, removing last sync keys.");
        Model.getInstance().clearCacheKeys();
    }
}
