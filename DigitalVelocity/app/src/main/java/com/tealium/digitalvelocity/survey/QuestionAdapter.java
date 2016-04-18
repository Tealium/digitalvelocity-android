package com.tealium.digitalvelocity.survey;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.gson.Question;
import com.tealium.digitalvelocity.event.LoadedEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class QuestionAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_QUESTION = 0;
    private static final int VIEW_TYPE_SUBMIT = 1;

    private final Map<Question, String> mSelectedAnswers;
    private final List<Question> mQuestions;
    private final QuestionViewHolder.AnswerSelectedListener mAnswerListener;

    public QuestionAdapter() {
        mSelectedAnswers = new HashMap<>();
        mQuestions = new ArrayList<>();
        mAnswerListener = createAnswerListener();
    }

    public Question get(int position) {
        return mQuestions.get(position);
    }

    public String getAnswer(int position) {
        return mSelectedAnswers.get(mQuestions.get(position));
    }

    public int getQuestionCount() {
        return mQuestions.size();
    }

    /**
     * @return whether every question is answered
     */
    public boolean isComplete() {
        return mSelectedAnswers.size() == mQuestions.size();
    }

    public boolean isSubmitButton(int position) {
        return position == mQuestions.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return mQuestions.size() == position ? VIEW_TYPE_SUBMIT : VIEW_TYPE_QUESTION;
    }

    @Override
    public int getCount() {
        return mQuestions.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return mQuestions.size() == position ? null : mQuestions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mQuestions.size() == position ? Long.MIN_VALUE : mQuestions.get(position).getId().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == mQuestions.size()) {
            return getSubmitView(convertView, parent);
        }

        return getQuestionView(position, convertView, parent);
    }

    private View getQuestionView(int position, View convertView, ViewGroup parent) {

        final QuestionViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_surveydetail_question, parent, false);
            convertView.setTag(viewHolder = new QuestionViewHolder(convertView, mAnswerListener));
        } else {
            viewHolder = (QuestionViewHolder) convertView.getTag();
        }

        final Question question = mQuestions.get(position);
        viewHolder.update(position, question, mSelectedAnswers.get(question));

        return convertView;
    }

    private View getSubmitView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_surveydetail_submit, parent, false);
        }

        return convertView;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadedEvent.Questions event) {
        mSelectedAnswers.clear();
        mQuestions.clear();
        mQuestions.addAll(event.getItems());

        final Model model = Model.getInstance();

        for (int i = 0; i < mQuestions.size(); i++) {
            final Question question = mQuestions.get(i);
            mSelectedAnswers.put(question, model.getSurveyAnswer(question));
        }

        notifyDataSetChanged();
    }

    private QuestionViewHolder.AnswerSelectedListener createAnswerListener() {
        return new QuestionViewHolder.AnswerSelectedListener() {
            @Override
            public void onAnswerSelected(Question question, String answer) {
                mSelectedAnswers.put(question, answer);
            }
        };
    }
}
