package com.tealium.digitalvelocity.sponsor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.WebViewActivity;
import com.tealium.digitalvelocity.data.gson.Category;
import com.tealium.digitalvelocity.data.gson.Sponsor;
import com.tealium.digitalvelocity.event.LoadedEvent;
import com.tealium.digitalvelocity.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class SponsorAdapter extends BaseAdapter implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private final Activity activity;
    private final ArrayList<Object> items;

    public SponsorAdapter(Activity activity) {
        this.activity = activity;
        this.items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Object item = this.items.get(position);

        if (item instanceof Category) {
            return getHeaverView((Category) item, convertView, parent);
        } else if (item instanceof Sponsor) {
            return getCellView((Sponsor) item, convertView, parent);
        } else if (item == null) {
            throw new RuntimeException("item is null.");
        }

        // Should never happen.
        throw new RuntimeException(item.getClass().toString());
    }

    private View getHeaverView(Category item, View convertView, ViewGroup parent) {

        if (convertView == null || !(convertView instanceof TextView)) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
        }

        ((TextView) convertView).setText(item.getName());

        return convertView;
    }

    private View getCellView(Sponsor item, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sponsor, parent, false);
            convertView.setTag(viewHolder = new ViewHolder(convertView));
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setSponsor(item);

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object item = this.getItem(position);
        if (!(item instanceof Sponsor)) {
            return;
        }

        Sponsor sponsor = (Sponsor) item;

        if (Util.isEmptyOrNull(sponsor.getUrl())) {
            Toast.makeText(this.activity, R.string.sponsor_click_error_no_url, Toast.LENGTH_SHORT).show();
            return;
        }

        this.openSponsorPageInternal(sponsor);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        Object item = this.getItem(position);
        if (!(item instanceof Sponsor)) {
            return false;
        }

        Sponsor sponsor = (Sponsor) item;

        if (Util.isEmptyOrNull(sponsor.getUrl())) {
            return false;
        }

        DialogInterface.OnClickListener openClickListener =
                this.createOpenClickListener(sponsor);

        new AlertDialog.Builder(this.activity)
                .setPositiveButton("Open in browser", openClickListener)
                .setNegativeButton("Open in app", openClickListener)
                .create().show();

        return true;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Sponsors event) {

        this.items.clear();

        Map<Category, List<Sponsor>> data = new TreeMap<>();

        for (Sponsor sponsor : event.getItems()) {
            if (sponsor.getCategory() == null) {
                continue;
            }

            if (!data.containsKey(sponsor.getCategory())) {
                data.put(sponsor.getCategory(), new LinkedList<Sponsor>());
            }

            data.get(sponsor.getCategory()).add(sponsor);
        }

        this.items.ensureCapacity(data.size() + event.getItems().size());

        List<Sponsor> sponsors;
        for (Map.Entry<Category, List<Sponsor>> entry : data.entrySet()) {
            this.items.add(entry.getKey());
            sponsors = entry.getValue();
            Collections.sort(sponsors);
            this.items.addAll(sponsors);
        }

        this.notifyDataSetChanged();
    }

    private DialogInterface.OnClickListener createOpenClickListener(final Sponsor parseCompany) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(parseCompany.getUrl()));
                        activity.startActivity(i);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        openSponsorPageInternal(parseCompany);
                        break;
                }
            }
        };
    }

    private void openSponsorPageInternal(Sponsor parseCompany) {
        Intent i = new Intent(this.activity, WebViewActivity.class);
        i.setData(Uri.parse(parseCompany.getUrl()));
        i.putExtra(WebViewActivity.EXTRA_TITLE, parseCompany.getName());
        this.activity.startActivity(i);
        //this.activity.overridePendingTransition(R.anim.in_from_left, R.anim.hold);
    }
}
