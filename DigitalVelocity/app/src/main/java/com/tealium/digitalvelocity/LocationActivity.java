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

    private MapController mMapController;
    private LayoutController mLayoutController;
    private BaseController mSelectedController;
    private String mLocationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mMapController = new MapController(this);
        mLayoutController = new LayoutController(this);

        ((RadioGroup) findViewById(R.id.location_radiogroup_categories))
                .setOnCheckedChangeListener(this.createCategoryChangeListener());

        (mSelectedController = mMapController).select();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (getIntent() != null) {
            mLocationId = getIntent().getStringExtra(EXTRA_LOCATION_ID);
        }

        EventBus bus = EventBus.getDefault();

        if (!bus.isRegistered(mMapController)) {
            bus.register(mMapController);
        }

        if (!bus.isRegistered(mLayoutController)) {
            bus.register(mLayoutController);
        }

        if (!bus.isRegistered(this)) {
            bus.register(this);
        }

        toggleActivityIndicator(true);

        bus.post(new LoadRequest.Coordinates());
        bus.post(new LoadRequest.Floors());
        findViewById(R.id.location_label_none).setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {

        EventBus bus = EventBus.getDefault();
        bus.unregister(mMapController);
        bus.unregister(mLayoutController);
        bus.unregister(this);

        super.onStop();
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_BACK_PRESSED);
        super.onBackPressed();
    }

    private void setController(BaseController controller) {
        mSelectedController.deselect();
        (mSelectedController = controller).select();
    }

    private RadioGroup.OnCheckedChangeListener createCategoryChangeListener() {
        return new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.location_radio_category_map:
                        setController(mMapController);
                        break;
                    case R.id.location_radio_category_layout:
                        setController(mLayoutController);
                        break;
                }
            }
        };
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.CoordinateData event) {

        if (mSelectedController == mMapController) {
            toggleActivityIndicator(false);
        }

        if (mLocationId == null) {
            return;
        }

        for (Coordinates coords : event.getItems()) {
            if (mLocationId.equals(coords.getId())) {
                findViewById(R.id.location_radio_category_map).performClick();
                mMapController.selectLocation(mLocationId);
                return;
            }
        }

        Log.w(Constant.TAG, "Unknown coordinate: " + mLocationId);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Floors event) {

        if (mSelectedController == mLayoutController) {
            toggleActivityIndicator(false);
        }

        if (mLocationId == null) {
            return;
        }

        for (Floor floor : event.getItems()) {
            if (mLocationId.equals(floor.getId())) {
                findViewById(R.id.location_radio_category_layout).performClick();
                mLayoutController.selectLocation(mLocationId);
                return;
            }
        }

        Log.w(Constant.TAG, "Unknown floor: " + mLocationId);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncCompleteEvent.ParseLocation event) {
        EventBus bus = EventBus.getDefault();
        bus.post(new LoadRequest.Coordinates());
        bus.post(new LoadRequest.Floors());
    }

    private void toggleActivityIndicator(boolean shouldShow) {

        final View activityIndicator = findViewById(R.id.location_activity_indicator);
        final View content = findViewById(R.id.location_content);

        if (shouldShow) {
            activityIndicator.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);
        } else {
            activityIndicator.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
        }
    }

}
