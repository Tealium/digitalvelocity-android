package com.tealium.digitalvelocity.notifications;

import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.gson.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ViewHolder {

    private final SimpleDateFormat format;
    private final TextView messageLabel;
    private final TextView timeLabel;

    public ViewHolder(View view) {

        this.format = new SimpleDateFormat(
                (DateFormat.is24HourFormat(view.getContext()) ? "EEEE HH:mm" : "EEEE h:mm a"),
                Locale.ROOT);
        this.messageLabel = (TextView) view.findViewById(R.id.item_notifications_label_message);
        this.timeLabel = (TextView) view.findViewById(R.id.item_notifications_label_time);
    }

    public void setNotification(Notification notification) {
        this.messageLabel.setText(notification.getText());

        this.format.setTimeZone(notification.isFromParseTable() ?
                TimeZone.getTimeZone("UTC") : TimeZone.getDefault());

        this.timeLabel.setText(this.format.format(new Date(notification.getUpdatedAt())));
    }
}
