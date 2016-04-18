package com.tealium.digitalvelocity.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tealium.digitalvelocity.R;

/**
 * This class was created because there's a bug in several Android versions with RadioGroup.
 * <p/>
 * If a RadioGroup is in a ListView cell, when recycled with a different RadioButton checked; the
 * API confirms an option is checked, but never draws the checked icon. No styling can fix this bug,
 * so a custom ViewGroup must be created.
 */
public final class DVRadioGroup extends LinearLayout {

    public static final int NO_SELECTION = -1;
    public static final int RADIO_CHECKED_TEXT = R.string.fa_dot_circle_o;
    public static final int RADIO_UNCHECKED_TEXT = R.string.fa_circle_o;
    public static final int RADIO_PRESSED_TEXT = R.string.fa_circle;


    private final LayoutInflater mInflater;

    private OptionSelectedListener mOptionSelectedListener;
    private int mCheckedIndex;

    {
        mInflater = LayoutInflater.from(getContext());
        mCheckedIndex = NO_SELECTION;
        setOrientation(LinearLayout.VERTICAL);
    }

    public DVRadioGroup(Context context) {
        super(context);
    }

    public DVRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DVRadioGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DVRadioGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getCheckedIndex() {
        return mCheckedIndex;
    }

    /**
     * @param position  - must be a valid position
     * @param isChecked - whether this is the current selected child (if multiple specified
     *                  getCheckedIndex() returns last specified)
     * @param text      - new text for child, if null, does not change text
     */
    public void setChild(int position, boolean isChecked, CharSequence text) {

        final View child = getChildAt(position);
        final TextView iconView = ((TextView) child.findViewById(R.id.dvradiogroup_item_image));
        if (isChecked) {
            iconView.setText(RADIO_CHECKED_TEXT);
            mCheckedIndex = position;
        } else {
            iconView.setText(RADIO_UNCHECKED_TEXT);
        }

        if (text != null) {
            ((TextView) child.findViewById(R.id.dvradiogroup_item_label))
                    .setText(text);
        }
    }

    public void setOptionSelectedListener(OptionSelectedListener optionSelectedListener) {
        mOptionSelectedListener = optionSelectedListener;
    }

    /**
     * Note: clears selection
     */
    public void resize(int newSize) {

        if (mCheckedIndex != NO_SELECTION) {
            // clear previous selection
            setChild(mCheckedIndex, false, null);
            mCheckedIndex = NO_SELECTION;
        }

        // Reduce if too many
        while (getChildCount() > newSize) {
            removeViewAt(0);
        }

        // Add if too few
        while (getChildCount() < newSize) {
            final View child = mInflater.inflate(R.layout.dvradiogroup_item, this, false);
//            /child.setOnClickListener(mChildClickListener);
            child.setOnTouchListener(createChildTouchListener(child));
            addView(child);
        }
    }

    public interface OptionSelectedListener {
        void onSelected(DVRadioGroup group, View checkedView);
    }

    private View.OnTouchListener createChildTouchListener(View receiver) {

        final TextView iconView = (TextView) receiver.findViewById(R.id.dvradiogroup_item_image);

        return new View.OnTouchListener() {

            private CharSequence mPreviousValue;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mPreviousValue = iconView.getText();
                        iconView.setText(RADIO_PRESSED_TEXT);
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        iconView.setText(mPreviousValue);
                        return false;
                    case MotionEvent.ACTION_UP:

                        if (mCheckedIndex != NO_SELECTION) {
                            setChild(mCheckedIndex, false, null);
                        }

                        iconView.setText(RADIO_CHECKED_TEXT);

                        mCheckedIndex = DVRadioGroup.this.indexOfChild(v);

                        if (mOptionSelectedListener != null) {
                            mOptionSelectedListener.onSelected(DVRadioGroup.this, v);
                        }
                        return false;
                    default:
                        return true;
                }
            }
        };
    }
}
