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

        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_bluetooth_prompt, null);

        ((CheckBox) view.findViewById(R.id.dialog_bluetooth_prompt_checkbox))
                .setOnCheckedChangeListener(createBluetoothPromptCheckboxListener());

        DialogInterface.OnClickListener listener = createBluetoothPromptListener();

        return new AlertDialog.Builder(context)
                .setView(view)
                .setTitle(R.string.dialog_bluetooth_prompt_title)
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

    private static DialogInterface.OnClickListener createBluetoothPromptListener() {
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
