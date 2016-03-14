package com.tealium.digitalvelocity.push.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.tealium.digitalvelocity.push.PushManager;
import com.tealium.digitalvelocity.push.R;
import com.tealium.digitalvelocity.push.event.PushTokenUpdateEvent;

import de.greenrobot.event.EventBus;

public class RegistrationService extends IntentService {

    public RegistrationService() {
        super("RegistrationIntentService ");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final SharedPreferences.Editor editor = getSharedPreferences(PushManager.SHARED_PREFS_NAME, 0).edit();
        String token = null;

        try {
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            final InstanceID instanceID = InstanceID.getInstance(this);
            token = instanceID.getToken(PushManager.getGcmSenderId(this),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            editor.putString(PushManager.KEY_GCM_TOKEN, token);
        } catch (Exception e) {
            Log.e(getPackageName(), "Error retrieving token", e);
            editor.remove(PushManager.KEY_GCM_TOKEN);
        }

        EventBus.getDefault().post(new PushTokenUpdateEvent(token));

        editor.commit();
    }
}