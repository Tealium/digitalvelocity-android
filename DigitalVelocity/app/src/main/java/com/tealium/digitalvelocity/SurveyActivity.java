package com.tealium.digitalvelocity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tealium.digitalvelocity.data.gson.Survey;
import com.tealium.digitalvelocity.event.LoadRequest;
import com.tealium.digitalvelocity.survey.SurveyAdapter;

import de.greenrobot.event.EventBus;

public class SurveyActivity extends DrawerLayoutActivity {

    private SurveyAdapter mSurveyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        ListView surveyList = ((ListView) findViewById(R.id.survey_listview_surveys));
        surveyList.setAdapter(mSurveyAdapter = new SurveyAdapter());
        surveyList.setOnItemClickListener(createItemClickListener());
    }

    private AdapterView.OnItemClickListener createItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Survey survey = (Survey) parent.getAdapter().getItem(position);
                final String[] questionIds = survey.getQuestionIds()
                        .toArray(new String[survey.getQuestionIds().size()]);

                startActivity(new Intent(SurveyActivity.this, SurveyDetailActivity.class)
                        .putExtra(SurveyDetailActivity.EXTRA_SURVEY_ID, survey.getId())
                        .putExtra(SurveyDetailActivity.EXTRA_QUESTION_IDS, questionIds)
                        .putExtra(SurveyDetailActivity.EXTRA_SURVEY_TITLE, survey.getTitle()));
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        final EventBus bus = EventBus.getDefault();
        if (!bus.isRegistered(mSurveyAdapter)) {
            bus.register(mSurveyAdapter);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        final EventBus bus = EventBus.getDefault();
        bus.post(new LoadRequest.Surveys());
    }

    @Override
    protected void onStop() {

        final EventBus bus = EventBus.getDefault();
        if (bus.isRegistered(mSurveyAdapter)) {
            bus.unregister(mSurveyAdapter);
        }

        super.onStop();
    }
}
