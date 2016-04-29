package com.tealium.digitalvelocity.drawerlayout;


import android.app.Activity;

import com.tealium.digitalvelocity.AgendaActivity;
import com.tealium.digitalvelocity.ContactActivity;
import com.tealium.digitalvelocity.DemoActivity;
import com.tealium.digitalvelocity.LocationActivity;
import com.tealium.digitalvelocity.NotificationsActivity;
import com.tealium.digitalvelocity.SnowshoeActivity;
import com.tealium.digitalvelocity.SponsorsActivity;
import com.tealium.digitalvelocity.SurveyActivity;

public final class DrawerItem {

    private final String mName;
    private final Class<? extends Activity> mActivityClass;

    private DrawerItem(String name, Class<? extends Activity> activityClass) {
        mName = name;
        mActivityClass = activityClass;
    }

    public Class<? extends Activity> getActivityClass() {
        return mActivityClass;
    }

    @Override
    public String toString() {
        return mName;
    }

    public static synchronized DrawerItem[] values() {

        return new DrawerItem[]{
                WELCOME,
                AGENDA,
                EVENT_LOCATION,
                NOTIFICATIONS,
                SPONSORS,
                CONTACT,
                DEMO,
                SURVEY
        };

    }

    public static final DrawerItem WELCOME = new DrawerItem("Welcome", SnowshoeActivity.class);
    public static final DrawerItem AGENDA = new DrawerItem("Agenda", AgendaActivity.class);
    public static final DrawerItem EVENT_LOCATION = new DrawerItem("Event Location", LocationActivity.class);
    public static final DrawerItem NOTIFICATIONS = new DrawerItem("Notifications", NotificationsActivity.class);
    public static final DrawerItem SPONSORS = new DrawerItem("Sponsors", SponsorsActivity.class);
    public static final DrawerItem CONTACT = new DrawerItem("Contact", ContactActivity.class);
    public static final DrawerItem DEMO = new DrawerItem("Demo", DemoActivity.class);
    public static final DrawerItem SURVEY = new DrawerItem("Survey", SurveyActivity.class);
}


