package com.tealium.digitalvelocity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tealium.digitalvelocity.agenda.AgendaAdapter;
import com.tealium.digitalvelocity.agenda.LocationClickEvent;
import com.tealium.digitalvelocity.data.gson.AgendaItem;
import com.tealium.digitalvelocity.event.LoadRequest;
import com.tealium.digitalvelocity.event.LoadedEvent;
import com.tealium.digitalvelocity.event.SyncCompleteEvent;
import com.tealium.digitalvelocity.event.TrackEvent;
import com.tealium.library.DataSources;

import de.greenrobot.event.EventBus;


public final class AgendaActivity extends DrawerLayoutActivity {

    public static final int REQUEST_VIEW_LOCATION = 1;
    public static final int REQUEST_VIEW_AGENDA_DETAIL = 2;

    private AgendaAdapter adapter;
    private boolean shouldScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        final ListView listView = (ListView) this.findViewById(R.id.agenda_root);
        listView.setAdapter(this.adapter = new AgendaAdapter(
                this.findViewById(R.id.agenda_label_nofavs)));
        listView.setOnItemClickListener(createItemClickListener());
        this.shouldScroll = true;
    }

    @Override
    protected boolean isFilteringFavorites() {
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus bus = EventBus.getDefault();
        if (!bus.isRegistered(this.adapter)) {
            bus.registerSticky(this.adapter);
        }

        if (!bus.isRegistered(this)) {
            bus.register(this);
        }

        bus.post(new LoadRequest.Agenda());
    }

    @Override
    protected void onStop() {
        EventBus bus = EventBus.getDefault();
        bus.unregister(this.adapter);
        bus.unregister(this);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_VIEW_LOCATION:
                this.shouldScroll = resultCode != LocationActivity.RESULT_BACK_PRESSED;
                break;
            case REQUEST_VIEW_AGENDA_DETAIL:
                if (resultCode == AgendaDetailActivity.RESULT_FAVORITE_TOGGLED) {
                    this.adapter.restageItems();
                    this.adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncCompleteEvent.ParseEvent event) {
        EventBus.getDefault().post(new LoadRequest.Agenda());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Agenda event) {

        final View activityIndicator = this.findViewById(R.id.agenda_activity_indicator);

        if (event.getItems().size() == 0) {
            activityIndicator.setVisibility(View.VISIBLE);
            return;
        }

        activityIndicator.setVisibility(View.GONE);

        if (!this.shouldScroll) {
            this.shouldScroll = true;
            //return;
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LocationClickEvent event) {
        this.startActivityForResult(new Intent(this, LocationActivity.class)
                        .putExtra(LocationActivity.EXTRA_LOCATION_ID, event.getLocationId())
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                REQUEST_VIEW_LOCATION);
    }

    @Override
    protected void onFilterClick(boolean shouldFilterFavorites) {
        this.adapter.setFilteringFavorites(shouldFilterFavorites);
    }

    private AdapterView.OnItemClickListener createItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Object o = adapter.getItem(position);

                if (!(o instanceof AgendaItem)) {
                    return;
                }

                final AgendaItem agendaItem = (AgendaItem) o;

                EventBus.getDefault().post(TrackEvent.createLinkTrackEvent()
                        .add(DataSources.Key.LINK_ID, "agenda_selected")
                        .add("agenda_title", agendaItem.getTitle())
                        .add("agenda_subtitle", agendaItem.getSubtitle())
                        .add("agenda_objectid", agendaItem.getId()));

                if (agendaItem.getUrl() != null) {
                    startActivity(new Intent(AgendaActivity.this, WebViewActivity.class)
                            .putExtra(WebViewActivity.EXTRA_TITLE, agendaItem.getTitle())
                            .setData(Uri.parse(agendaItem.getUrl())));
                    return;
                }

                startActivityForResult(new Intent(AgendaActivity.this, AgendaDetailActivity.class)
                                .putExtra(AgendaDetailActivity.EXTRA_AGENDA_ITEM_ID, agendaItem.getId()),
                        REQUEST_VIEW_AGENDA_DETAIL);
            }
        };
    }
}
