package com.tealium.digitalvelocity.survey;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.gson.Question;
import com.tealium.digitalvelocity.view.DVRadioGroup;

import java.util.Locale;

final class QuestionViewHolder implements DVRadioGroup.OptionSelectedListener {

    private final TextView mTitleLabel;
    private final DVRadioGroup mAnswersGroup;
    private final AnswerSelectedListener mAnswerListener;

    private Question mQuestion;


    QuestionViewHolder(View view, AnswerSelectedListener listener) {
        mTitleLabel = (TextView) view.findViewById(R.id.item_surveydetail_question_label_title);
        mAnswersGroup = (DVRadioGroup) view.findViewById(R.id.item_surveydetail_question_group_answers);
        mAnswersGroup.setOptionSelectedListener(this);

        if ((mAnswerListener = listener) == null) {
            throw new IllegalArgumentException();
        }
    }

    void update(int position, Question question, String currentAnswer) {
        if (mQuestion != null && TextUtils.equals(mQuestion.getId(), question.getId())) {
            return;
        }

        final int oneBasedPosition = position + 1;

        mQuestion = question;
        mTitleLabel.setText(String.format(Locale.ROOT, "%d). %s", oneBasedPosition, question.getTitle()));
        mAnswersGroup.resize(question.getAnswers().size());

        for (int i = 0; i < question.getAnswers().size(); i++) {
            final String answer = question.getAnswers().get(i);
            mAnswersGroup.setChild(i, answer.equals(currentAnswer), answer);
        }
    }

    @Override
    public void onSelected(DVRadioGroup group, View selectedView) {
        final TextView answer = (TextView) selectedView.findViewById(R.id.dvradiogroup_item_label);
        mAnswerListener.onAnswerSelected(mQuestion, "" + answer.getText());
    }

    interface AnswerSelectedListener {
        void onAnswerSelected(Question question, String answer);
    }
}
