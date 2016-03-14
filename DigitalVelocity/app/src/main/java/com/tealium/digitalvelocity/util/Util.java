package com.tealium.digitalvelocity.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.tealium.beacon.EstimoteManager;
import com.tealium.digitalvelocity.AlarmReceiver;
import com.tealium.digitalvelocity.BuildConfig;
import com.tealium.digitalvelocity.data.Model;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public final class Util {
    private Util() {
    }

    public static boolean isEmptyOrNull(String s) {
        return s == null || s.length() == 0;
    }

    public static String describe(Activity activity) {
        return describe(activity.findViewById(android.R.id.content));
    }

    public static String describe(View view) {
        if (view == null) {
            return null;
        }
        return describe("", view);
    }

    private static String describe(String prefix, View view) {
        final String name = view.getClass().getSimpleName();
        String description = prefix + "<" + name;
        if (view instanceof ViewGroup && ((ViewGroup) view).getChildCount() > 0) {
            ViewGroup group = (ViewGroup) view;
            description += ">\n";
            for (int i = 0; i < group.getChildCount(); i++) {
                description += describe(prefix + "|", group.getChildAt(i));
            }
            return description + prefix + "</" + name + ">\n";
        } else if (view instanceof TextView) {
            return description + ">" + ((TextView) view).getText() + "</" + name + ">\n";
        } else {
            return description + "/>\n";
        }
    }

    public static String describe(Map<?, ?> map) {
        if (map == null) {
            return null;
        }

        String contents = "{\r\n";

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            contents += "\t" + entry.getKey() + " : " + entry.getValue();
        }

        return contents;
    }

    public static boolean isValidEmail(String email) {

        if (email == null) {
            return false;
        }

        return email.matches("^[a-zA-Z0-9_\\-\\.]+@[a-zA-Z0-9_\\-\\.]+\\.[a-zA-Z]+$");
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getActivityTitle(Activity activity) {
        PackageManager packageManager = activity.getPackageManager();
        try {
            final int labelId = packageManager.getActivityInfo(activity.getComponentName(), 0).labelRes;
            if (labelId != 0) {
                return activity.getString(labelId);
            }
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
            Log.e(Constant.TAG, "Error loading label for " + activity.getClass().getSimpleName(), e);
        }

        return activity.getClass().getName();

    }

    public static boolean hasBluetooth() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public static boolean isInetConnected(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } else {
            Log.e(Constant.TAG, "Request focus false");
        }
    }

    public static void attemptToStartMonitoringBeacons(Context context) {
        final Model model = Model.getInstance();
        final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        EstimoteManager em = EstimoteManager.getInstance();
        if (!em.isListening() &&
                AlarmReceiver.isInMonitoringWindow() &&
                model.getMonitoringStartHour() <= hour &&
                model.getMonitoringStopHour() >= hour) {

            if (BuildConfig.DEBUG) Log.d(Constant.TAG, "Started monitoring...");
            em.start();
        }

    }

    /**
     * @return instanceId if account/profile/environment are available or null if invalid
     */
    public static String createInstanceId(
            String accountName, String profileName, String environmentName) {

        final boolean isInvalid = TextUtils.isEmpty(accountName) ||
                TextUtils.isEmpty(profileName) ||
                TextUtils.isEmpty(environmentName);

        if (isInvalid) {
            return null;
        }

        return String.format(Locale.ROOT, "%s/%s/%s", accountName, profileName, environmentName);
    }

}
