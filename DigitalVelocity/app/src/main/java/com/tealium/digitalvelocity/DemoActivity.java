package com.tealium.digitalvelocity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.event.DemoChangeEvent;
import com.tealium.digitalvelocity.event.TraceUpdateEvent;

import de.greenrobot.event.EventBus;

// TODO: stop visibility if usage data is toggled off
public final class DemoActivity extends DrawerLayoutActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        findViewById(R.id.demo_save_button)
                .setOnClickListener(createSaveClickListener());
        findViewById(R.id.demo_trace_label)
                .setOnClickListener(createASLabelListener());

        final Model model = Model.getInstance();
        setText(R.id.demo_account_input, model.getDemoAccount());
        setText(R.id.demo_profile_input, model.getDemoProfile());
        setText(R.id.demo_env_input, model.getDemoEnvironment());
        setText(R.id.demo_trace_input, model.getTraceId());
    }

    private void updateDemoConfiguration() {
        final String accountName = getTextFromTextView(R.id.demo_account_input);
        final String profileName = getTextFromTextView(R.id.demo_profile_input);
        final String environmentName = getTextFromTextView(R.id.demo_env_input);
        final String traceId = getTextFromTextView(R.id.demo_trace_input);

        final TraceUpdateEvent traceUpdateEvent =
                TextUtils.isEmpty(traceId) ?
                        TraceUpdateEvent.createLeaveTraceEvent() :
                        TraceUpdateEvent.createJoinTraceEvent(traceId);
        final DemoChangeEvent demoChangeEvent = new DemoChangeEvent(accountName, profileName, environmentName);

        final EventBus eventBus = EventBus.getDefault();

        eventBus.post(demoChangeEvent);
        eventBus.post(traceUpdateEvent);

        if (demoChangeEvent.getDemoInstanceId() == null) {
            Toast.makeText(this, R.string.demo_not_running_toast, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(
                    R.string.demo_running_toast_format,
                    demoChangeEvent.getDemoInstanceId()), Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener createSaveClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDemoConfiguration();
                finish();
            }
        };
    }

    private String getTextFromTextView(int id) {
        return ((TextView) findViewById(id)).getText().toString();
    }

    private void setText(int viewId, String text) {
        ((TextView) findViewById(viewId)).setText(text);
    }

    private View.OnClickListener createASLabelListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DemoActivity.this, WebViewActivity.class)
                        .putExtra(WebViewActivity.EXTRA_TITLE, "AudienceStream")
                        .setData(Uri.parse("http://tealium.com/products/data-distribution/audiencestream/")));
            }
        };
    }
}
