package com.tealium.digitalvelocity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.gson.Question;
import com.tealium.digitalvelocity.event.LoadRequest;
import com.tealium.digitalvelocity.event.TrackEvent;
import com.tealium.digitalvelocity.survey.QuestionAdapter;
import com.tealium.digitalvelocity.util.Constant;
import com.tealium.library.DataSources;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public final class SurveyDetailActivity extends Activity implements AdapterView.OnItemClickListener {

    public static final String EXTRA_SURVEY_TITLE = "survey_title";
    public static final String EXTRA_SURVEY_ID = "survey_id";
    public static final String EXTRA_QUESTION_IDS = "question_ids";

    private String mSurveyTitle;
    private String mSurveyId;
    private String[] mQuestionIds;
    private QuestionAdapter mQuestionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_detail);

        mSurveyTitle = getIntent().getStringExtra(EXTRA_SURVEY_TITLE);
        mSurveyId = getIntent().getStringExtra(EXTRA_SURVEY_ID);
        mQuestionIds = getIntent().getStringArrayExtra(EXTRA_QUESTION_IDS);

        if (TextUtils.isEmpty(mSurveyTitle) ||
                TextUtils.isEmpty(mSurveyId) ||
                mQuestionIds == null) {
            throw new IllegalStateException();
        }

        findViewById(R.id.actionbar_image_logo).setVisibility(View.GONE);
        findViewById(R.id.actionbar_imageview_hamburger).setVisibility(View.GONE);
        findViewById(R.id.actionbar_imageview_favorites).setVisibility(View.GONE);


        final TextView titleLabel = ((TextView) findViewById(R.id.actionbar_title));
        titleLabel.setVisibility(View.VISIBLE);
        titleLabel.setText(mSurveyTitle);

        /*((TextView) findViewById(R.id.survey_detail_label_title))
                .setText(mSurveyTitle);*/

        final ListView questionsListView = (ListView) findViewById(R.id.survey_detail_listview_questions);
        questionsListView.setAdapter(mQuestionAdapter = new QuestionAdapter());
        questionsListView.setOnItemClickListener(this);

        if (Model.getInstance().isSurveyComplete(mSurveyId)) {
            Toast.makeText(this, R.string.surveydetail_already_completed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final EventBus bus = EventBus.getDefault();
        bus.post(new LoadRequest.Questions(mQuestionIds));
    }

    @Override
    protected void onStop() {

        final EventBus bus = EventBus.getDefault();

        if (bus.isRegistered(mQuestionAdapter)) {
            bus.unregister(mQuestionAdapter);
        }

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        final EventBus bus = EventBus.getDefault();
        if (!bus.isRegistered(mQuestionAdapter)) {
            bus.register(mQuestionAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Log.d(Constant.TAG, "onItemClick:" + position);

        if (!mQuestionAdapter.isSubmitButton(position)) {
            return;
        }

        if (!mQuestionAdapter.isComplete()) {
            Toast.makeText(this, R.string.surveydetail_incomplete, Toast.LENGTH_SHORT).show();
            return;
        }

        final EventBus bus = EventBus.getDefault();
        final Map<String, String> asData = new HashMap<>(3);
        final Map<Question, String> answerData = new HashMap<>(mQuestionAdapter.getQuestionCount());
        asData.put("survey_title", mSurveyTitle);
        asData.put(DataSources.Key.LINK_ID, "survey_complete");
        asData.put("survey_id", mSurveyId);

        for (int i = 0; i < mQuestionAdapter.getQuestionCount(); i++) {
            final Question question = mQuestionAdapter.get(i);
            final String answer = mQuestionAdapter.getAnswer(i);

            answerData.put(question, answer);
            asData.put("survey_question_id", question.getId());
            asData.put("survey_question", question.getTitle());
            asData.put("survey_answer", answer);

            final TrackEvent event = TrackEvent.createLinkTrackEvent();
            event.getData().putAll(asData);
            bus.post(event);
        }

        Model.getInstance().setSurveyComplete(mSurveyId, answerData);
        Toast.makeText(this, R.string.surveydetail_completed, Toast.LENGTH_SHORT).show();
        finish();
    }


}
