package com.tealium.digitalvelocity.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.gson.Notification;
import com.tealium.digitalvelocity.event.LoadedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public final class NotificationsAdapter extends BaseAdapter {

    private final ArrayList<Notification> notifications;

    public NotificationsAdapter() {
        this.notifications = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return this.notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return this.notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            convertView.setTag(viewHolder = new ViewHolder(convertView));
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setNotification(this.notifications.get(position));

        return convertView;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Notifications notifications) {
        this.notifications.clear();

        LinkedList<Notification> incoming = new LinkedList<>(notifications.getItems());
        Collections.sort(incoming);
        Iterator<Notification> i = incoming.iterator();

        String lastMsg = null;
        Notification current;
        while (i.hasNext()) {
            current = i.next();
            if (current.getText().equals(lastMsg)) {
                i.remove();
            }
            lastMsg = current.getText();
        }

        this.notifications.addAll(incoming);
        this.notifyDataSetChanged();
    }
}
