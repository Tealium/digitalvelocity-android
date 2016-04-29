package com.tealium.digitalvelocity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import com.tealium.digitalvelocity.data.gson.Coordinates;
import com.tealium.digitalvelocity.data.gson.Floor;
import com.tealium.digitalvelocity.event.LoadRequest;
import com.tealium.digitalvelocity.event.LoadedEvent;
import com.tealium.digitalvelocity.event.SyncCompleteEvent;
import com.tealium.digitalvelocity.location.BaseController;
import com.tealium.digitalvelocity.location.LayoutController;
import com.tealium.digitalvelocity.location.MapController;
import com.tealium.digitalvelocity.util.Constant;

import de.greenrobot.event.EventBus;


public final class LocationActivity extends DrawerLayoutActivity {

    public static final int RESULT_BACK_PRESSED = 2;
    public static final String EXTRA_LOCATION_ID = "location_id";

    private MapController mapController;
    private LayoutController layoutController;
    private BaseController selectedController;
    private String locationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        this.mapController = new MapController(this);
        this.layoutController = new LayoutController(this);

        ((RadioGroup) this.findViewById(R.id.location_radiogroup_categories))
                .setOnCheckedChangeListener(this.createCategoryChangeListener());

        //this.setController(this.selectedController = mapController);
        (this.selectedController = mapController).select();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (this.getIntent() != null) {
            this.locationId = this.getIntent().getStringExtra(EXTRA_LOCATION_ID);
        }

        EventBus bus = EventBus.getDefault();

        if (!bus.isRegistered(this.mapController)) {
            bus.register(this.mapController);
        }

        if (!bus.isRegistered(this.layoutController)) {
            bus.register(this.layoutController);
        }

        if (!bus.isRegistered(this)) {
            bus.register(this);
        }

        bus.post(new LoadRequest.Coordinates());
        bus.post(new LoadRequest.Floors());

    }

    @Override
    protected void onStop() {

        EventBus bus = EventBus.getDefault();
        bus.unregister(this.mapController);
        bus.unregister(this.layoutController);
        bus.unregister(this);

        super.onStop();
    }


    @Override
    public void onBackPressed() {
        this.setResult(RESULT_BACK_PRESSED);
        super.onBackPressed();
    }

    private void setController(BaseController controller) {
        this.selectedController.deselect();
        (this.selectedController = controller).select();
    }

    private RadioGroup.OnCheckedChangeListener createCategoryChangeListener() {
        return new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.location_radio_category_map:
                        setController(mapController);
                        break;
                    case R.id.location_radio_category_layout:
                        setController(layoutController);
                        break;
                }
            }
        };
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.CoordinateData event) {

        this.toggleActivityIndicator(event.getItems().size());

        if (this.locationId == null) {
            return;
        }

        for (Coordinates coords : event.getItems()) {
            if (this.locationId.equals(coords.getId())) {
                this.findViewById(R.id.location_radio_category_map).performClick();
                this.mapController.selectLocation(this.locationId);
                break;
            }
        }

        Log.w(Constant.TAG, "Unknown coordinate: " + this.locationId);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Floors event) {

        this.toggleActivityIndicator(event.getItems().size());

        if (this.locationId == null) {
            return;
        }

        for (Floor floor : event.getItems()) {
            if (this.locationId.equals(floor.getId())) {
                this.findViewById(R.id.location_radio_category_layout).performClick();
                this.layoutController.selectLocation(this.locationId);
                break;
            }
        }

        Log.w(Constant.TAG, "Unknown floor: " + this.locationId);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncCompleteEvent.ParseLocation event) {
        EventBus bus = EventBus.getDefault();
        bus.post(new LoadRequest.Coordinates());
        bus.post(new LoadRequest.Floors());
    }

    private void toggleActivityIndicator(int itemsSize) {

        final View activityIndicator = this.findViewById(R.id.location_activity_indicator);
        final View content = this.findViewById(R.id.location_content);

        if (itemsSize == 0) {
            activityIndicator.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);
        } else {
            activityIndicator.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
        }
    }

}
