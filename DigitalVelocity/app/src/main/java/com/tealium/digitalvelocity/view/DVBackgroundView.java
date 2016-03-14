package com.tealium.digitalvelocity.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tealium.digitalvelocity.R;

public final class DVBackgroundView extends View implements Target {

    private final int resourceId;
    private int oldWidth;
    private int oldHeight;
    private boolean firstLoadHasOccurred = false;

    public DVBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.resourceId = extractResourceId(context, attrs);
    }

    public DVBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.resourceId = extractResourceId(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public DVBackgroundView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.resourceId = extractResourceId(context, attrs);
    }

    private static int extractResourceId(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DVBackgroundView,
                0, 0);

        try {
            return a.getResourceId(R.styleable.DVBackgroundView_src, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (this.resourceId == 0) {
            // No resource to load.
            return;
        }

        final int currentWidth = this.getWidth();
        final int currentHeight = this.getHeight();

        if (currentWidth == 0 || currentHeight == 0 || (currentWidth == this.oldWidth && currentHeight == this.oldHeight)) {
            // There is no visible surface or it's the same size
            return;
        }

        this.oldWidth = currentWidth;
        this.oldHeight = currentHeight;

        Picasso.with(this.getContext())
                .load(this.resourceId)
                .resize(currentWidth, currentHeight)
                .centerCrop()
                .into(this);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

        this.setBackground(new BitmapDrawable(this.getResources(), bitmap));

        if (!this.firstLoadHasOccurred) {
            this.startAnimation(AnimationUtils.loadAnimation(this.getContext(), R.anim.fade_in_fast));
            this.firstLoadHasOccurred = true;
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
