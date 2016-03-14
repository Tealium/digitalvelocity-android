package com.tealium.digitalvelocity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.util.Constant;

public final class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            // Alarms don't persist across boots
            Model.getInstance().getSharedPreferences().edit()
                    .remove(Constant.SP.KEY_ALARM_HOUR_START)
                    .remove(Constant.SP.KEY_ALARM_HOUR_STOP)
                    .commit();

            // Schedules
            AlarmReceiver.setup(context);
            BeaconService.start(context);
        }
    }
}
