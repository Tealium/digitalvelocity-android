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

import java.util.Locale;

import de.greenrobot.event.EventBus;


public final class EmailActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        ((EditText) this.findViewById(R.id.email_input))
                .setOnEditorActionListener(createInputListener());

        this.findViewById(R.id.email_button_enter)
                .setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        this.prompt();
    }

    private void prompt() {
        Toast.makeText(this, "Please enter your registered e-mail address.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View ignored) {

        final String text = ((TextView) this.findViewById(R.id.email_input)).getText().toString();
        if (Util.isEmptyOrNull(text)) {
            this.prompt();
            return;
        }

        if (!Util.isValidEmail(text)) {
            Toast.makeText(this,
                    String.format(Locale.ROOT, "I'm sorry, I don't understand \"%s\".", text),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        EventBus.getDefault().post(new TrackUpdateEvent(TrackingManager.Key.EMAIL, text));

        this.finish();
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
