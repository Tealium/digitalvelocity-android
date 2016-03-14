package com.tealium.digitalvelocity.event;


import com.tealium.digitalvelocity.util.Util;

public final class DemoChangeEvent {
    private final String mAccountName;
    private final String mProfileName;
    private final String mEnvironmentName;
    private final String mDemoInstanceId;

    public DemoChangeEvent(String accountName, String profileName, String environmentName) {

        mDemoInstanceId = Util.createInstanceId(accountName, profileName, environmentName);

        if (mDemoInstanceId == null) {
            mAccountName = null;
            mProfileName = null;
            mEnvironmentName = null;
        } else {
            mAccountName = accountName;
            mProfileName = profileName;
            mEnvironmentName = environmentName;
        }
    }

    public String getAccountName() {
        return mAccountName;
    }

    public String getProfileName() {
        return mProfileName;
    }

    public String getEnvironmentName() {
        return mEnvironmentName;
    }

    public String getDemoInstanceId() {
        return mDemoInstanceId;
    }
}
