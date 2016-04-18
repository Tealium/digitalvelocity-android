package com.tealium.digitalvelocity.survey;

import android.view.View;
import android.widget.TextView;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.gson.Survey;

final class SurveyViewHolder {

    private final TextView mTitleLabel;
    private final TextView mCheckBox;

    public SurveyViewHolder(View view) {
        mTitleLabel = (TextView) view.findViewById(R.id.item_survey_label_title);
        mCheckBox = (TextView) view.findViewById(R.id.item_survey_label_completed);
    }

    void update(Survey survey) {
        mTitleLabel.setText(survey.getTitle());
        mCheckBox.setText(Model.getInstance().isSurveyComplete(survey.getId()) ?
                R.string.survey_square_checked : R.string.survey_square_unchecked);
    }
}
