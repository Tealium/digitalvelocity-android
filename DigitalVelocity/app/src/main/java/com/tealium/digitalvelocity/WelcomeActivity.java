package com.tealium.digitalvelocity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tealium.digitalvelocity.data.Model;

public final class WelcomeActivity extends DrawerLayoutActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BuildConfig.DEBUG) {
            View debug = this.findViewById(R.id.main_button_debug);
            debug.setVisibility(View.VISIBLE);
            debug.setOnClickListener(createDebugClickListener());
        }

        if (Model.getInstance().getUserEmail() == null) {
            startActivity(new Intent(this, EmailActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Model model = Model.getInstance();
        this.setLabelText(R.id.main_label_year, model.getWelcomeYear());
        this.setLabelText(R.id.main_label_description, model.getWelcomeDescription());
        this.setLabelText(R.id.main_label_subtitle, model.getWelcomeSubtitle());
    }

    private void setLabelText(int labelId, CharSequence text) {
        ((TextView) this.findViewById(labelId)).setText(text);
    }

    private View.OnClickListener createDebugClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, DebugActivity.class));
            }
        };
    }
}
