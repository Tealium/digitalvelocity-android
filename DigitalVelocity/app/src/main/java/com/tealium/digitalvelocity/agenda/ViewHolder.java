package com.tealium.digitalvelocity.agenda;

import android.os.Build;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.IOUtils;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.gson.AgendaItem;
import com.tealium.digitalvelocity.util.Util;

import java.io.File;

import de.greenrobot.event.EventBus;

final class ViewHolder implements View.OnClickListener, Animation.AnimationListener, Callback {

    private final View mRootView;
    private final ImageView mDefaultImageView;
    private final ImageView mImageView;
    private final TextView mFaLabel;
    private final TextView mTitleLabel;
    private final TextView mSubtitleLabel;
    private final TextView mLocationButton;
    private final View mFavoriteImage;
    private final int mColorPast;
    private final int mColorFuture;
    private String mItemId;
    private String mLocationId;

    public ViewHolder(View view) {
        mRootView = view.findViewById(R.id.item_agenda_root);
        mDefaultImageView = (ImageView) view.findViewById(R.id.item_agenda_image_default);
        mImageView = (ImageView) view.findViewById(R.id.item_agenda_image);
        mFaLabel = (TextView) view.findViewById(R.id.item_agenda_label_fa);
        mTitleLabel = (TextView) view.findViewById(R.id.item_agenda_label_title);
        mSubtitleLabel = (TextView) view.findViewById(R.id.item_agenda_label_subtitle);
        mLocationButton = (TextView) view.findViewById(R.id.item_agenda_button_location);
        mFavoriteImage = view.findViewById(R.id.item_agenda_label_favorite);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mColorPast = view.getResources().getColor(R.color.item_agenda_past, null);
            mColorFuture = view.getResources().getColor(R.color.item_agenda_future, null);
        } else {
            mColorPast = view.getResources().getColor(R.color.item_agenda_past);
            mColorFuture = view.getResources().getColor(R.color.item_agenda_future);
        }

        view.findViewById(R.id.item_agenda_button_location).setOnClickListener(this);
    }

    public void setAgendaItem(AgendaItem item) {

        mFavoriteImage.setVisibility(Model.getInstance().isAgendaFavorite(item) ? View.VISIBLE : View.GONE);

        if (item.getId().equals(mItemId)) {
            return;
        }

        mRootView.setBackgroundColor(
                (item.getEnd() < System.currentTimeMillis()) ?
                        mColorPast : mColorFuture);

        if (mItemId != null) {
            Picasso.with(mImageView.getContext())
                    .cancelTag(mItemId);
        }

        mItemId = item.getId();

        mTitleLabel.setText(item.getTitle());

        mSubtitleLabel.setText(item.getTimeLocDescription(mSubtitleLabel.getContext()));

        mLocationButton.setVisibility((mLocationId = item.getLocationId()) == null ?
                View.GONE : View.VISIBLE);

        final File file = IOUtils.getImageFile(mImageView.getContext(), mItemId);
        final boolean hasFAValue = !Util.isEmptyOrNull(item.getFontAwesomeValue());
        final boolean imgExists = file != null;

        if (imgExists || !hasFAValue) {
            mDefaultImageView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.VISIBLE);
            mFaLabel.setVisibility(View.INVISIBLE);

            mDefaultImageView.setAlpha(1.0f);
            mImageView.setImageBitmap(null);

            if (imgExists) {
                RequestCreator req = Picasso.with(mImageView.getContext())
                        .load(file)
                        .tag(item.getId());

                if (mImageView.getWidth() > 0 && mImageView.getHeight() > 0) {
                    req.resize(mImageView.getWidth(), mImageView.getHeight())
                            .centerInside();
                }

                req.into(mImageView, this);
            }
        } else {
            mDefaultImageView.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.INVISIBLE);
            mFaLabel.setVisibility(View.VISIBLE);

            mFaLabel.setText(Html.fromHtml("&#x" + item.getFontAwesomeValue() + ';'));
        }
    }

    @Override
    public void onClick(View v) {
        EventBus.getDefault().post(new LocationClickEvent(mLocationId));
    }

    @Override
    public void onAnimationStart(Animation animation) {
        mDefaultImageView.setAlpha(1.0f);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mDefaultImageView.setAlpha(0.0f);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onSuccess() {
        Animation out = AnimationUtils.loadAnimation(mDefaultImageView.getContext(), R.anim.fade_out_slow);
        Animation in = AnimationUtils.loadAnimation(mDefaultImageView.getContext(), R.anim.fade_in_slow);

        out.setAnimationListener(this);
        mDefaultImageView.startAnimation(out);
        mImageView.startAnimation(in);
    }

    @Override
    public void onError() {

    }
}

