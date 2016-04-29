package com.tealium.digitalvelocity.view;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.tealium.digitalvelocity.R;
import com.tealium.digitalvelocity.data.Model;

public final class Dialogs {
    private Dialogs() {
    }

    public static AlertDialog createBluetoothPrompt(Context context) {

        final LayoutInflater inflater = LayoutInflater.from(context);

        View titleView = inflater.inflate(R.layout.dialog_bluetooth_prompt_title, null);
        View view = inflater.inflate(R.layout.dialog_bluetooth_prompt, null);

        ((CheckBox) view.findViewById(R.id.dialog_bluetooth_prompt_checkbox))
                .setOnCheckedChangeListener(createBluetoothPromptCheckboxListener());

        DialogInterface.OnClickListener listener = createBluetoothPromptListener(context);

        return new AlertDialog.Builder(context)
                .setCustomTitle(titleView)
                .setView(view)
                .setNegativeButton(R.string.dialog_bluetooth_prompt_negative, listener)
                .setPositiveButton(R.string.dialog_bluetooth_prompt_positive, listener)
                .create();
    }

    private static CompoundButton.OnCheckedChangeListener createBluetoothPromptCheckboxListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Model.getInstance().setBluetoothPromptEnabled(!isChecked);
            }
        };
    }

    private static DialogInterface.OnClickListener createBluetoothPromptListener(final Context context) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != DialogInterface.BUTTON_POSITIVE) {
                    return;
                }

                BluetoothAdapter.getDefaultAdapter().enable();
            }
        };
    }
}
