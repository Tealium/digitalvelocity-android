package com.tealium.digitalvelocity.sponsor;

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
import com.tealium.digitalvelocity.data.gson.Sponsor;
import com.tealium.digitalvelocity.util.Util;

import java.io.File;

final class ViewHolder implements Animation.AnimationListener, Callback {

    private final ImageView defaultImageView;
    private final ImageView logoImageView;
    private final TextView titleLabel;
    private final TextView subtitleLabel;
    private String sponsorId;

    public ViewHolder(View view) {
        this.defaultImageView = (ImageView) view.findViewById(R.id.item_sponsor_image_default);
        this.logoImageView = (ImageView) view.findViewById(R.id.item_sponsor_image_logo);
        this.titleLabel = (TextView) view.findViewById(R.id.item_sponsor_label_title);
        this.subtitleLabel = (TextView) view.findViewById(R.id.item_sponsor_label_subtitle);

        view.findViewById(R.id.item_sponsor_button_location)
                .setVisibility(View.GONE);
    }

    public void setSponsor(Sponsor sponsor) {

        if (sponsor.getId().equals(this.sponsorId)) {
            return;
        }

        if(this.sponsorId != null) {
            Picasso.with(this.logoImageView.getContext())
                    .cancelTag(this.sponsorId);
        }

        this.sponsorId = sponsor.getId();

        this.defaultImageView.setAlpha(1.0f);
        this.logoImageView.setImageBitmap(null);

        this.titleLabel.setText(sponsor.getName());
        this.subtitleLabel.setText(sponsor.getDescription());
        this.subtitleLabel.setVisibility(Util.isEmptyOrNull(sponsor.getDescription()) ? View.GONE : View.VISIBLE);


        File file = IOUtils.getImageFile(this.logoImageView.getContext(), this.sponsorId);
        if (file != null) {
            RequestCreator req = Picasso.with(this.logoImageView.getContext())
                    .load(file)
                    .tag(sponsor.getId());

            if (this.logoImageView.getWidth() > 0 && this.logoImageView.getHeight() > 0) {
                req.resize(this.logoImageView.getWidth(), this.logoImageView.getHeight())
                        .centerInside();
            }

            req.into(this.logoImageView, this);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
        this.defaultImageView.setAlpha(1.0f);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        this.defaultImageView.setAlpha(0.0f);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onSuccess() {
        Animation a = AnimationUtils.loadAnimation(defaultImageView.getContext(), R.anim.fade_out_slow);
        a.setAnimationListener(this);
        defaultImageView.startAnimation(a);
    }

    @Override
    public void onError() {

    }
}
