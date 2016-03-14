package com.tealium.digitalvelocity.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tealium.digitalvelocity.data.Model;

public class DVTextView extends TextView {

    {
        if (!this.isInEditMode()) {
            this.setTypeface(Model.getInstance().getDefaultTypeface());
        }
    }

    public DVTextView(Context context) {
        super(context);
    }

    public DVTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DVTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DVTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


}
