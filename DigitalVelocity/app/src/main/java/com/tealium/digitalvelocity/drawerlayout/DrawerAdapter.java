package com.tealium.digitalvelocity.drawerlayout;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tealium.digitalvelocity.R;

public class DrawerAdapter extends BaseAdapter {

    private final Activity activity;
    private final LayoutInflater layoutInflater;

    public DrawerAdapter(Activity activity) {
        if ((this.activity = activity) == null) {
            throw new IllegalArgumentException();
        }
        this.layoutInflater = LayoutInflater.from(this.activity);
    }

    @Override
    public int getCount() {
        return DrawerItem.values().length;
    }

    @Override
    public Object getItem(int position) {
        return DrawerItem.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView label = (TextView) convertView;
        if (label == null) {
            label = (TextView) this.layoutInflater.inflate(R.layout.item_drawerlayout, parent, false);
        }

        DrawerItem item = DrawerItem.values()[position];

        label.setText(item.toString());
        if (this.activity.getClass().equals(item.getActivityClass())) {
            label.setTextColor(Color.WHITE);
        }

        return label;
    }
}
