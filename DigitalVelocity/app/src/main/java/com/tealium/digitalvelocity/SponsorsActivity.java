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

    private SponsorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsors);

        ListView sponsorsListView = ((ListView) this.findViewById(R.id.sponsors_root));
        sponsorsListView.setAdapter(mAdapter = new SponsorAdapter(this));
        sponsorsListView.setOnItemClickListener(mAdapter);
        sponsorsListView.setOnItemLongClickListener(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus bus = EventBus.getDefault();
        if (!bus.isRegistered(mAdapter)) {
            bus.register(mAdapter);
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
        bus.unregister(mAdapter);
        bus.unregister(this);
        super.onStop();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Sponsors event) {

        findViewById(R.id.sponsors_activity_indicator)
                .setVisibility(View.GONE);

        findViewById(R.id.sponsors_label_none)
                .setVisibility(event.getItems().size() == 0 ? View.VISIBLE : View.GONE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncCompleteEvent.ParseCompany event) {
        EventBus.getDefault().post(new LoadRequest.Sponsors());
    }
}
