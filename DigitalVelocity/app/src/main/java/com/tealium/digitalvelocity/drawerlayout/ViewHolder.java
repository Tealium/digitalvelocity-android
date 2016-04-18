package com.tealium.digitalvelocity.drawerlayout;


import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.tealium.digitalvelocity.R;

final class ViewHolder {
    private final TextView mTitleLabel;
    private final TextView mNotificationLabel;

    public ViewHolder(View view) {
        mTitleLabel = (TextView) view.findViewById(R.id.drawerlayout_item_label_title);
        mNotificationLabel = (TextView) view.findViewById(R.id.drawerlayout_item_label_notification);
    }

    void setItem(DrawerItem item, boolean isCurrent) {

        mNotificationLabel.setVisibility(View.INVISIBLE);

        mTitleLabel.setText(item.toString());
        if (isCurrent) {
            mTitleLabel.setTextColor(Color.WHITE);
        } else {
            mTitleLabel.setTextColor(Color.rgb(0xa6, 0xaa, 0xa9));
        }
    }
}
