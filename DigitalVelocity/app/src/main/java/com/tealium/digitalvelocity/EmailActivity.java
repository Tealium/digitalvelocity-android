package com.tealium.digitalvelocity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tealium.digitalvelocity.data.TrackingManager;
import com.tealium.digitalvelocity.event.TrackUpdateEvent;
import com.tealium.digitalvelocity.util.Util;

import de.greenrobot.event.EventBus;


public final class EmailActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        ((EditText) findViewById(R.id.email_input))
                .setOnEditorActionListener(createInputListener());

        findViewById(R.id.email_button_enter)
                .setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        this.prompt();
    }

    private void prompt() {
        Toast.makeText(this, R.string.email_prompt, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View ignored) {

        final String text = ((TextView) findViewById(R.id.email_input)).getText().toString();
        if (Util.isEmptyOrNull(text)) {
            prompt();
            return;
        }

        if (!Util.isValidEmail(text)) {
            Toast.makeText(this,
                    getString(R.string.email_malformed_input, text),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        EventBus.getDefault().post(new TrackUpdateEvent(TrackingManager.Key.EMAIL, text));

        finish();
    }

    private TextView.OnEditorActionListener createInputListener() {
        return new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClick(null);
                    return true;
                }

                return false;
            }
        };
    }
}
