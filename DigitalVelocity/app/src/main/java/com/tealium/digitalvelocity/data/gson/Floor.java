package com.tealium.digitalvelocity.data.gson;


import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * TODO
 * 04-18 09:34:38.563  2844  2898 E DigitalVelocity: org.json.JSONException: No value for priority
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at org.json.JSONObject.get(JSONObject.java:389)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at org.json.JSONObject.getInt(JSONObject.java:478)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at com.tealium.digitalvelocity.data.gson.Floor.<init>(Floor.java:21)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at com.tealium.digitalvelocity.parse.SyncManager.processLocationData(SyncManager.java:388)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at com.tealium.digitalvelocity.parse.SyncManager.onEventBackgroundThread(SyncManager.java:146)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at java.lang.reflect.Method.invoke(Native Method)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at de.greenrobot.event.EventBus.invokeSubscriber(EventBus.java:498)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at de.greenrobot.event.EventBus.postToSubscription(EventBus.java:442)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at de.greenrobot.event.EventBus.postSingleEventForEventType(EventBus.java:410)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at de.greenrobot.event.EventBus.postSingleEvent(EventBus.java:383)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at de.greenrobot.event.EventBus.post(EventBus.java:263)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at com.tealium.digitalvelocity.parse.SyncManager$1.doInBackground(SyncManager.java:423)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at com.tealium.digitalvelocity.parse.SyncManager$1.doInBackground(SyncManager.java:410)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at android.os.AsyncTask$2.call(AsyncTask.java:295)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at java.util.concurrent.FutureTask.run(FutureTask.java:237)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1113)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:588)
 04-18 09:34:38.563  2844  2898 E DigitalVelocity: 	at java.lang.Thread.run(Thread.java:818)
 04-18 09:34:38.601  2844  2931 V Dig
 * */
public final class Floor extends ParseItem implements Comparable<Floor> {
    private String name;
    private String imageUri;
    private int priority;

    public Floor(JSONObject o) throws JSONException {
        super(o);

        this.name = o.getString("title");
        this.priority = o.optInt("priority", 0);
        this.imageUri = o.getJSONObject("imageData").getString("url");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Floor floor = (Floor) o;

        return this.getId().equals(floor.getId());

    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    public String getName() {
        return name;
    }

    public String getImageUri() {
        return imageUri;
    }

    public static File getFloorsDir(Context context) {
        return new File(context.getFilesDir(), "floor");
    }

    @Override
    public int compareTo(@NonNull Floor another) {
        return this.priority - another.priority;
    }
}

