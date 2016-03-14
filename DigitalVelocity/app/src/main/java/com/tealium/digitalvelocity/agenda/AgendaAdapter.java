package com.tealium.digitalvelocity.agenda;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.gson.AgendaItem;
import com.tealium.digitalvelocity.data.gson.Category;
import com.tealium.digitalvelocity.event.LoadedEvent;
import com.tealium.digitalvelocity.util.Constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public final class AgendaAdapter extends BaseAdapter {

    private final TreeMap<Category, List<AgendaItem>> source;
    private final ArrayList<Comparable> stagedItems;
    private final View noFavoritesLabel;

    private boolean isFilteringFavorites;

    private long latestUpdated;

    public AgendaAdapter(@NonNull View noFavoritesLabel) {
        this.noFavoritesLabel = noFavoritesLabel;
        this.source = new TreeMap<>();
        this.stagedItems = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return this.stagedItems.size();
    }

    @Override
    public Object getItem(int position) {
        return this.stagedItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Object item = this.stagedItems.get(position);
        if (item instanceof Category) {
            return getHeaderView((Category) item, convertView, parent);
        } else if (item instanceof AgendaItem) {
            return getAgendaItemView((AgendaItem) item, convertView, parent);
        }

        IllegalStateException t = new IllegalStateException();
        Log.e(Constant.TAG, "Don't know what to do with " + item, t);
        Log.e(Constant.TAG, Arrays.toString(this.stagedItems.toArray()));


        throw t;
    }

    public void setFilteringFavorites(boolean isFilteringFavorites) {
        this.isFilteringFavorites = isFilteringFavorites;
        this.stageItems(this.stagedItems.size());
        this.notifyDataSetChanged();
    }

    private View getHeaderView(Category category, View convertView, ViewGroup parent) {

        TextView headerLabel;

        if (convertView == null || !(convertView instanceof TextView)) {
            headerLabel = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
        } else {
            headerLabel = (TextView) convertView;
        }

        headerLabel.setText(category.getName());

        return headerLabel;
    }

    private View getAgendaItemView(AgendaItem item, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_agenda, parent, false);
            convertView.setTag(viewHolder = new ViewHolder(convertView));
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setAgendaItem(item);

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return this.stagedItems.get(position) instanceof AgendaItem;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Agenda event) {

        if (event.getLatestUpdatedAt() == this.latestUpdated) {
            return; // no changes.
        }

        this.latestUpdated = event.getLatestUpdatedAt();
        this.source.clear();

        for (AgendaItem item : event.getItems()) {

            if (item.getCategory() == null) {
                Log.e(Constant.TAG, "Null category for " + item.getId(), new IllegalArgumentException());
                continue;
            }

            List<AgendaItem> list = this.source.get(item.getCategory());
            if (list == null) {
                this.source.put(item.getCategory(), list = new LinkedList<>());
            }

            list.add(item);
        }

        this.stageItems(this.source.size() + event.getItems().size());
        this.notifyDataSetChanged();
    }

    /**
     * Stages available data based on filter settings.
     *
     * @param capacity capacity of the number of stagedItems shown by this adapter.
     */
    private void stageItems(int capacity) {

        this.stagedItems.clear();

        if (this.source.size() == 0) {
            return;
        }

        this.stagedItems.ensureCapacity(capacity);

        Set<Category> categorySet = this.source.keySet();
        Category[] categories = categorySet.toArray(new Category[categorySet.size()]);
        Arrays.sort(categories);

        final Model model = Model.getInstance();

        for (int i = 0; i < categories.length; i++) {
            Category category = categories[i];
            List<AgendaItem> agendaItems = this.source.get(category);
            Collections.sort(agendaItems);

            if (this.isFilteringFavorites) {

                boolean isCategoryStaged = false;

                for (AgendaItem item : agendaItems) {
                    if (model.isAgendaFavorite(item)) {
                        if (!isCategoryStaged) {
                            // Only add if there's eligible items
                            this.stagedItems.add(category);
                            isCategoryStaged = true;
                        }

                        this.stagedItems.add(item);
                    }
                }
            } else {
                this.stagedItems.add(category);
                this.stagedItems.addAll(agendaItems);
            }
        }

        if (this.isFilteringFavorites && this.stagedItems.size() == 0) {
            this.noFavoritesLabel.setVisibility(View.VISIBLE);
        } else {
            this.noFavoritesLabel.setVisibility(View.GONE);
        }
    }

    /**
     * Repopulates the data based on filtering rules.
     * NOTE: this method does NOT call {@link BaseAdapter#notifyDataSetChanged()}.
     */
    public void restageItems() {
        this.stageItems(this.stagedItems.size());
    }
}
