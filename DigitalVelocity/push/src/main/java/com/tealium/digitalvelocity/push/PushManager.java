package com.tealium.digitalvelocity.push;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.tealium.digitalvelocity.push.gcm.RegistrationService;

public final class PushManager {

    public static final String KEY_GCM_TOKEN = "gcm_token";
    public static final String SHARED_PREFS_NAME = "com.tealium.digitalvelocity.push";

    private PushManager() {
    }

    /**
     * Returns whether the token is generatable
     */
    public static boolean generateGcmToken(Context context) {
        if (isPlayServicesAvailable(context)) {
            context.startService(new Intent(context, RegistrationService.class));
            return true;
        }

        return false;
    }

    public static String getGcmSenderId(Context context) {
        return context.getString(R.string.gcm_defaultSenderId);
    }

    public static String getGcmPushToken(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, 0).getString(KEY_GCM_TOKEN, null);
    }

    private static boolean isPlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }
}
