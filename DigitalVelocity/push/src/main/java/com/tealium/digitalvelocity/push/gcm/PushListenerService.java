package com.tealium.digitalvelocity.push.gcm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.tealium.digitalvelocity.push.BuildConfig;
import com.tealium.digitalvelocity.push.R;
import com.tealium.digitalvelocity.push.event.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

public class PushListenerService extends GcmListenerService {

    public static final int NOTIFICATION_ID = 1;
    private static Class<? extends Activity> pendingIntentActivityClass;

    public static void setPendingIntentActivityClass(Class<? extends Activity> pendingIntentActivityClass) {
        PushListenerService.pendingIntentActivityClass = pendingIntentActivityClass;
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {

        if (!data.isEmpty()) {  // has effect of unparcelling Bundle
            sendNotification(data.getString("data"));
        }
    }


    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        try {
            JSONObject o = new JSONObject(msg);
            final String content = o.getString("alert");
            if (content != null && content.length() > 0) {
                EventBus.getDefault().post(new PushMessage(content));
            }

            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, pendingIntentActivityClass), 0);

            final int tealiumBlue = Color.rgb(0, 125, 195);

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                            .setAutoCancel(true)
                            .setColor(tealiumBlue)
                            .setOnlyAlertOnce(true)
                            .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                            .setShowWhen(true)
                            .setLights(tealiumBlue, 1000, 1000)
                            .setSmallIcon(R.mipmap.dv_logo)
                            .setContentTitle(o.optString("title", this.getString(R.string.app_name)))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setContentText(content);

            builder.setContentIntent(contentIntent);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        } catch (JSONException | NullPointerException e) {
            Log.e("DVPush", "Error Parsing PARSE Push: " + msg, e);
        }
    }
}