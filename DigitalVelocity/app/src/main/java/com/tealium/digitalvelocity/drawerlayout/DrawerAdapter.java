package com.tealium.digitalvelocity.drawerlayout;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tealium.digitalvelocity.R;

public final class DrawerAdapter extends BaseAdapter {

    private final Activity mActivity;
    private final LayoutInflater mLayoutInflater;
    private final DrawerItem[] mDrawerItems;

    public DrawerAdapter(Activity activity) {
        if ((mActivity = activity) == null) {
            throw new IllegalArgumentException();
        }
        mLayoutInflater = LayoutInflater.from(mActivity);
        mDrawerItems = DrawerItem.values();
    }

    @Override
    public int getCount() {
        return mDrawerItems.length;
    }

    @Override
    public Object getItem(int position) {
        return mDrawerItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_drawerlayout, parent, false);
            convertView.setTag(viewHolder = new ViewHolder(convertView));
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final DrawerItem item = mDrawerItems[position];
        final boolean isCurrent = mActivity.getClass().equals(item.getActivityClass());

        viewHolder.setItem(item, isCurrent);

        return convertView;
    }
}
