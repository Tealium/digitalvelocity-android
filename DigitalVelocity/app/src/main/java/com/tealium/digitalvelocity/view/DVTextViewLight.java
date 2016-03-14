package com.tealium.digitalvelocity.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tealium.digitalvelocity.data.Model;

public class DVTextViewLight extends TextView {

    {
        if (!this.isInEditMode()) {
            this.setTypeface(Model.getInstance().getLightTypeface());
        }
    }

    public DVTextViewLight(Context context) {
        super(context);
    }

    public DVTextViewLight(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DVTextViewLight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DVTextViewLight(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


}
