package com.tealium.digitalvelocity.location;

import android.app.Activity;
import android.view.View;
import android.widget.RadioButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.gson.Coordinates;
import com.tealium.digitalvelocity.event.LoadedEvent;

import java.util.ArrayList;
import java.util.List;


public class MapController extends BaseController implements View.OnClickListener {

    private final MapFragment mMapFragment;
    private final List<Coordinates> mCoordinates = new ArrayList<>(2);
    private final View mMapFragmentContainer;
    private GoogleMap mMap;
    private Coordinates mSelectedCoords;

    public MapController(Activity activity) {
        super(activity);

        mMapFragmentContainer = activity.findViewById(R.id.location_content_map_holder);

        mMapFragment = (MapFragment) activity.getFragmentManager()
                        .findFragmentById(R.id.location_content_map);
        mMapFragment.getMapAsync(createMapReadyCallback());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.CoordinateData coordinateData) {
        mCoordinates.clear();
        mCoordinates.addAll(coordinateData.getItems());
        if (isSelected()) {
            refresh();
            loadSelectedCoordinate();
        }
    }

    @Override
    protected void onDeselected() {
        mSelectedCoords = null;
        mMapFragmentContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onRefresh() {
        mMapFragmentContainer.setVisibility(View.VISIBLE);
        loadSelectedCoordinate();
    }

    @Override
    protected int getOptionCount() {
        final int count = mCoordinates.size();
        if (count > 0 && mSelectedCoords == null) {
            // Select 1st if none selected.
            mSelectedCoords = mCoordinates.get(0);
        }
        return count;
    }

    @Override
    protected void populateRadio(int position, RadioButton button) {
        final Coordinates coords = mCoordinates.get(position);
        button.setText(coords.getName());
        button.setTag(coords);
        button.setOnClickListener(this);
        button.setChecked(coords.equals(mSelectedCoords));
    }

    @Override
    public void onClick(View v) {
        mSelectedCoords = (Coordinates) v.getTag();
        loadSelectedCoordinate();
    }

    @Override
    public boolean selectLocation(String locationId) {
        for (Coordinates coords : mCoordinates) {
            if (coords.getId().equals(locationId)) {
                mSelectedCoords = coords;
                refresh();
                return true;
            }
        }

        return false;
    }

    private OnMapReadyCallback createMapReadyCallback() {
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                loadSelectedCoordinate();
            }
        };
    }

    private void loadSelectedCoordinate() {
        if (mSelectedCoords == null || mMap == null) {
            return;
        }

        LatLng loc = mSelectedCoords.toLatLng();

        mMap.clear();

        // TODO REMOVE MAGIC NUM
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
        mMap.addMarker(new MarkerOptions()
                .position(loc)
                .title(mSelectedCoords.getName()));
    }
}
