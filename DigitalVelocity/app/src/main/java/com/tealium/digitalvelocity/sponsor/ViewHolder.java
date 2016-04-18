package com.tealium.digitalvelocity.sponsor;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.tealium.digitalvelocity.BuildConfig;
import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.IOUtils;
import com.tealium.digitalvelocity.data.gson.Sponsor;
import com.tealium.digitalvelocity.util.Constant;
import com.tealium.digitalvelocity.util.Util;

import java.io.File;

final class ViewHolder implements Animation.AnimationListener, Callback, View.OnClickListener {

    private final ImageView mDefaultImageView;
    private final ImageView mLogoImageView;
    private final TextView mTitleLabel;
    private final TextView mSubtitleLabel;
    private final View mDemoClickable;
    private final TextView mDemoLabel;
    private String mSponsorEmail;
    private String mSponsorEmailMessage;
    private String mSponsorId;

    public ViewHolder(View view) {
        mDefaultImageView = (ImageView) view.findViewById(R.id.item_sponsor_image_default);
        mLogoImageView = (ImageView) view.findViewById(R.id.item_sponsor_image_logo);
        mTitleLabel = (TextView) view.findViewById(R.id.item_sponsor_label_title);
        mSubtitleLabel = (TextView) view.findViewById(R.id.item_sponsor_label_subtitle);
        mDemoClickable = view.findViewById(R.id.item_sponsor_button_demo);
        mDemoLabel = (TextView) view.findViewById(R.id.item_sponsor_label_demo);

        view.findViewById(R.id.item_sponsor_button_location)
                .setVisibility(View.GONE);
    }

    public void setSponsor(Sponsor sponsor) {

        if (sponsor.getId().equals(mSponsorId)) {
            return;
        }

        if (mSponsorId != null) {
            Picasso.with(mLogoImageView.getContext())
                    .cancelTag(mSponsorId);
        }

        mSponsorId = sponsor.getId();

        if (TextUtils.isEmpty(mSponsorEmail = sponsor.getEmail())) {
            mDemoClickable.setOnClickListener(null);
            mDemoLabel.setVisibility(View.GONE);
        } else {
            mDemoClickable.setOnClickListener(this);
            mDemoLabel.setVisibility(View.VISIBLE);
            mSponsorEmailMessage = sponsor.getEmailMessage();
        }

        mDefaultImageView.setAlpha(1.0f);
        mLogoImageView.setImageBitmap(null);

        mTitleLabel.setText(sponsor.getName());
        mSubtitleLabel.setText(sponsor.getDescription());
        mSubtitleLabel.setVisibility(Util.isEmptyOrNull(sponsor.getDescription()) ? View.GONE : View.VISIBLE);

        File file = IOUtils.getImageFile(mLogoImageView.getContext(), mSponsorId);
        if (file != null) {
            RequestCreator req = Picasso.with(mLogoImageView.getContext())
                    .load(file)
                    .tag(sponsor.getId());

            if (mLogoImageView.getWidth() > 0 && mLogoImageView.getHeight() > 0) {
                req.resize(mLogoImageView.getWidth(), mLogoImageView.getHeight())
                        .centerInside();
            }

            req.into(mLogoImageView, this);
        }
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
        Animation a = AnimationUtils.loadAnimation(mDefaultImageView.getContext(), R.anim.fade_out_slow);
        a.setAnimationListener(this);
        mDefaultImageView.startAnimation(a);
    }

    @Override
    public void onError() {
        if (BuildConfig.DEBUG) {
            Log.e(Constant.TAG, "Error Loading Sponsor Image");
        }
    }

    @Override
    public void onClick(View v) {
        try {
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{mSponsorEmail});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Demo Request");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mSponsorEmailMessage);

            v.getContext().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (ActivityNotFoundException ignored) {
            Toast.makeText(v.getContext(), "No e-mail service available", Toast.LENGTH_SHORT).show();
        }
    }
}
