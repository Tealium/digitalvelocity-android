package com.tealium.digitalvelocity.event;

import android.text.TextUtils;

public class TealiumConfigEvent {
    private final String mAccountName;
    private final String mProfileName;
    private final String mEnvName;

    public TealiumConfigEvent(String accountName, String profileName, String envName) {

        if (!isValid(accountName, profileName, envName)) {
            throw new IllegalArgumentException();
        }

        mAccountName = accountName;
        mProfileName = profileName;
        mEnvName = envName;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public String getEnvName() {
        return mEnvName;
    }

    public String getProfileName() {
        return mProfileName;
    }

    public static boolean isValid(String accountName, String profileName, String envName) {
        return !TextUtils.isEmpty(accountName) &&
                !TextUtils.isEmpty(profileName) &&
                !TextUtils.isEmpty(envName);
    }
}
