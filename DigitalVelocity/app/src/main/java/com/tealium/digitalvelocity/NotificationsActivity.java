package com.tealium.digitalvelocity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.event.LoadRequest;
import com.tealium.digitalvelocity.event.LoadedEvent;
import com.tealium.digitalvelocity.notifications.NotificationsAdapter;

import de.greenrobot.event.EventBus;


public final class NotificationsActivity extends DrawerLayoutActivity {

    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ((ListView) this.findViewById(R.id.notifications_root))
                .setAdapter(this.adapter = new NotificationsAdapter());

        if (Model.getInstance().getUserEmail() == null) {
            startActivity(new Intent(this, EmailActivity.class));
        }

        this.register();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.register();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(new LoadRequest.Notifications());
    }

    @Override
    protected void onStop() {
        EventBus bus = EventBus.getDefault();
        bus.unregister(this.adapter);
        bus.unregister(this);
        super.onStop();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Notifications notifications) {
        this.findViewById(R.id.notifications_label_none)
                .setVisibility(notifications.getItems().size() == 0 ? View.VISIBLE : View.GONE);
    }

    private void register() {
        EventBus bus = EventBus.getDefault();

        if (!bus.isRegistered(this.adapter)) {
            bus.register(this.adapter);
        }

        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
    }
}
