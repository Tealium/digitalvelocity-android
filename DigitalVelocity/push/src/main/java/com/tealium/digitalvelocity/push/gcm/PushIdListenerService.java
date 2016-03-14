package com.tealium.digitalvelocity.push.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * I don't know why google separates the "InstanceIDListenerService" and the "Registration IntentService", but this works.
 */
public class PushIdListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationService.class);
        startService(intent);
    }
}
