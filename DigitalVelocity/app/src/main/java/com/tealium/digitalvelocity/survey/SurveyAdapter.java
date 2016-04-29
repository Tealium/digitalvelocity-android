package com.tealium.digitalvelocity.survey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.gson.Survey;
import com.tealium.digitalvelocity.event.LoadedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SurveyAdapter extends BaseAdapter {

    private final List<Survey> mSurveys;

    public SurveyAdapter() {
        mSurveys = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mSurveys.size();
    }

    @Override
    public Object getItem(int position) {
        return mSurveys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mSurveys.get(position).getId().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final SurveyViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_survey, parent, false);
            convertView.setTag(viewHolder = new SurveyViewHolder(convertView));
        } else {
            viewHolder = (SurveyViewHolder) convertView.getTag();
        }

        viewHolder.update(mSurveys.get(position));

        return convertView;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Surveys event) {
        mSurveys.clear();
        mSurveys.addAll(event.getItems());
        Collections.sort(mSurveys);
        notifyDataSetChanged();
    }

}
