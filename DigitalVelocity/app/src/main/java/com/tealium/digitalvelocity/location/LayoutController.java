package com.tealium.digitalvelocity.location;


import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RadioButton;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.IOUtils;
import com.tealium.digitalvelocity.data.gson.Floor;
import com.tealium.digitalvelocity.event.LoadedEvent;
import com.tealium.digitalvelocity.event.SyncCompleteEvent;
import com.tealium.digitalvelocity.util.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LayoutController extends BaseController implements View.OnClickListener {

    private final List<Floor> mFloors = new ArrayList<>(2);
    private final View mLoadingIndicator;
    private final WebView mWebView;
    private Floor mSelectedFloor;

    public LayoutController(Activity activity) {
        super(activity);

        mWebView = (WebView) getContentView().findViewById(R.id.location_content_layout);
        mLoadingIndicator = activity.findViewById(R.id.location_activity_indicator);

        WebSettings settings = mWebView.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Floors floors) {
        mFloors.clear();
        mFloors.addAll(floors.getItems());
        if (isSelected()) {
            refresh();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(SyncCompleteEvent.Image event) {
        if (mSelectedFloor != null && mSelectedFloor.getId().equals(event.getId())) {
            loadSelectedFloor();
        }
    }

    @Override
    protected void onDeselected() {
        mSelectedFloor = null;
        mWebView.setVisibility(View.GONE);
        mLoadingIndicator.setVisibility(View.GONE);
    }

    @Override
    protected void onRefresh() {
        mWebView.setVisibility(View.VISIBLE);
        loadSelectedFloor();
    }

    @Override
    public int getOptionCount() {

        final int count = mFloors.size();

        if (count > 0 && mSelectedFloor == null) {
            // Sets first by default.
            this.mSelectedFloor = this.mFloors.get(0);
        }

        return this.mFloors.size();
    }

    @Override
    protected void populateRadio(int position, RadioButton button) {

        Floor floor = mFloors.get(position);
        button.setText(floor.getName());
        button.setTag(floor);
        button.setOnClickListener(this);
        button.setChecked(floor.equals(mSelectedFloor));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void onClick(View v) {
        mSelectedFloor = (Floor) v.getTag();
        loadSelectedFloor();
    }

    @Override
    public boolean selectLocation(String locationId) {
        for (Floor floor : mFloors) {
            if (floor.getId().equals(locationId)) {
                mSelectedFloor = floor;
                refresh();
                return true;
            }
        }


        return false;
    }

    private void loadSelectedFloor() {
        if (mSelectedFloor == null) {
            Log.w(Constant.TAG, "No floor selected.");
            mWebView.loadUrl(null);
            return;
        }

        final File imageFile = IOUtils.getImageFile(
                getActivity(), mSelectedFloor.getId());

        if (imageFile != null) {
            final String html = String.format(Locale.ROOT,
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "   <body style=\"background-color: black;\"> " +
                            "       <img " +
                            "           style=\"position: absolute; " +
                            "               top: 10%%; " +
                            //"               transform: translate(0%%, -50%%);" +
                            "               \"" +
                            "           src=\"%s\" " +
                            "           alt=\"Floor Image\"/>" +
                            "   </body>" +
                            "</html>",
                    Uri.fromFile(imageFile).toString());

            mWebView.loadDataWithBaseURL("", html, "text/html", "UTF-8", "");
            mWebView.setVisibility(View.VISIBLE);
            mLoadingIndicator.setVisibility(View.GONE);
        } else {
            mWebView.setVisibility(View.GONE);
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
    }
}
