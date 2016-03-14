package com.tealium.digitalvelocity.drawerlayout;


import android.app.Activity;

import com.tealium.digitalvelocity.AgendaActivity;
import com.tealium.digitalvelocity.ContactActivity;
import com.tealium.digitalvelocity.DemoActivity;
import com.tealium.digitalvelocity.LocationActivity;
import com.tealium.digitalvelocity.NotificationsActivity;
import com.tealium.digitalvelocity.SnowshoeActivity;
import com.tealium.digitalvelocity.SponsorsActivity;

public enum DrawerItem {

    // TODO: localize
    WELCOME("Welcome", SnowshoeActivity.class),
    AGENDA("Agenda", AgendaActivity.class),
    EVENT_LOCATION("Event Location", LocationActivity.class),
    NOTIFICATIONS("Notifications", NotificationsActivity.class),
    SPONSORS("Sponsors", SponsorsActivity.class),
    CONTACT("Contact", ContactActivity.class),
    DEMO("Demo", DemoActivity.class),
    CHAT("Chat", null);

    private final String name;
    private final Class<? extends Activity> activityClass;

    DrawerItem(String name, Class<? extends Activity> activityClass) {
        this.name = name;
        this.activityClass = activityClass;
    }

    public Class<? extends Activity> getActivityClass() {
        return activityClass;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
