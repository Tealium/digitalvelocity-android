package com.tealium.digitalvelocity.location;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.tealium.digitalvelocity.R;

public abstract class BaseController {

    private final Activity activity;
    private final FrameLayout contentView;
    private final RadioGroup optionRadioGroup;
    private boolean isSelected;

    protected BaseController(Activity activity) {
        this.activity = activity;
        this.contentView = (FrameLayout) activity.findViewById(R.id.location_content);
        this.optionRadioGroup = (RadioGroup) activity.findViewById(R.id.location_radiogroup_options);
    }

    protected final Activity getActivity() {
        return activity;
    }

    protected final FrameLayout getContentView() {
        return contentView;
    }

    protected final RadioGroup getOptionRadioGroup() {
        return optionRadioGroup;
    }

    public final boolean isSelected() {
        return isSelected;
    }

    public final BaseController deselect() {
        this.isSelected = false;
        this.onDeselected();
        return this;
    }

    public final BaseController select() {
        this.isSelected = true;
        this.refresh();
        return this;
    }

    public abstract boolean selectLocation(String locationId);

    protected final void refresh() {
        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        RadioGroup group = this.getOptionRadioGroup();

        final int optionCount = this.getOptionCount();

        while (group.getChildCount() < optionCount) {
            group.addView(inflater.inflate(R.layout.radio_location_option, group, false));
        }

        while (group.getChildCount() > optionCount) {
            group.removeViewAt(0);
        }

        for (int i = 0; i < this.getOptionCount(); i++) {
            this.populateRadio(i, (RadioButton) group.getChildAt(i));
        }

        this.onRefresh();
    }

    protected void onDeselected() {
    }

    protected abstract void onRefresh();

    protected abstract int getOptionCount();

    protected abstract void populateRadio(int position, RadioButton button);
}
