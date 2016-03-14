package com.tealium.digitalvelocity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tealium.digitalvelocity.data.Model;
import com.tealium.digitalvelocity.data.TrackingManager;
import com.tealium.digitalvelocity.event.TrackUpdateEvent;
import com.tealium.digitalvelocity.event.UsageDataToggle;
import com.tealium.digitalvelocity.util.Util;

import java.util.Locale;

import de.greenrobot.event.EventBus;

public final class SettingsActivity extends DrawerLayoutActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ((TextView) findViewById(R.id.settings_email_input))
                .setText(Model.getInstance().getUserEmail());

        final Switch usageSwitch = (Switch) findViewById(R.id.settings_usage_switch);
        usageSwitch.setChecked(Model.getInstance().isUsageDataEnabled());
        usageSwitch.setOnCheckedChangeListener(createUsageListener());
    }

    @Override
    protected void onPause() {
        super.onPause();

        Model model = Model.getInstance();

        final String email = ((TextView) findViewById(R.id.settings_email_input))
                .getText().toString();

        if (model.getUserEmail().equals(email)) {
            // no change occurred
            return;
        }

        String msg;

        if (Util.isValidEmail(email)) {
            msg = String.format(Locale.ROOT, "Updated e-mail to \"%s\".", email);
            EventBus.getDefault().post(new TrackUpdateEvent(TrackingManager.Key.EMAIL, email));
        } else {
            msg = String.format(Locale.ROOT, "\"%s\" is not a valid e-mail.", email);
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private static CompoundButton.OnCheckedChangeListener createUsageListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EventBus.getDefault().post(new UsageDataToggle(isChecked));
            }
        };
    }
}
