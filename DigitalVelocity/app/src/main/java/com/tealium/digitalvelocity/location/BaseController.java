package com.tealium.digitalvelocity.location;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.tealium.digitalvelocity.R;

public abstract class BaseController {

    private final Activity mActivity;
    private final FrameLayout mContentView;
    private final RadioGroup mOptionRadioGroup;
    private boolean mIsSelected;

    protected BaseController(Activity activity) {
        mActivity = activity;
        mContentView = (FrameLayout) activity.findViewById(R.id.location_content);
        mOptionRadioGroup = (RadioGroup) activity.findViewById(R.id.location_radiogroup_options);
    }

    protected final Activity getActivity() {
        return mActivity;
    }

    protected final FrameLayout getContentView() {
        return mContentView;
    }

    protected final RadioGroup getOptionRadioGroup() {
        return mOptionRadioGroup;
    }

    public final boolean isSelected() {
        return mIsSelected;
    }

    public final BaseController deselect() {
        mIsSelected = false;
        onDeselected();
        return this;
    }

    public final BaseController select() {
        mIsSelected = true;
        refresh();
        return this;
    }

    public abstract boolean selectLocation(String locationId);

    protected final void refresh() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        RadioGroup group = getOptionRadioGroup();

        final int optionCount = getOptionCount();

        getActivity().findViewById(R.id.location_label_none)
                .setVisibility(optionCount == 0 ? View.VISIBLE : View.GONE);

        while (group.getChildCount() < optionCount) {
            group.addView(inflater.inflate(R.layout.radio_location_option, group, false));
        }

        while (group.getChildCount() > optionCount) {
            group.removeViewAt(0);
        }

        for (int i = 0; i < getOptionCount(); i++) {
            populateRadio(i, (RadioButton) group.getChildAt(i));
        }

        onRefresh();
    }

    protected void onDeselected() {
    }

    protected abstract void onRefresh();

    public abstract int getOptionCount();

    protected abstract void populateRadio(int position, RadioButton button);
}
