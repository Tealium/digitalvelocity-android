package com.tealium.digitalvelocity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.tealium.digitalvelocity.event.LoadRequest;
import com.tealium.digitalvelocity.event.LoadedEvent;
import com.tealium.digitalvelocity.event.SyncCompleteEvent;
import com.tealium.digitalvelocity.sponsor.SponsorAdapter;

import de.greenrobot.event.EventBus;


public final class SponsorsActivity extends DrawerLayoutActivity {

    private SponsorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsors);

        ListView sponsorsListView = ((ListView) this.findViewById(R.id.sponsors_root));
        sponsorsListView.setAdapter(this.adapter = new SponsorAdapter(this));
        sponsorsListView.setOnItemClickListener(adapter);
        sponsorsListView.setOnItemLongClickListener(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus bus = EventBus.getDefault();
        if (!bus.isRegistered(this.adapter)) {
            bus.register(this.adapter);
        }

        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(new LoadRequest.Sponsors());
    }

    @Override
    protected void onStop() {
        EventBus bus = EventBus.getDefault();
        bus.unregister(this.adapter);
        bus.unregister(this);
        super.onStop();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Sponsors event) {
        this.findViewById(R.id.sponsors_activity_indicator)
                .setVisibility(event.getItems().size() == 0 ? View.VISIBLE : View.GONE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncCompleteEvent.ParseCompany event) {
        EventBus.getDefault().post(new LoadRequest.Sponsors());
    }
}
